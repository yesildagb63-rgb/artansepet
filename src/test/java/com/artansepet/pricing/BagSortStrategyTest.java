package com.artansepet.pricing;

import com.artansepet.model.Business;
import com.artansepet.model.BusinessType;
import com.artansepet.model.Customer;
import com.artansepet.model.Location;
import com.artansepet.model.SurpriseBag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BagSortStrategyTest {

    private final LocalDateTime simdi = LocalDateTime.of(2026, 11, 2, 20, 0);
    private final Customer musteri = new Customer("Ayse", "ayse@ornek.com", "gizli123",
            new Location(40.9903, 29.0290));                       // Kadikoy

    private SurpriseBag yakin;   // Moda, ~0.7 km, 150 TL ama %50 indirimli -> 75 TL, 22:00
    private SurpriseBag orta;    // Uskudar, ~4 km, 80 TL, 21:00
    private SurpriseBag uzak;    // Taksim, ~6.4 km, 60 TL, 23:00
    private List<SurpriseBag> liste;

    private SurpriseBag paket(String isim, Location konum, String fiyat,
                              int bitisSaati, PricingStrategy strateji) {
        Business isletme = new Business(isim, isim.toLowerCase() + "@ornek.com", "gizli123",
                BusinessType.BAKERY, isim, konum);
        return SurpriseBag.builder()
                .business(isletme)
                .title(isim + " paketi")
                .originalPrice(new BigDecimal(fiyat))
                .pickupWindow(simdi, simdi.withHour(bitisSaati))
                .pricing(strateji)
                .build();
    }

    @BeforeEach
    void hazirla() {
        yakin = paket("Moda", new Location(40.9840, 29.0260), "150", 22,
                new FixedDiscountStrategy(0.50));
        orta = paket("Uskudar", new Location(41.0260, 29.0150), "80", 21,
                PricingStrategy.noDiscount());
        uzak = paket("Taksim", new Location(41.0370, 28.9850), "60", 23,
                PricingStrategy.noDiscount());
        liste = List.of(orta, uzak, yakin);                        // karisik sira
    }

    @Test
    void fiyataGoreSiralamaGuncelFiyataBakar() {
        List<SurpriseBag> sonuc = new SortByPrice().sort(liste, musteri, simdi);
        // Moda'nin orijinal fiyati en yuksek (150) ama indirimle 75 TL, Uskudar'dan (80) once gelir
        assertEquals(List.of(uzak, yakin, orta), sonuc);
    }

    @Test
    void mesafeyeGoreSiralamaEnYakiniOneAlir() {
        List<SurpriseBag> sonuc = new SortByDistance().sort(liste, musteri, simdi);
        assertEquals(List.of(yakin, orta, uzak), sonuc);
    }

    @Test
    void teslimSaatineGoreSiralamaEnErkenBiteniOneAlir() {
        List<SurpriseBag> sonuc = new SortByPickupTime().sort(liste, musteri, simdi);
        assertEquals(List.of(orta, yakin, uzak), sonuc);
    }

    @Test
    void siralamaOrijinalListeyiDegistirmez() {
        new SortByPrice().sort(liste, musteri, simdi);
        assertEquals(List.of(orta, uzak, yakin), liste);
    }

    @Test
    void bosListeBosDoner() {
        assertTrue(new SortByDistance().sort(List.of(), musteri, simdi).isEmpty());
    }

    @Test
    void ayniListeFarkliStratejilerleFarkliSiralanir() {
        BagSortStrategy[] stratejiler = { new SortByPrice(), new SortByDistance(), new SortByPickupTime() };
        SurpriseBag[] beklenenIlk = { uzak, yakin, orta };
        for (int i = 0; i < stratejiler.length; i++) {
            assertEquals(beklenenIlk[i], stratejiler[i].sort(liste, musteri, simdi).get(0));
        }
    }
}