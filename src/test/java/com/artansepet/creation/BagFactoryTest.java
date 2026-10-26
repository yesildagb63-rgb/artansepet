package com.artansepet.creation;

import com.artansepet.model.Business;
import com.artansepet.model.BusinessType;
import com.artansepet.model.DietaryTag;
import com.artansepet.model.Location;
import com.artansepet.model.SurpriseBag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BagFactoryTest {

    private final LocalDateTime baslangic = LocalDateTime.of(2026, 11, 1, 20, 0);
    private final LocalDateTime bitis = LocalDateTime.of(2026, 11, 1, 22, 0);
    private final BigDecimal fiyat = new BigDecimal("120");

    private Business isletme(BusinessType tur) {
        return new Business("Ornek " + tur, tur.name().toLowerCase() + "@ornek.com", "gizli123",
                tur, "Kadikoy", new Location(40.99, 29.03));
    }

    private SurpriseBag paketOlustur(BusinessType tur) {
        return BagFactory.forType(tur).createBag(isletme(tur), fiyat, 4, baslangic, bitis);
    }

    @Test
    void herIsletmeTuruIcinFabrikaVardir() {
        // Enum'a yeni tur eklenip fabrikasi unutulursa bu test (ve derleyici) yakalar
        for (BusinessType tur : BusinessType.values()) {
            SurpriseBag paket = paketOlustur(tur);
            assertEquals(tur, paket.getBusiness().getType(), "Tur: " + tur);
            assertFalse(paket.getTitle().isBlank(), "Baslik bos: " + tur);
        }
    }

    @Test
    void forTypeDogruFabrikayiDondurur() {
        assertInstanceOf(BakeryBagFactory.class, BagFactory.forType(BusinessType.BAKERY));
        assertInstanceOf(GreengrocerBagFactory.class, BagFactory.forType(BusinessType.GREENGROCER));
        assertInstanceOf(HotelBagFactory.class, BagFactory.forType(BusinessType.HOTEL));
    }

    @Test
    void ortakAlanlarTumFabrikalardaAyniSekildeAktarilir() {
        SurpriseBag paket = paketOlustur(BusinessType.CAFE);
        assertEquals(fiyat, paket.getOriginalPrice());
        assertEquals(4, paket.getQuantity());
        assertEquals(baslangic, paket.getPickupStart());
        assertEquals(bitis, paket.getPickupEnd());
    }

    @Test
    void firinPaketiVejetaryendir() {
        SurpriseBag paket = paketOlustur(BusinessType.BAKERY);
        assertEquals("Firin Surpriz Paketi", paket.getTitle());
        assertTrue(paket.getTags().contains(DietaryTag.VEGETARIAN));
    }

    @Test
    void manavPaketiVeganVeGlutensizdir() {
        SurpriseBag paket = paketOlustur(BusinessType.GREENGROCER);
        assertTrue(paket.getTags().contains(DietaryTag.VEGAN));
        assertTrue(paket.getTags().contains(DietaryTag.GLUTEN_FREE));
    }

    @Test
    void marketPaketindeSabitEtiketYoktur() {
        assertTrue(paketOlustur(BusinessType.MARKET).getTags().isEmpty());
    }

    @Test
    void yanlisTurdeIsletmeVerilirseHataFirlatir() {
        BagFactory firinFabrikasi = new BakeryBagFactory();
        Business restoran = isletme(BusinessType.RESTAURANT);
        assertThrows(IllegalArgumentException.class,
                () -> firinFabrikasi.createBag(restoran, fiyat, 4, baslangic, bitis));
    }

    @Test
    void builderKurallariFabrikadaDaGecerlidir() {
        // Fabrika Builder'i kullandigi icin adet 0 yine reddedilir
        assertThrows(IllegalArgumentException.class,
                () -> BagFactory.forType(BusinessType.BAKERY)
                        .createBag(isletme(BusinessType.BAKERY), fiyat, 0, baslangic, bitis));
    }

    @Test
    void nullTurIcinHataFirlatir() {
        assertThrows(NullPointerException.class, () -> BagFactory.forType(null));
    }
}