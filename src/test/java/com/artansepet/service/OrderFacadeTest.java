package com.artansepet.service;

import com.artansepet.exception.BagSoldOutException;
import com.artansepet.exception.InvalidPickupCodeException;
import com.artansepet.exception.InvalidStateTransitionException;
import com.artansepet.exception.NotFoundException;
import com.artansepet.exception.PaymentFailedException;
import com.artansepet.model.Business;
import com.artansepet.model.BusinessType;
import com.artansepet.model.Customer;
import com.artansepet.model.Location;
import com.artansepet.model.Order;
import com.artansepet.model.SurpriseBag;
import com.artansepet.payment.PaytrAdapter;
import com.artansepet.payment.PaytrGateway;
import com.artansepet.repository.DataStore;
import com.artansepet.repository.InMemoryBagRepository;
import com.artansepet.repository.InMemoryOrderRepository;
import com.artansepet.repository.InMemoryUserRepository;
import com.artansepet.state.CancelledState;
import com.artansepet.state.PaidState;
import com.artansepet.state.PickedUpState;
import com.artansepet.state.ReadyForPickupState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OrderFacadeTest {

    private static final String KOD = "123456";
    private final LocalDateTime simdi = LocalDateTime.of(2026, 11, 2, 20, 30);

    private final InMemoryUserRepository userRepo = new InMemoryUserRepository();
    private final InMemoryBagRepository bagRepo = new InMemoryBagRepository();
    private final InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();

    private PaytrGateway paytr;
    private OrderFacade facade;
    private Customer ayse;
    private Business firin;
    private SurpriseBag paket;

    @BeforeEach
    void hazirla() {
        DataStore.getInstance().clear();

        paytr = new PaytrGateway();
        // Testte teslim kodunu bilmek icin sabit kod ureten bir uretici
        PickupCodeGenerator sabitKod = new PickupCodeGenerator() {
            @Override
            public String generate() {
                return KOD;
            }
        };
        facade = new OrderFacade(userRepo, bagRepo, orderRepo, new PaytrAdapter(paytr), sabitKod);

        Location kadikoy = new Location(40.99, 29.03);
        ayse = new Customer("Ayse", "ayse@ornek.com", "gizli123", kadikoy);
        firin = new Business("Moda Firini", "firin@ornek.com", "gizli123",
                BusinessType.BAKERY, "Moda", kadikoy);
        paket = SurpriseBag.builder()
                .business(firin)
                .title("Aksam paketi")
                .originalPrice(new BigDecimal("100"))
                .quantity(2)
                .pickupWindow(LocalDateTime.of(2026, 11, 2, 20, 0),
                              LocalDateTime.of(2026, 11, 2, 22, 0))
                .build();

        userRepo.save(ayse);
        userRepo.save(firin);
        bagRepo.save(paket);
    }

    private Order siparisVer() {
        return facade.placeOrder(ayse.getId(), paket.getId(), simdi);
    }

    // ---------------- Siparis verme ----------------

    @Test
    void basariliSiparisOdenmisOlarakKaydedilir() {
        Order siparis = siparisVer();

        assertInstanceOf(PaidState.class, siparis.getState());
        assertEquals("PAYTR-" + siparis.getId(), siparis.getTransactionId());
        assertEquals(1, paket.getQuantity());
        assertTrue(orderRepo.findById(siparis.getId()).isPresent());
    }

    @Test
    void musteriyeTeslimKoduIleBildirimGider() {
        siparisVer();
        assertTrue(ayse.getNotifications().get(0).contains(KOD));
    }

    @Test
    void odemeBasarisizsaSiparisIptalEdilirVeStokGeriDoner() {
        paytr.setAvailable(false);

        assertThrows(PaymentFailedException.class, this::siparisVer);
        assertEquals(2, paket.getQuantity());
        Order iptal = orderRepo.findByCustomer(ayse).get(0);
        assertInstanceOf(CancelledState.class, iptal.getState());
    }

    @Test
    void olmayanMusteriVeyaPaketIcinHataFirlar() {
        assertThrows(NotFoundException.class,
                () -> facade.placeOrder("yok", paket.getId(), simdi));
        assertThrows(NotFoundException.class,
                () -> facade.placeOrder(ayse.getId(), "yok", simdi));
        // Isletme id'si ile siparis verilemez
        assertThrows(NotFoundException.class,
                () -> facade.placeOrder(firin.getId(), paket.getId(), simdi));
    }
    
    @Test
    void stokBitinceSiparisVerilemez() {
        siparisVer();
        siparisVer();
        assertThrows(BagSoldOutException.class, this::siparisVer);
    }

    // ---------------- Iptal ----------------

    @Test
    void odenmisSiparisIptalEdilinceIadeYapilir() {
        Order siparis = siparisVer();

        facade.cancelOrder(siparis.getId());

        assertInstanceOf(CancelledState.class, siparis.getState());
        assertTrue(paytr.getReversedTransactions().contains(siparis.getId()));
        assertEquals(2, paket.getQuantity());
    }

    @Test
    void hazirSiparisIptalEdilemezVeIadeYapilmaz() {
        Order siparis = siparisVer();
        facade.markReady(siparis.getId());

        assertThrows(InvalidStateTransitionException.class,
                () -> facade.cancelOrder(siparis.getId()));
        assertTrue(paytr.getReversedTransactions().isEmpty());
    }

    // ---------------- Teslim ----------------

    @Test
    void tumAkisSiparistenTeslimeKadarCalisir() {
        Order siparis = siparisVer();

        facade.markReady(siparis.getId());
        assertInstanceOf(ReadyForPickupState.class, siparis.getState());

        facade.confirmPickup(siparis.getId(), KOD);
        assertInstanceOf(PickedUpState.class, siparis.getState());
    }

    @Test
    void yanlisTeslimKoduReddedilir() {
        Order siparis = siparisVer();
        facade.markReady(siparis.getId());

        assertThrows(InvalidPickupCodeException.class,
                () -> facade.confirmPickup(siparis.getId(), "000000"));
        assertInstanceOf(ReadyForPickupState.class, siparis.getState());
    }
}