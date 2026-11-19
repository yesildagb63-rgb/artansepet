package com.artansepet.web;

import com.artansepet.model.Business;
import com.artansepet.model.Customer;
import com.artansepet.model.Location;
import com.artansepet.model.Order;
import com.artansepet.model.SurpriseBag;
import com.artansepet.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * API'nin aldigi ve gonderdigi veriler.
 * Model nesneleri disari dogrudan verilmez: sifre ozeti gibi alanlar asla JSON'a girmez.
 */
public final class Dtos {

    private static final DateTimeFormatter SAAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter TARIH = DateTimeFormatter.ofPattern("dd.MM HH:mm");

    private Dtos() { }

    // ---------------- Gelen istekler ----------------

    public record LoginRequest(String email, String password) { }

    public record RegisterRequest(String name, String email, String password, String district) { }

    public record OrderRequest(String bagId) { }

    public record PickupRequest(String code) { }

    /** discount: none | fixed | lastHour | weekend. rate ve extraRate yuzde olarak (30 = %30). */
    public record PublishRequest(BigDecimal price, int quantity, int hours,
                                 String discount, int rate, int extraRate) { }

    // ---------------- Giden cevaplar ----------------

    public record UserDto(String id, String name, String email, String role, Integer followerCount) {
        static UserDto of(User u) {
            if (u instanceof Business b) {
                return new UserDto(b.getId(), b.getName(), b.getEmail(), "business", b.getFollowerCount());
            }
            return new UserDto(u.getId(), u.getName(), u.getEmail(), "customer", null);
        }
    }

    public record AuthResponse(String token, UserDto user) { }

    public record BagDto(String id, String title, String businessId, String businessName,
                         String businessType, String address, BigDecimal currentPrice,
                         BigDecimal originalPrice, int quantity, int initialQuantity,
                         String pickupStart, String pickupEnd, List<String> tags,
                         Double distanceKm, boolean expired) {
        /** from: musterinin konumu (mesafe icin), isletme icin null. */
        static BagDto of(SurpriseBag b, LocalDateTime now, Location from) {
            Double km = from == null ? null
                    : Math.round(from.distanceTo(b.getBusiness().getLocation()) * 10) / 10.0;
            return new BagDto(b.getId(), b.getTitle(), b.getBusiness().getId(), b.getBusiness().getName(),
                    b.getBusiness().getType().name(), b.getBusiness().getAddress(),
                    b.getCurrentPrice(now), b.getOriginalPrice(), b.getQuantity(), b.getInitialQuantity(),
                    b.getPickupStart().format(SAAT), b.getPickupEnd().format(SAAT),
                    b.getTags().stream().map(Enum::name).toList(), km, b.isPickupOver(now));
        }
    }

    /** pickupCode sadece musteriye ve sadece acik sipariste gonderilir. */
    public record OrderDto(String id, String bagTitle, String businessName, String customerName,
                           BigDecimal price, String state, boolean open, String pickupCode,
                           String createdAt, String pickupEnd) {
        static OrderDto forCustomer(Order o) {
            boolean acik = !o.getState().isFinal();
            return of(o, acik ? o.getPickupCode() : null);
        }

        static OrderDto forBusiness(Order o) {
            return of(o, null);
        }

        private static OrderDto of(Order o, String code) {
            return new OrderDto(o.getId(), o.getBag().getTitle(), o.getBag().getBusiness().getName(),
                    o.getCustomer().getName(), o.getPrice(), o.getStateName(), !o.getState().isFinal(),
                    code, o.getCreatedAt().format(TARIH), o.getBag().getPickupEnd().format(SAAT));
        }
    }

    public record BusinessDto(String id, String name, String type, String address,
                              boolean followed, int followerCount) {
        static BusinessDto of(Business b, Customer viewer) {
            return new BusinessDto(b.getId(), b.getName(), b.getType().name(), b.getAddress(),
                    viewer != null && b.isFollowedBy(viewer), b.getFollowerCount());
        }
    }
}