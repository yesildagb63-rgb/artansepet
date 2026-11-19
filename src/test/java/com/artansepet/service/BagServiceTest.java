package com.artansepet.service;

import com.artansepet.model.Business;
import com.artansepet.model.BusinessType;
import com.artansepet.model.Customer;
import com.artansepet.model.DietaryTag;
import com.artansepet.model.Location;
import com.artansepet.model.SurpriseBag;
import com.artansepet.pricing.FixedDiscountStrategy;
import com.artansepet.pricing.PricingStrategy;
import com.artansepet.pricing.SortByDistance;
import com.artansepet.repository.DataStore;
import com.artansepet.repository.InMemoryBagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BagServiceTest {

    private final LocalDateTime simdi = LocalDateTime.of(2026, 11, 2, 20, 0);
    private final LocalDateTime bitis = LocalDateTime.of(2026, 11, 2, 22, 0);

    private BagService servis;
    private Customer ayse;          // Kadikoy
    private Business firin;         // Moda, yakin
    private Business manav;         // Taksim, uzak

    @BeforeEach
    void hazirla() {
        DataStore.getInstance().clear();
        servis = new BagService(new InMemoryBagRepository());
        ayse = new Customer("Ayse", "ayse@ornek.com", "gizli123", new Location(40.9903, 29.0290));
        firin = new Business("Moda Firini", "firin@ornek.com", "gizli123",
                BusinessType.BAKERY, "Moda", new Location(40.9840, 29.0260));
        manav = new Business("Taksim Manavi", "manav@ornek.com", "gizli123",
                BusinessType.GREENGROCER, "Taksim", new Location(41.0370, 28.9850));
    }

    private SurpriseBag yayinla(Business isletme, int adet) {
        return servis.publishBag(isletme, new BigDecimal("100"), adet, simdi, bitis,
                PricingStrategy.noDiscount());
    }

    @Test
    void yayinlananPaketTuruneUygunFabrikaIleOlusturulur() {
        SurpriseBag paket = yayinla(firin, 3);
        assertEquals("Firin Surpriz Paketi", paket.getTitle());
        assertTrue(paket.getTags().contains(DietaryTag.VEGETARIAN));
        assertEquals(List.of(paket), servis.listBusinessBags(firin));
    }

    @Test
    void yayinlananPaketTakipcilereBildirilir() {
        ayse.follow(firin);
        yayinla(firin, 3);
        assertEquals(1, ayse.getNotifications().size());
        assertTrue(ayse.getNotifications().get(0).contains("Moda Firini"));
    }

    @Test
    void secilenIndirimStratejisiPaketeUygulanir() {
        SurpriseBag paket = servis.publishBag(firin, new BigDecimal("100"), 3, simdi, bitis,
                new FixedDiscountStrategy(0.40));
        assertEquals(new BigDecimal("60.00"), paket.getCurrentPrice(simdi));
    }

    @Test
    void aramaSonuclariMesafeyeGoreSiralanir() {
        SurpriseBag uzak = yayinla(manav, 2);
        SurpriseBag yakin = yayinla(firin, 2);
        assertEquals(List.of(yakin, uzak), servis.search(ayse, new SortByDistance(), simdi));
    }

    @Test
    void tukenenVeSuresiGecenPaketlerAramadaGorunmez() {
        SurpriseBag tukenen = yayinla(firin, 1);
        tukenen.reserveOne();
        yayinla(manav, 2);

        assertEquals(1, servis.search(ayse, new SortByDistance(), simdi).size());
        assertTrue(servis.search(ayse, new SortByDistance(), bitis.plusMinutes(1)).isEmpty());
    }
}