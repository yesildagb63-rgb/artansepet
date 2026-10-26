package com.artansepet.pricing;

import com.artansepet.model.SurpriseBag;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Normalde temel indirim; teslim bitisine son 1 saat kala ek indirim.
 * Amac: kapanisa yakin kalan paketleri hizla eritmek.
 */
public class LastHourDiscountStrategy implements PricingStrategy {

    private final BigDecimal baseRate;
    private final BigDecimal lastHourRate;

    public LastHourDiscountStrategy(double baseRate, double extraRate) {
        if (baseRate < 0 || extraRate < 0 || baseRate + extraRate >= 1) {
            throw new IllegalArgumentException("Oranlar gecersiz: " + baseRate + " + " + extraRate);
        }
        this.baseRate = BigDecimal.valueOf(baseRate);
        this.lastHourRate = BigDecimal.valueOf(baseRate + extraRate);
    }

    @Override
    public BigDecimal calculate(SurpriseBag bag, LocalDateTime now) {
        boolean sonSaat = !now.isBefore(bag.getPickupEnd().minusHours(1));
        BigDecimal rate = sonSaat ? lastHourRate : baseRate;
        return bag.getOriginalPrice().multiply(BigDecimal.ONE.subtract(rate));
    }
}