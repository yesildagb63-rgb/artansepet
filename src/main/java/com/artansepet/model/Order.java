package com.artansepet.model;

import com.artansepet.state.OrderState;
import com.artansepet.state.ReservedState;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Order {

    private final String id;
    private final Customer customer;
    private final SurpriseBag bag;
    private final BigDecimal price;          // rezervasyon anindaki fiyat
    private final String pickupCode;
    private final LocalDateTime createdAt;
    private String transactionId;            // odeme yapilinca dolar
    private OrderState state;

    private Order(Customer customer, SurpriseBag bag, String pickupCode, LocalDateTime now) {
        this.id = UUID.randomUUID().toString();
        this.customer = customer;
        this.bag = bag;
        this.price = bag.getCurrentPrice(now);
        this.pickupCode = pickupCode;
        this.createdAt = now;
        this.state = new ReservedState();    // her siparis "Rezerve" olarak baslar
    }

    
    public static Order create(Customer customer, SurpriseBag bag,
                               String pickupCode, LocalDateTime now) {
        Objects.requireNonNull(customer, "Musteri belirtilmeli");
        Objects.requireNonNull(bag, "Paket belirtilmeli");
        Objects.requireNonNull(now, "Zaman belirtilmeli");
        if (pickupCode == null || pickupCode.isBlank()) {
            throw new IllegalArgumentException("Teslim kodu bos olamaz");
        }
        if (bag.isPickupOver(now)) {
            throw new IllegalStateException("Teslim saati gecmis pakete siparis verilemez");
        }
        bag.reserveOne();                    // once stok ayrilir (synchronized)
        return new Order(customer, bag, pickupCode, now);
    }

    // ---------------- Durum gecisleri (isi state'e devreder) ----------------

    public void markPaid(String transactionId) {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("Islem numarasi bos olamaz");
        }
        state = state.markPaid(this);        // izin yoksa burada hata firlar
        this.transactionId = transactionId;  // sadece gecis basariliysa kaydedilir
    }

    public void markReady() {
        state = state.markReady(this);
    }

    public void pickUp(String code) {
        state = state.pickUp(this, code);
    }

    public void cancel() {
        state = state.cancel(this);
    }

    public void expire() {
        state = state.expire(this);
    }

    // ---------------- Sorgular ----------------

    /** Teslim saati gecmis ve hala acik mi? (Sure dolumu servisi icin) */
    public boolean isOverdue(LocalDateTime now) {
        return !state.isFinal() && bag.isPickupOver(now);
    }

    public boolean isPaid() {
        return transactionId != null;
    }

    public String getStateName() {
        return state.displayName();
    }

    public OrderState getState() { return state; }
    public String getId() { return id; }
    public Customer getCustomer() { return customer; }
    public SurpriseBag getBag() { return bag; }
    public BigDecimal getPrice() { return price; }
    public String getPickupCode() { return pickupCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getTransactionId() { return transactionId; }

    @Override
    public String toString() {
        return "Order{" + bag.getTitle() + ", " + customer.getName()
                + ", " + price + " TL, " + getStateName() + "}";
    }
}