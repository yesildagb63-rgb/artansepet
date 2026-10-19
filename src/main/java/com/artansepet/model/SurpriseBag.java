package com.artansepet.model;

import com.artansepet.exception.BagSoldOutException;
import com.artansepet.pricing.PricingStrategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


public class SurpriseBag {

    private final String id;
    private final Business business;
    private final String title;
    private final BigDecimal originalPrice;
    private final int initialQuantity;
    private int quantity;                       // degisen tek alan: kalan stok
    private final LocalDateTime pickupStart;
    private final LocalDateTime pickupEnd;
    private final Set<DietaryTag> tags;
    private final PricingStrategy pricingStrategy;

    // private: disaridan "new SurpriseBag(...)" yazilamaz, sadece Builder cagirabilir
    private SurpriseBag(Builder b) {
        this.id = UUID.randomUUID().toString();
        this.business = b.business;
        this.title = b.title;
        this.originalPrice = b.originalPrice;
        this.initialQuantity = b.quantity;
        this.quantity = b.quantity;
        this.pickupStart = b.pickupStart;
        this.pickupEnd = b.pickupEnd;
        this.tags = Collections.unmodifiableSet(EnumSet.copyOf(b.tags));
        this.pricingStrategy = b.pricingStrategy;
    }

    public static Builder builder() {
        return new Builder();
    }

    // ---------------- Is mantigi ----------------

    /** Paketin verilen andaki satis fiyati (stratejiye gore). */
    public BigDecimal getCurrentPrice(LocalDateTime now) {
        return pricingStrategy.calculate(this, now).setScale(2, RoundingMode.HALF_UP);
    }

    
    public synchronized void reserveOne() {
        if (quantity == 0) {
            throw new BagSoldOutException(title);
        }
        quantity--;
    }

    /** Iptal veya sure dolmasinda bir adedi stoga geri birakir. */
    public synchronized void releaseOne() {
        if (quantity < initialQuantity) {
            quantity++;
        }
    }

    public synchronized boolean isAvailable() {
        return quantity > 0;
    }

    public synchronized int getQuantity() {
        return quantity;
    }

    /** Teslim saati gecti mi? */
    public boolean isPickupOver(LocalDateTime now) {
        return now.isAfter(pickupEnd);
    }

   
    public String getId() { return id; }
    public Business getBusiness() { return business; }
    public String getTitle() { return title; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public int getInitialQuantity() { return initialQuantity; }
    public LocalDateTime getPickupStart() { return pickupStart; }
    public LocalDateTime getPickupEnd() { return pickupEnd; }
    public Set<DietaryTag> getTags() { return tags; }

    @Override
    public String toString() {
        return "SurpriseBag{" + title + ", " + business.getName()
                + ", " + originalPrice + " TL, kalan=" + getQuantity() + "}";
    }

    
    public static class Builder {

        private Business business;
        private String title;
        private BigDecimal originalPrice;
        private int quantity = 1;
        private LocalDateTime pickupStart;
        private LocalDateTime pickupEnd;
        private final Set<DietaryTag> tags = EnumSet.noneOf(DietaryTag.class);
        private PricingStrategy pricingStrategy = PricingStrategy.noDiscount();

        private Builder() { }

        public Builder business(Business business) {
            this.business = business;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder originalPrice(BigDecimal price) {
            this.originalPrice = price;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder pickupWindow(LocalDateTime start, LocalDateTime end) {
            this.pickupStart = start;
            this.pickupEnd = end;
            return this;
        }

        public Builder tag(DietaryTag tag) {
            this.tags.add(Objects.requireNonNull(tag));
            return this;
        }

        public Builder pricing(PricingStrategy strategy) {
            this.pricingStrategy = Objects.requireNonNull(strategy);
            return this;
        }

        
        public SurpriseBag build() {
            Objects.requireNonNull(business, "Isletme belirtilmeli");
            if (title == null || title.isBlank()) {
                throw new IllegalArgumentException("Baslik bos olamaz");
            }
            if (originalPrice == null || originalPrice.signum() <= 0) {
                throw new IllegalArgumentException("Fiyat sifirdan buyuk olmali");
            }
            if (quantity < 1) {
                throw new IllegalArgumentException("Adet en az 1 olmali");
            }
            if (pickupStart == null || pickupEnd == null) {
                throw new IllegalArgumentException("Teslim saat araligi belirtilmeli");
            }
            if (!pickupStart.isBefore(pickupEnd)) {
                throw new IllegalArgumentException("Teslim baslangici bitisten once olmali");
            }
            return new SurpriseBag(this);
        }
    }
}