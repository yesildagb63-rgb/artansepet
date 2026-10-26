package com.artansepet.pricing;

import com.artansepet.model.SurpriseBag;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Her zaman ayni oranda indirim. Ornek: 0.30 = %30 indirim. */
public class FixedDiscountStrategy implements PricingStrategy {

    private final BigDecimal rate;

    public FixedDiscountStrategy(double rate) {
        if (rate < 0 || rate >= 1) {
            throw new IllegalArgumentException("Indirim orani 0 ile 1 arasinda olmali: " + rate);
        }
        this.rate = BigDecimal.valueOf(rate);
    }

    @Override
    public BigDecimal calculate(SurpriseBag bag, LocalDateTime now) {
        return bag.getOriginalPrice().multiply(BigDecimal.ONE.subtract(rate));
    }
}