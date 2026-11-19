package com.artansepet.ui;

import com.artansepet.model.Business;
import com.artansepet.model.BusinessType;
import com.artansepet.model.Customer;
import com.artansepet.model.Location;
import com.artansepet.pricing.FixedDiscountStrategy;
import com.artansepet.pricing.LastHourDiscountStrategy;
import com.artansepet.pricing.PricingStrategy;
import com.artansepet.pricing.WeekendDiscountStrategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Sunum icin hazir veri. Tum sifreler: 123456
 * Teslim saatleri "simdi"ye gore ayarlanir, uygulama ne zaman acilirsa acilsin paketler gorunur.
 */
public final class DemoData {

    private DemoData() { }

    public static void load(AppContext ctx, LocalDateTime now) {
        Location kadikoy = new Location(40.9903, 29.0290);

        Business firin = new Business("Moda Firini", "firin@artansepet.com", "123456",
                BusinessType.BAKERY, "Moda Cad. No:5 Kadikoy", new Location(40.9840, 29.0260));
        Business pastane = new Business("Bahariye Pastanesi", "pastane@artansepet.com", "123456",
                BusinessType.PASTRY, "Bahariye Cad. No:12 Kadikoy", new Location(40.9890, 29.0310));
        Business restoran = new Business("Uskudar Lokantasi", "restoran@artansepet.com", "123456",
                BusinessType.RESTAURANT, "Hakimiyeti Milliye Cad. Uskudar", new Location(41.0260, 29.0150));
        Business manav = new Business("Besiktas Manavi", "manav@artansepet.com", "123456",
                BusinessType.GREENGROCER, "Carsi Besiktas", new Location(41.0430, 29.0050));

        Customer ayse = new Customer("Ayse Yilmaz", "ayse@ornek.com", "123456", kadikoy);
        Customer mehmet = new Customer("Mehmet Kaya", "mehmet@ornek.com", "123456",
                new Location(41.0370, 28.9850));

        for (var u : new com.artansepet.model.User[]{firin, pastane, restoran, manav, ayse, mehmet}) {
            ctx.users.save(u);
        }

        // Ayse fırını takip ediyor: paketler yayınlanınca bildirim alacak (Observer)
        ayse.follow(firin);

        LocalDateTime bitis = now.plusHours(3);
        ctx.bagService.publishBag(firin, new BigDecimal("150"), 5, now, bitis,
                new FixedDiscountStrategy(0.50));
        ctx.bagService.publishBag(pastane, new BigDecimal("200"), 3, now, now.plusHours(1),
                new LastHourDiscountStrategy(0.30, 0.20));
        ctx.bagService.publishBag(restoran, new BigDecimal("180"), 4, now, bitis,
                new WeekendDiscountStrategy(0.40));
        ctx.bagService.publishBag(manav, new BigDecimal("90"), 6, now, bitis,
                PricingStrategy.noDiscount());
    }
}