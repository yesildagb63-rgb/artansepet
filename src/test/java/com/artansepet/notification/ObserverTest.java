package com.artansepet.notification;

import com.artansepet.model.Business;
import com.artansepet.model.BusinessType;
import com.artansepet.model.Customer;
import com.artansepet.model.Location;
import com.artansepet.model.SurpriseBag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ObserverTest {

    private final Location kadikoy = new Location(40.99, 29.03);
    private Business firin;
    private Customer ayse;
    private Customer mehmet;

    @BeforeEach
    void hazirla() {
        firin = new Business("Moda Firini", "firin@ornek.com", "gizli123",
                BusinessType.BAKERY, "Moda", kadikoy);
        ayse = new Customer("Ayse", "ayse@ornek.com", "gizli123", kadikoy);
        mehmet = new Customer("Mehmet", "mehmet@ornek.com", "gizli123", kadikoy);
    }

    private SurpriseBag paket(Business isletme) {
        return SurpriseBag.builder()
                .business(isletme)
                .title("Aksam paketi")
                .originalPrice(new BigDecimal("100"))
                .pickupWindow(LocalDateTime.of(2026, 11, 2, 20, 0),
                              LocalDateTime.of(2026, 11, 2, 22, 0))
                .build();
    }

    @Test
    void takipciYeniPakettenHaberdarEdilir() {
        ayse.follow(firin);
        firin.notifyObservers(paket(firin));
        assertEquals(1, ayse.getNotifications().size());
    }

    @Test
    void takipEtmeyenBildirimAlmaz() {
        ayse.follow(firin);
        firin.notifyObservers(paket(firin));
        assertTrue(mehmet.getNotifications().isEmpty());
    }

    @Test
    void takibiBirakanArtikBildirimAlmaz() {
        ayse.follow(firin);
        ayse.unfollow(firin);
        firin.notifyObservers(paket(firin));
        assertTrue(ayse.getNotifications().isEmpty());
        assertEquals(0, firin.getFollowerCount());
    }

    @Test
    void ikiKezTakipEdenTekBildirimAlir() {
        ayse.follow(firin);
        ayse.follow(firin);
        firin.notifyObservers(paket(firin));
        assertEquals(1, firin.getFollowerCount());
        assertEquals(1, ayse.getNotifications().size());
    }

    @Test
    void tumTakipcilerHaberdarEdilir() {
        ayse.follow(firin);
        mehmet.follow(firin);
        firin.notifyObservers(paket(firin));
        assertEquals(1, ayse.getNotifications().size());
        assertEquals(1, mehmet.getNotifications().size());
    }

    @Test
    void bildirimMesajiIsletmeVePaketBilgisiIcerir() {
        ayse.follow(firin);
        firin.notifyObservers(paket(firin));
        String mesaj = ayse.getNotifications().get(0);
        assertTrue(mesaj.contains("Moda Firini"));
        assertTrue(mesaj.contains("Aksam paketi"));
    }

    @Test
    void isletmeBaskasininPaketiniDuyuramaz() {
        Business baskaFirin = new Business("Diger Firin", "diger@ornek.com", "gizli123",
                BusinessType.BAKERY, "Kadikoy", kadikoy);
        assertThrows(IllegalArgumentException.class,
                () -> firin.notifyObservers(paket(baskaFirin)));
    }

    @Test
    void musteriOlmayanGozlemcilerDeTakipEdebilir() {
        // Business, Customer'i degil BagObserver'i tanir.
        // Ornegin bir istatistik servisi de takipci olabilir (lambda ile).
        List<String> istatistik = new ArrayList<>();
        firin.subscribe(bag -> istatistik.add(bag.getTitle()));

        firin.notifyObservers(paket(firin));
        assertEquals(List.of("Aksam paketi"), istatistik);
    }
}