package com.artansepet.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class Customer extends User {

    private Location location;
    private final List<String> notifications = new ArrayList<>();

    public Customer(String name, String email, String rawPassword, Location location) {
        super(name, email, rawPassword);
        this.location = Objects.requireNonNull(location, "Konum bos olamaz");
    }

    public Location getLocation() {
        return location;
    }

    
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