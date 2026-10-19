package com.artansepet.model;

import java.util.Objects;


public class Business extends User {

    private final BusinessType type;
    private final String address;
    private final Location location;

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

    public BusinessType getType() { return type; }
    public String getAddress() { return address; }
    public Location getLocation() { return location; }
}