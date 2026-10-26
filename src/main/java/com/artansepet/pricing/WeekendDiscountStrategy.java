package com.artansepet.pricing;

import com.artansepet.model.SurpriseBag;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;

/** Sadece cumartesi ve pazar indirim uygular, hafta ici orijinal fiyat. */
public class WeekendDiscountStrategy implements PricingStrategy {

    private final BigDecimal weekendRate;

    public WeekendDiscountStrategy(double weekendRate) {
        if (weekendRate < 0 || weekendRate >= 1) {
            throw new IllegalArgumentException("Indirim orani 0 ile 1 arasinda olmali: " + weekendRate);
        }
        this.weekendRate = BigDecimal.valueOf(weekendRate);
    }

    @Override
    public BigDecimal calculate(SurpriseBag bag, LocalDateTime now) {
        DayOfWeek gun = now.getDayOfWeek();
        boolean haftaSonu = gun == DayOfWeek.SATURDAY || gun == DayOfWeek.SUNDAY;
        if (!haftaSonu) {
            return bag.getOriginalPrice();
        }
        return bag.getOriginalPrice().multiply(BigDecimal.ONE.subtract(weekendRate));
    }
}