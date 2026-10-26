package com.artansepet.model;

import com.artansepet.notification.BagObserver;
import com.artansepet.notification.BagSubject;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Surpriz paket satan isletme (firin, pastane, kafe, restoran, market, manav, otel).
 * Observer pattern'da gozlenen (subject): yeni paket ekleyince takipcilerine haber verir.
 */
public class Business extends User implements BagSubject {

    private final BusinessType type;
    private final String address;
    private final Location location;
    private final Set<BagObserver> followers = ConcurrentHashMap.newKeySet();

    public Business(String name, String email, String rawPassword,
                    BusinessType type, String address, Location location) {
        super(name, email, rawPassword);
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Adres bos olamaz");
        }
        this.type = Objects.requireNonNull(type, "Isletme turu bos olamaz");
        this.address = address;
        this.location = Objects.requireNonNull(location, "Konum bos olamaz");
    }

    // ---------------- Observer (subject) ----------------

    @Override
    public void subscribe(BagObserver observer) {
        followers.add(Objects.requireNonNull(observer));
    }

    @Override
    public void unsubscribe(BagObserver observer) {
        followers.remove(observer);
    }

    @Override
    public void notifyObservers(SurpriseBag bag) {
        if (bag.getBusiness() != this) {
            throw new IllegalArgumentException("Isletme sadece kendi paketini duyurabilir");
        }
        for (BagObserver observer : followers) {
            observer.onNewBag(bag);
        }
    }

    public int getFollowerCount() {
        return followers.size();
    }

    // ---------------- Getter'lar ----------------

    public BusinessType getType() { return type; }
    public String getAddress() { return address; }
    public Location getLocation() { return location; }
}