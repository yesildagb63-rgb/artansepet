package com.artansepet.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessTest {

    private final Location moda = new Location(40.9840, 29.0260);

    private Business ornekFirin() {
        return new Business("Moda Firini", "firin@ornek.com", "gizli123",
                BusinessType.BAKERY, "Moda Cad. No:5 Kadikoy", moda);
    }

    @Test
    void isletmeBilgileriDogruSaklanir() {
        Business firin = ornekFirin();
        assertEquals("Moda Firini", firin.getName());
        assertEquals(BusinessType.BAKERY, firin.getType());
        assertEquals("Moda Cad. No:5 Kadikoy", firin.getAddress());
        assertEquals(moda, firin.getLocation());
    }

    @Test
    void isletmeDeUserKurallariniMirasAlir() {
        assertTrue(ornekFirin().checkPassword("gizli123"));
    }

    @Test
    void bosAdresHataFirlatir() {
        assertThrows(IllegalArgumentException.class,
                () -> new Business("Firin", "a@b.com", "gizli123",
                        BusinessType.BAKERY, "", moda));
    }

    @Test
    void turBosOlamaz() {
        assertThrows(NullPointerException.class,
                () -> new Business("Firin", "a@b.com", "gizli123",
                        null, "Adres", moda));
    }

    @Test
    void konumBosOlamaz() {
        assertThrows(NullPointerException.class,
                () -> new Business("Firin", "a@b.com", "gizli123",
                        BusinessType.BAKERY, "Adres", null));
    }

    @Test
    void ayniKisiKendisineEsittir() {
        Business firin = ornekFirin();
        assertEquals(firin, firin);
    }

    @Test
    void ayniBilgilerleOlusturulanIkiIsletmeFarklidir() {
        // Ayni isim/e-posta olsa bile id'leri farkli, yani farkli kayitlar
        assertNotEquals(ornekFirin(), ornekFirin());
    }

    @Test
    void musteriVeIsletmeMesafesiHesaplanabilir() {
        Customer musteri = new Customer("Ali", "ali@ornek.com", "gizli123",
                new Location(40.9903, 29.0290));
        double km = musteri.getLocation().distanceTo(ornekFirin().getLocation());
        assertTrue(km < 1.5, "Kadikoy ile Moda arasi 1.5 km'den az olmali, bulunan: " + km);
    }
}