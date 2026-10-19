package com.artansepet.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;


public abstract class User {

    private final String id;
    private final String name;
    private final String email;
    private final String passwordHash;

    protected User(String name, String email, String rawPassword) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Isim bos olamaz");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Gecersiz e-posta: " + email);
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("Sifre en az 6 karakter olmali");
        }
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.email = email.toLowerCase();
        this.passwordHash = hash(rawPassword);
    }

    public boolean checkPassword(String rawPassword) {
        return rawPassword != null && passwordHash.equals(hash(rawPassword));
    }

    private static String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 bulunamadi", e);
        }
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{name='" + name + "', email='" + email + "'}";
    }
}