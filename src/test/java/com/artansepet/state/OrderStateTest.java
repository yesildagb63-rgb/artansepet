package com.artansepet.state;

import com.artansepet.exception.BagSoldOutException;
import com.artansepet.exception.InvalidPickupCodeException;
import com.artansepet.exception.InvalidStateTransitionException;
import com.artansepet.model.Business;
import com.artansepet.model.BusinessType;
import com.artansepet.model.Customer;
import com.artansepet.model.Location;
import com.artansepet.model.Order;
import com.artansepet.model.SurpriseBag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OrderStateTest {

    private static final String KOD = "482913";

    private final LocalDateTime saat2030 = LocalDateTime.of(2026, 11, 1, 20, 30);
    private final LocalDateTime teslimBitis = LocalDateTime.of(2026, 11, 1, 22, 0);

    private Customer ayse;
    private SurpriseBag paket;

    @BeforeEach
    void hazirla() {
        Location kadikoy = new Location(40.99, 29.03);
        Business firin = new Business("Firin", "firin@ornek.com", "gizli123",
                BusinessType.BAKERY, "Moda", kadikoy);
        ayse = new Customer("Ayse", "ayse@ornek.com", "gizli123", kadikoy);
        paket = SurpriseBag.builder()
                .business(firin)
                .title("Aksam paketi")
                .originalPrice(new BigDecimal("100"))
                .quantity(2)
                .pickupWindow(LocalDateTime.of(2026, 11, 1, 20, 0), teslimBitis)
                .build();
    }

    private Order yeniSiparis() {
        return Order.create(ayse, paket, KOD, saat2030);
    }

    // ---------------- Olusturma ----------------

    @Test
    void yeniSiparisRezerveDurumundaBaslarVeStokDuser() {
        Order siparis = yeniSiparis();
        assertInstanceOf(ReservedState.class, siparis.getState());
        assertEquals("Rezerve", siparis.getStateName());
        assertEquals(1, paket.getQuantity());
    }

    @Test
    void stokYoksaSiparisOlusmaz() {
        yeniSiparis();
        yeniSiparis();                              // 2 adet bitti
        assertThrows(BagSoldOutException.class, this::yeniSiparis);
    }

    @Test
    void teslimSaatiGecmisPaketeSiparisVerilemez() {
        assertThrows(IllegalStateException.class,
                () -> Order.create(ayse, paket, KOD, teslimBitis.plusMinutes(1)));
        assertEquals(2, paket.getQuantity());        // stok dusmemis olmali
    }

    @Test
    void fiyatRezervasyonAnindaSabitlenir() {
        SurpriseBag indirimliPaket = SurpriseBag.builder()
                .business(paket.getBusiness())
                .title("Indirimli")
                .originalPrice(new BigDecimal("100"))
                .pickupWindow(LocalDateTime.of(2026, 11, 1, 20, 0), teslimBitis)
                // 21:00'den sonra yari fiyat
                .pricing((bag, now) -> now.getHour() >= 21
                        ? bag.getOriginalPrice().divide(new BigDecimal("2"))
                        : bag.getOriginalPrice())
                .build();

        Order siparis = Order.create(ayse, indirimliPaket, KOD, saat2030);
        assertEquals(new BigDecimal("100.00"), siparis.getPrice());
        // Fiyat sonradan dusse bile siparisin fiyati degismez
        assertEquals(new BigDecimal("50.00"), indirimliPaket.getCurrentPrice(saat2030.plusHours(1)));
        assertEquals(new BigDecimal("100.00"), siparis.getPrice());
    }

    // ---------------- Mutlu yol ----------------

    @Test
    void tumAkisRezervedenTeslimAlindiyaKadarCalisir() {
        Order siparis = yeniSiparis();

        siparis.markPaid("TX-1");
        assertInstanceOf(PaidState.class, siparis.getState());
        assertTrue(siparis.isPaid());
        assertEquals("TX-1", siparis.getTransactionId());

        siparis.markReady();
        assertInstanceOf(ReadyForPickupState.class, siparis.getState());

        siparis.pickUp(KOD);
        assertInstanceOf(PickedUpState.class, siparis.getState());
        assertTrue(siparis.getState().isFinal());
    }
    
    // ---------------- Iptal ----------------

    @Test
    void rezerveSiparisIptalEdilinceStokGeriDoner() {
        Order siparis = yeniSiparis();
        siparis.cancel();
        assertInstanceOf(CancelledState.class, siparis.getState());
        assertEquals(2, paket.getQuantity());
    }

    @Test
    void odenmisSiparisIptalEdilebilirVeStokGeriDoner() {
        Order siparis = yeniSiparis();
        siparis.markPaid("TX-1");
        siparis.cancel();
        assertInstanceOf(CancelledState.class, siparis.getState());
        assertEquals(2, paket.getQuantity());
        assertTrue(siparis.isPaid());               // Facade bunu gorup iade yapacak
    }

    @Test
    void teslimeHazirSiparisIptalEdilemez() {
        Order siparis = yeniSiparis();
        siparis.markPaid("TX-1");
        siparis.markReady();
        assertThrows(InvalidStateTransitionException.class, siparis::cancel);
        assertInstanceOf(ReadyForPickupState.class, siparis.getState());
    }

    @Test
    void teslimAlinmisSiparisIptalEdilemez() {
        Order siparis = yeniSiparis();
        siparis.markPaid("TX-1");
        siparis.markReady();
        siparis.pickUp(KOD);
        InvalidStateTransitionException hata =
                assertThrows(InvalidStateTransitionException.class, siparis::cancel);
        assertTrue(hata.getMessage().contains("Teslim Alindi"));
    }

    // ---------------- Gecersiz gecisler ----------------

    @Test
    void odenmedenHazirIsaretlenemez() {
        Order siparis = yeniSiparis();
        assertThrows(InvalidStateTransitionException.class, siparis::markReady);
    }

    @Test
    void hazirOlmadanTeslimAlinamaz() {
        Order siparis = yeniSiparis();
        siparis.markPaid("TX-1");
        assertThrows(InvalidStateTransitionException.class, () -> siparis.pickUp(KOD));
    }

    @Test
    void iptalEdilmisSipariseOdemeYapilamazVeIslemNumarasiYazilmaz() {
        Order siparis = yeniSiparis();
        siparis.cancel();
        assertThrows(InvalidStateTransitionException.class, () -> siparis.markPaid("TX-1"));
        assertNull(siparis.getTransactionId());
        assertFalse(siparis.isPaid());
    }

    @Test
    void sonDurumdakiSiparisteHicbirIslemYapilamaz() {
        Order siparis = yeniSiparis();
        siparis.cancel();
        assertThrows(InvalidStateTransitionException.class, () -> siparis.markPaid("TX"));
        assertThrows(InvalidStateTransitionException.class, siparis::markReady);
        assertThrows(InvalidStateTransitionException.class, () -> siparis.pickUp(KOD));
        assertThrows(InvalidStateTransitionException.class, siparis::cancel);
        assertThrows(InvalidStateTransitionException.class, siparis::expire);
    }

    // ---------------- Teslim kodu ----------------

    @Test
    void yanlisKodlaTeslimAlinamazAmaDurumBozulmaz() {
        Order siparis = yeniSiparis();
        siparis.markPaid("TX-1");
        siparis.markReady();

        assertThrows(InvalidPickupCodeException.class, () -> siparis.pickUp("000000"));
        assertInstanceOf(ReadyForPickupState.class, siparis.getState());

        siparis.pickUp(KOD);                        // dogru kodla tekrar deneyince olur
        assertInstanceOf(PickedUpState.class, siparis.getState());
    }

    // ---------------- Sure dolumu ----------------

    @Test
    void hazirSiparisinSuresiDoluncaStokGeriDoner() {
        Order siparis = yeniSiparis();
        siparis.markPaid("TX-1");
        siparis.markReady();
        siparis.expire();
        assertInstanceOf(ExpiredState.class, siparis.getState());
        assertEquals(2, paket.getQuantity());
    }

    @Test
    void acikSiparisTeslimSaatiGecinceGecikmisSayilir() {
        Order siparis = yeniSiparis();
                assertFalse(siparis.isOverdue(teslimBitis.minusMinutes(1)));
        assertTrue(siparis.isOverdue(teslimBitis.plusMinutes(1)));
    }

    @Test
    void kapanmisSiparisGecikmisSayilmaz() {
        Order siparis = yeniSiparis();
        siparis.cancel();
        assertFalse(siparis.isOverdue(teslimBitis.plusHours(1)));
    }
}