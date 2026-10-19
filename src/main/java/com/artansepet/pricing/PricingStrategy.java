package com.artansepet.pricing;

import com.artansepet.model.SurpriseBag;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@FunctionalInterface
public interface PricingStrategy {

    BigDecimal calculate(SurpriseBag bag, LocalDateTime now);

    /** Indirim uygulanmaz, orijinal fiyat doner. */
    static PricingStrategy noDiscount() {
        return (bag, now) -> bag.getOriginalPrice();
    }
}