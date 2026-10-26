package com.artansepet.creation;

import com.artansepet.model.Business;
import com.artansepet.model.BusinessType;
import com.artansepet.model.SurpriseBag;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Factory Method pattern.
 * Paket olusturmanin ortak adimlarini tanimlar; isletme turune ozel
 * ayarlari alt siniflara (configure metoduna) birakir.
 */
public abstract class BagFactory {

    /**
     * Ortak akis: her fabrika bu adimlari ayni sirayla izler.
     * final: alt siniflar bu akisi degistiremez, sadece configure'u doldurur.
     */
    public final SurpriseBag createBag(Business business, BigDecimal price, int quantity,
                                       LocalDateTime pickupStart, LocalDateTime pickupEnd) {
        Objects.requireNonNull(business, "Isletme belirtilmeli");
        if (business.getType() != supportedType()) {
            throw new IllegalArgumentException(getClass().getSimpleName()
                    + " sadece " + supportedType() + " icin paket olusturur, verilen: "
                    + business.getType());
        }

        SurpriseBag.Builder builder = SurpriseBag.builder()
                .business(business)
                .originalPrice(price)
                .quantity(quantity)
                .pickupWindow(pickupStart, pickupEnd);

        configure(builder);          // <-- alt sinifin doldurdugu kisim
        return builder.build();
    }

    /** Factory Method: her isletme turu kendi varsayilan ayarlarini burada verir. */
    protected abstract void configure(SurpriseBag.Builder builder);

    /** Bu fabrikanin hangi isletme turu icin calistigi. */
    protected abstract BusinessType supportedType();

    /**
     * Isletme turune gore dogru fabrikayi dondurur.
     * Arayuz ve servis kodu hangi alt sinifin kullanildigini bilmek zorunda kalmaz.
     */
    public static BagFactory forType(BusinessType type) {
        return switch (Objects.requireNonNull(type)) {
            case BAKERY -> new BakeryBagFactory();
            case PASTRY -> new PastryBagFactory();
            case CAFE -> new CafeBagFactory();
            case RESTAURANT -> new RestaurantBagFactory();
            case MARKET -> new MarketBagFactory();
            case GREENGROCER -> new GreengrocerBagFactory();
            case HOTEL -> new HotelBagFactory();
        };
    }
}