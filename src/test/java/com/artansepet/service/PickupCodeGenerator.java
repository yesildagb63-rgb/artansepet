package com.artansepet.service;

import java.security.SecureRandom;

/**
 * 6 haneli teslim kodu uretir (ornek: "048213").
 */
public class PickupCodeGenerator {

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        return String.format("%06d", random.nextInt(1_000_000));
    }
}