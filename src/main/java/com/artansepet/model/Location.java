package com.artansepet.model;


public record Location(double latitude, double longitude) {

    private static final double EARTH_RADIUS_KM = 6371.0;

    
    public Location {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Enlem -90 ile 90 arasinda olmali: " + latitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Boylam -180 ile 180 arasinda olmali: " + longitude);
        }
    }

    
    public double distanceTo(Location other) {
        double dLat = Math.toRadians(other.latitude - this.latitude);
        double dLon = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(this.latitude))
                 * Math.cos(Math.toRadians(other.latitude))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return 2 * EARTH_RADIUS_KM * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}