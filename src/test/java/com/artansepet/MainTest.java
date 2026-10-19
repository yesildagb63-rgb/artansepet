package com.artansepet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void mesajDogruDonmeli() {
        assertEquals("Artan Sepet çalışıyor", Main.message());
    }
}