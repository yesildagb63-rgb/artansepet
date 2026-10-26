package com.artansepet.model;

import com.artansepet.notification.BagObserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Paket satin alan musteri.
 * Observer pattern'da gozlemci (observer): takip ettigi isletmelerden bildirim alir.
 */
public class Customer extends User implements BagObserver {

    private Location location;
    private final List<String> notifications = Collections.synchronizedList(new ArrayList<>());

    public Customer(String name, String email, String rawPassword, Location location) {
        super(name, email, rawPassword);
        this.location = Objects.requireNonNull(location, "Konum bos olamaz");
    }

    // ---------------- Takip ----------------

    /** Okunakli kullanim icin: musteri.follow(firin) */
    public void follow(Business business) {
        business.subscribe(this);
    }

    public void unfollow(Business business) {
        business.unsubscribe(this);
    }

    // ---------------- Observer ----------------

    @Override
    public void onNewBag(SurpriseBag bag) {
        addNotification(bag.getBusiness().getName() + " yeni paket ekledi: "
                + bag.getTitle() + " (" + bag.getOriginalPrice() + " TL)");
    }

    // ---------------- Konum ve bildirimler ----------------

    public Location getLocation() {
        return location;
    }

    // Musteri baska bir yere tasinabilir, bu yuzden konum degistirilebilir
    public void updateLocation(Location newLocation) {
        this.location = Objects.requireNonNull(newLocation, "Konum bos olamaz");
    }

    public void addNotification(String message) {
        notifications.add(message);
    }

    // Disariya salt-okunur liste veriyoruz, kimse listeyi disaridan bozamasin
    public List<String> getNotifications() {
        return Collections.unmodifiableList(notifications);
    }
}