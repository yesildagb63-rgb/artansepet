package com.artansepet.repository;

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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryTest {

    private final UserRepository userRepo = new InMemoryUserRepository();
    private final BagRepository bagRepo = new InMemoryBagRepository();
    private final Location kadikoy = new Location(40.99, 29.03);

    // Singleton tum testlerde ortak oldugu icin her testten once temizliyoruz
    @BeforeEach
    void temizle() {
        DataStore.getInstance().clear();
    }

    private Business firin(String email) {
        return new Business("Firin", email, "gizli123", BusinessType.BAKERY, "Moda", kadikoy);
    }

    private SurpriseBag paket(Business isletme, int adet) {
        return SurpriseBag.builder()
                .business(isletme)
                .title("Paket")
                .originalPrice(new BigDecimal("100"))
                .quantity(adet)
                .pickupWindow(LocalDateTime.of(2026, 11, 1, 20, 0),
                              LocalDateTime.of(2026, 11, 1, 22, 0))
                .build();
    }

    // ---------------- Singleton ----------------

    @Test
    void getInstanceHerZamanAyniNesneyiDondurur() {
        assertSame(DataStore.getInstance(), DataStore.getInstance());
    }

    @Test
    void farkliThreadlerdeDeTekNesneOlusur() {
        Set<DataStore> nesneler = ConcurrentHashMap.newKeySet();
        IntStream.range(0, 100).parallel()
                .forEach(i -> nesneler.add(DataStore.getInstance()));
        assertEquals(1, nesneler.size());
    }

    @Test
    void farkliRepositoryNesneleriAyniVeriyiGorur() {
        Customer ayse = new Customer("Ayse", "ayse@ornek.com", "gizli123", kadikoy);
        new InMemoryUserRepository().save(ayse);
        // Baska bir repository nesnesi ayni kullaniciyi bulabilmeli
        assertTrue(new InMemoryUserRepository().findById(ayse.getId()).isPresent());
    }

    @Test
    void clearTumVeriyiSiler() {
        userRepo.save(firin("a@b.com"));
        DataStore.getInstance().clear();
        assertTrue(userRepo.findAll().isEmpty());
    }

    // ---------------- UserRepository ----------------

    @Test
    void kaydedilenKullaniciIdIleBulunur() {
        Business isletme = firin("firin@ornek.com");
        userRepo.save(isletme);
        assertEquals(isletme, userRepo.findById(isletme.getId()).orElseThrow());
    }

    @Test
    void olmayanIdIcinBosDoner() {
        assertTrue(userRepo.findById("yok-boyle-bir-id").isEmpty());
    }

    @Test
    void epostaIleAramaBuyukKucukHarfeDuyarsizdir() {
        userRepo.save(firin("firin@ornek.com"));
        assertTrue(userRepo.findByEmail("FIRIN@Ornek.com").isPresent());
    }

    @Test
    void ayniEpostaIleIkinciHesapAcilamaz() {
        userRepo.save(firin("firin@ornek.com"));
        assertThrows(IllegalStateException.class,
                () -> userRepo.save(firin("firin@ornek.com")));
    }

    @Test
    void ayniKullaniciTekrarKaydedilebilir() {
        Business isletme = firin("firin@ornek.com");
        userRepo.save(isletme);
        assertDoesNotThrow(() -> userRepo.save(isletme));
        assertEquals(1, userRepo.findAll().size());
    }

    @Test
    void findAllListesiDisaridanDegistirilemez() {
        List<?> liste = userRepo.findAll();
        assertThrows(UnsupportedOperationException.class, () -> liste.clear());
    }

    // ---------------- BagRepository ----------------

    @Test
    void kaydedilenPaketIdIleBulunur() {
        SurpriseBag p = paket(firin("a@b.com"), 2);
        bagRepo.save(p);
        assertEquals(p, bagRepo.findById(p.getId()).orElseThrow());
    }

    @Test
    void tukenenPaketMusaitListesindeGorunmez() {
        Business isletme = firin("a@b.com");
        SurpriseBag dolu = paket(isletme, 2);
        SurpriseBag biten = paket(isletme, 1);
        biten.reserveOne();
        bagRepo.save(dolu);
        bagRepo.save(biten);

        List<SurpriseBag> musait = bagRepo.findAvailable();
        assertEquals(1, musait.size());
        assertTrue(musait.contains(dolu));
    }

    @Test
    void isletmeyeGorePaketlerFiltrelenir() {
        Business firin1 = firin("bir@ornek.com");
        Business firin2 = firin("iki@ornek.com");
        bagRepo.save(paket(firin1, 1));
        bagRepo.save(paket(firin1, 1));
        bagRepo.save(paket(firin2, 1));

        assertEquals(2, bagRepo.findByBusiness(firin1).size());
        assertEquals(1, bagRepo.findByBusiness(firin2).size());
    }
}