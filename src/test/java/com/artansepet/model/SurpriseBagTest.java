package com.artansepet.model;

import com.artansepet.exception.BagSoldOutException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SurpriseBagTest {

    private final Business firin = new Business("Moda Firini", "firin@ornek.com", "gizli123",
            BusinessType.BAKERY, "Moda Cad. No:5", new Location(40.9840, 29.0260));

    private final LocalDateTime baslangic = LocalDateTime.of(2026, 11, 1, 20, 0);
    private final LocalDateTime bitis = LocalDateTime.of(2026, 11, 1, 22, 0);

    // Tum zorunlu alanlari dolu, hazir bir Builder
    private SurpriseBag.Builder gecerliBuilder() {
        return SurpriseBag.builder()
                .business(firin)
                .title("Aksam paketi")
                .originalPrice(new BigDecimal("150"))
                .quantity(3)
                .pickupWindow(baslangic, bitis);
    }

    // ---------------- Builder ----------------

    @Test
    void builderTumAlanlarlaPaketOlusturur() {
        SurpriseBag paket = gecerliBuilder()
                .tag(DietaryTag.VEGAN)
                .tag(DietaryTag.GLUTEN_FREE)
                .build();

        assertEquals("Aksam paketi", paket.getTitle());
        assertEquals(firin, paket.getBusiness());
        assertEquals(3, paket.getQuantity());
        assertTrue(paket.getTags().contains(DietaryTag.VEGAN));
        assertEquals(2, paket.getTags().size());
    }

    @Test
    void opsiyonelAlanlarVerilmezseVarsayilanlarKullanilir() {
        SurpriseBag paket = SurpriseBag.builder()
                .business(firin)
                .title("Basit paket")
                .originalPrice(new BigDecimal("100"))
                .pickupWindow(baslangic, bitis)
                .build();

        assertEquals(1, paket.getQuantity());                 // varsayilan adet
        assertTrue(paket.getTags().isEmpty());                // etiket yok
        assertEquals(new BigDecimal("100.00"),                // indirimsiz fiyat
                paket.getCurrentPrice(baslangic));
    }

    @Test
    void ayniEtiketIkiKezEklenirseTekSayilir() {
        SurpriseBag paket = gecerliBuilder()
                .tag(DietaryTag.VEGAN)
                .tag(DietaryTag.VEGAN)
                .build();
        assertEquals(1, paket.getTags().size());
    }

    @Test
    void isletmesizPaketOlusmaz() {
        assertThrows(NullPointerException.class,
                () -> gecerliBuilder().business(null).build());
    }

    @Test
    void bosBaslikliPaketOlusmaz() {
        assertThrows(IllegalArgumentException.class,
                () -> gecerliBuilder().title("  ").build());
    }

    @Test
    void sifirFiyatliPaketOlusmaz() {
        assertThrows(IllegalArgumentException.class,
                () -> gecerliBuilder().originalPrice(BigDecimal.ZERO).build());
    }

    @Test
    void sifirAdetliPaketOlusmaz() {
        assertThrows(IllegalArgumentException.class,
                () -> gecerliBuilder().quantity(0).build());
    }

    @Test
    void teslimBitisiBaslangictanOnceOlamaz() {
        assertThrows(IllegalArgumentException.class,
                () -> gecerliBuilder().pickupWindow(bitis, baslangic).build());
    }

    @Test
    void etiketListesiDisaridanDegistirilemez() {
        SurpriseBag paket = gecerliBuilder().build();
        assertThrows(UnsupportedOperationException.class,
                () -> paket.getTags().add(DietaryTag.HALAL));
    }

    // ---------------- Fiyat ----------------

    @Test
    void verilenStratejiyeGoreFiyatHesaplanir() {
        // Gecici bir "yari fiyat" stratejisi (lambda ile)
        SurpriseBag paket = gecerliBuilder()
                .pricing((bag, now) -> bag.getOriginalPrice().multiply(new BigDecimal("0.5")))
                .build();
        assertEquals(new BigDecimal("75.00"), paket.getCurrentPrice(baslangic));
    }

    // ---------------- Stok ----------------

    @Test
    void rezervasyonStoguAzaltir() {
        SurpriseBag paket = gecerliBuilder().build();
        paket.reserveOne();
        assertEquals(2, paket.getQuantity());
    }

    @Test
    void stokBitinceRezervasyonHataFirlatir() {
        SurpriseBag paket = gecerliBuilder().quantity(1).build();
        paket.reserveOne();
        assertFalse(paket.isAvailable());
        assertThrows(BagSoldOutException.class, paket::reserveOne);
    }

    @Test
    void iptalStoguGeriBirakir() {
        SurpriseBag paket = gecerliBuilder().build();
        paket.reserveOne();
        paket.releaseOne();
        assertEquals(3, paket.getQuantity());
    }

    @Test
    void stokBaslangicAdedininUstuneCikamaz() {
        SurpriseBag paket = gecerliBuilder().build();
        paket.releaseOne();   // hic rezervasyon yokken geri birakma
        assertEquals(3, paket.getQuantity());
    }

    @Test
    void teslimSaatiGecinceIsPickupOverTrueDoner() {
        SurpriseBag paket = gecerliBuilder().build();
        assertFalse(paket.isPickupOver(bitis.minusMinutes(1)));
        assertTrue(paket.isPickupOver(bitis.plusMinutes(1)));
    }

    // ---------------- Eszamanlilik ----------------

    @Test
    void aynıAndaYuzMusteriOnPaketiKapisirsaSadeceOnuBasarili() throws InterruptedException {
        SurpriseBag paket = gecerliBuilder().quantity(10).build();
        int musteriSayisi = 100;

        ExecutorService havuz = Executors.newFixedThreadPool(20);
        CountDownLatch baslaSinyali = new CountDownLatch(1);
        CountDownLatch bitti = new CountDownLatch(musteriSayisi);
        AtomicInteger basarili = new AtomicInteger();
        AtomicInteger tukendi = new AtomicInteger();

        for (int i = 0; i < musteriSayisi; i++) {
            havuz.submit(() -> {
                try {
                    baslaSinyali.await();          // herkes ayni anda baslasin
                    paket.reserveOne();
                    basarili.incrementAndGet();
                } catch (BagSoldOutException e) {
                    tukendi.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    bitti.countDown();
                }
            });
        }

        baslaSinyali.countDown();                  // "basla!"
        assertTrue(bitti.await(5, TimeUnit.SECONDS));
        havuz.shutdown();

        assertEquals(10, basarili.get());
        assertEquals(90, tukendi.get());
        assertEquals(0, paket.getQuantity());
    }
}