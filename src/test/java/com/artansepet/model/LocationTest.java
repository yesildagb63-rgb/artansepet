package com.artansepet.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocationTest {

    @Test
    void ayniNoktaArasindakiMesafeSifirdir() {
        Location kadikoy = new Location(40.9903, 29.0290);
        assertEquals(0.0, kadikoy.distanceTo(kadikoy), 0.001);
    }

    @Test
    void kadikoyIleTaksimArasiYaklasik6Km() {
        Location kadikoy = new Location(40.9903, 29.0290);
        Location taksim = new Location(41.0370, 28.9850);
        // Gercek kus ucusu mesafe ~6.4 km
        assertEquals(6.4, kadikoy.distanceTo(taksim), 0.5);
    }

    @Test
    void mesafeSimetriktir() {
        Location a = new Location(41.0, 29.0);
        Location b = new Location(39.9, 32.8);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0.0001);
    }

    @Test
    void gecersizEnlemHataFirlatir() {
        assertThrows(IllegalArgumentException.class, () -> new Location(91, 29));
    }

    @Test
    void gecersizBoylamHataFirlatir() {
        assertThrows(IllegalArgumentException.class, () -> new Location(41, 181));
    }
}