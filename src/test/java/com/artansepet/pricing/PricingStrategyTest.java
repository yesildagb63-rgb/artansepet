package com.artansepet.pricing;

import com.artansepet.model.Business;
import com.artansepet.model.BusinessType;
import com.artansepet.model.Location;
import com.artansepet.model.SurpriseBag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PricingStrategyTest {

    // 31 Ekim 2026 cumartesi, 2 Kasim 2026 pazartesi
    private final LocalDateTime cumartesi2030 = LocalDateTime.of(2026, 10, 31, 20, 30);
    private final LocalDateTime pazartesi2030 = LocalDateTime.of(2026, 11, 2, 20, 30);

    private final Business firin = new Business("Firin", "firin@ornek.com", "gizli123",
            BusinessType.BAKERY, "Moda", new Location(40.99, 29.03));

    /** 100 TL'lik, 20:00-22:00 arasi teslim edilen paket. */
    private SurpriseBag paket(LocalDateTime gun, PricingStrategy strateji) {
        return SurpriseBag.builder()
                .business(firin)
                .title("Paket")
                .originalPrice(new BigDecimal("100"))
                .pickupWindow(gun.withHour(20).withMinute(0), gun.withHour(22).withMinute(0))
                .pricing(strateji)
                .build();
    }

    @Test
    void indirimsizStratejiOrijinalFiyatiVerir() {
        SurpriseBag p = paket(pazartesi2030, PricingStrategy.noDiscount());
        assertEquals(new BigDecimal("100.00"), p.getCurrentPrice(pazartesi2030));
    }

    @Test
    void sabitIndirimHerZamanAyniOraniUygular() {
        SurpriseBag p = paket(pazartesi2030, new FixedDiscountStrategy(0.30));
        assertEquals(new BigDecimal("70.00"), p.getCurrentPrice(pazartesi2030));
        assertEquals(new BigDecimal("70.00"), p.getCurrentPrice(pazartesi2030.plusHours(1)));
    }

    @Test
    void gecersizIndirimOraniReddedilir() {
        assertThrows(IllegalArgumentException.class, () -> new FixedDiscountStrategy(1.0));
        assertThrows(IllegalArgumentException.class, () -> new FixedDiscountStrategy(-0.1));
        assertThrows(IllegalArgumentException.class, () -> new LastHourDiscountStrategy(0.6, 0.5));
    }

    @Test
    void sonSaattenOnceTemelIndirimUygulanir() {
        SurpriseBag p = paket(pazartesi2030, new LastHourDiscountStrategy(0.20, 0.30));
        assertEquals(new BigDecimal("80.00"), p.getCurrentPrice(pazartesi2030));    // 20:30
    }

    @Test
    void sonSaatteEkIndirimUygulanir() {
        SurpriseBag p = paket(pazartesi2030, new LastHourDiscountStrategy(0.20, 0.30));
        LocalDateTime saat21 = pazartesi2030.withHour(21).withMinute(0);
        assertEquals(new BigDecimal("50.00"), p.getCurrentPrice(saat21));            // tam sinir
        assertEquals(new BigDecimal("50.00"), p.getCurrentPrice(saat21.plusMinutes(30)));
    }

    @Test
    void haftaSonuIndirimiCumartesiUygulanir() {
        SurpriseBag p = paket(cumartesi2030, new WeekendDiscountStrategy(0.25));
        assertEquals(new BigDecimal("75.00"), p.getCurrentPrice(cumartesi2030));
    }

    @Test
    void haftaSonuIndirimiHaftaIciUygulanmaz() {
        SurpriseBag p = paket(pazartesi2030, new WeekendDiscountStrategy(0.25));
        assertEquals(new BigDecimal("100.00"), p.getCurrentPrice(pazartesi2030));
    }

    @Test
    void ayniPaketFarkliStratejilerleFarkliFiyatAlir() {
        // Strategy'nin ozu: SurpriseBag ayni, sadece strateji degisiyor
        assertEquals(new BigDecimal("70.00"),
                paket(pazartesi2030, new FixedDiscountStrategy(0.30)).getCurrentPrice(pazartesi2030));
        assertEquals(new BigDecimal("80.00"),
                paket(pazartesi2030, new LastHourDiscountStrategy(0.20, 0.30)).getCurrentPrice(pazartesi2030));
        assertEquals(new BigDecimal("100.00"),
                paket(pazartesi2030, new WeekendDiscountStrategy(0.25)).getCurrentPrice(pazartesi2030));
    }
}