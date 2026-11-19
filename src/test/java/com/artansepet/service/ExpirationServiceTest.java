package com.artansepet.service;

import com.artansepet.model.Business;
import com.artansepet.model.BusinessType;
import com.artansepet.model.Customer;
import com.artansepet.model.Location;
import com.artansepet.model.Order;
import com.artansepet.model.SurpriseBag;
import com.artansepet.repository.DataStore;
import com.artansepet.repository.InMemoryOrderRepository;
import com.artansepet.state.ExpiredState;
import com.artansepet.state.PickedUpState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ExpirationServiceTest {

    private final LocalDateTime saat2030 = LocalDateTime.of(2026, 11, 2, 20, 30);
    private final LocalDateTime teslimBitis = LocalDateTime.of(2026, 11, 2, 22, 0);

    private InMemoryOrderRepository orderRepo;
    private ExpirationService servis;
    private Customer ayse;
    private SurpriseBag paket;

    @BeforeEach
    void hazirla() {
        DataStore.getInstance().clear();
        orderRepo = new InMemoryOrderRepository();
        servis = new ExpirationService(orderRepo);

        Location kadikoy = new Location(40.99, 29.03);
        ayse = new Customer("Ayse", "ayse@ornek.com", "gizli123", kadikoy);
        Business firin = new Business("Firin", "firin@ornek.com", "gizli123",
                BusinessType.BAKERY, "Moda", kadikoy);
        paket = SurpriseBag.builder()
                .business(firin)
                .title("Aksam paketi")
                .originalPrice(new BigDecimal("100"))
                .quantity(3)
                .pickupWindow(LocalDateTime.of(2026, 11, 2, 20, 0), teslimBitis)
                .build();
    }

    private Order hazirSiparis() {
        Order siparis = Order.create(ayse, paket, "123456", saat2030);
        siparis.markPaid("TX-1");
        siparis.markReady();
        orderRepo.save(siparis);
        return siparis;
    }

    @Test
    void teslimSaatiGecenSiparisinSuresiDolar() {
        Order siparis = hazirSiparis();

        int adet = servis.expireOverdueOrders(teslimBitis.plusMinutes(1));

        assertEquals(1, adet);
        assertInstanceOf(ExpiredState.class, siparis.getState());
        assertEquals(3, paket.getQuantity());                  // stok geri dondu
        assertTrue(ayse.getNotifications().get(0).contains("suresi doldu"));
    }

    @Test
    void teslimSaatiGecmedenHicbirSeyDegismez() {
        hazirSiparis();
        assertEquals(0, servis.expireOverdueOrders(teslimBitis.minusMinutes(1)));
    }

    @Test
    void teslimAlinmisSiparisEtkilenmez() {
        Order alinan = hazirSiparis();
        alinan.pickUp("123456");

        assertEquals(0, servis.expireOverdueOrders(teslimBitis.plusHours(1)));
        assertInstanceOf(PickedUpState.class, alinan.getState());
    }

    @Test
    void ikinciKezCalistirmakAyniSiparisiTekrarIslemez() {
        hazirSiparis();
        servis.expireOverdueOrders(teslimBitis.plusMinutes(1));
        assertEquals(0, servis.expireOverdueOrders(teslimBitis.plusMinutes(2)));
    }
}