package com.artansepet.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    private final Location kadikoy = new Location(40.9903, 29.0290);

    private Customer ornekMusteri() {
        return new Customer("Ayse Yilmaz", "Ayse@Ornek.com", "gizli123", kadikoy);
    }

    

    @Test
    void dogruSifreKabulEdilir() {
        assertTrue(ornekMusteri().checkPassword("gizli123"));
    }

    @Test
    void yanlisSifreReddedilir() {
        assertFalse(ornekMusteri().checkPassword("yanlis"));
    }

    @Test
    void epostaKucukHarfeCevrilir() {
        assertEquals("ayse@ornek.com", ornekMusteri().getEmail());
    }

    @Test
    void herKullaniciyaFarkliIdVerilir() {
        assertNotEquals(ornekMusteri().getId(), ornekMusteri().getId());
    }

    @Test
    void bosIsimHataFirlatir() {
        assertThrows(IllegalArgumentException.class,
                () -> new Customer(" ", "a@b.com", "gizli123", kadikoy));
    }

    @Test
    void gecersizEpostaHataFirlatir() {
        assertThrows(IllegalArgumentException.class,
                () -> new Customer("Ayse", "epostasiz", "gizli123", kadikoy));
    }

    @Test
    void kisaSifreHataFirlatir() {
        assertThrows(IllegalArgumentException.class,
                () -> new Customer("Ayse", "a@b.com", "123", kadikoy));
    }

    @Test
    void sifreToStringIcindeGorunmez() {
        assertFalse(ornekMusteri().toString().contains("gizli123"));
    }

    
    @Test
    void konumGuncellenebilir() {
        Customer musteri = ornekMusteri();
        Location taksim = new Location(41.0370, 28.9850);
        musteri.updateLocation(taksim);
        assertEquals(taksim, musteri.getLocation());
    }

    @Test
    void bildirimEklenir() {
        Customer musteri = ornekMusteri();
        musteri.addNotification("Yeni paket var!");
        assertEquals(1, musteri.getNotifications().size());
    }

    @Test
    void bildirimListesiDisaridanDegistirilemez() {
        Customer musteri = ornekMusteri();
        assertThrows(UnsupportedOperationException.class,
                () -> musteri.getNotifications().add("hack"));
    }
}