package com.artansepet.web;

import com.sun.net.httpserver.HttpExchange;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Oturumlar: giriste rastgele bir token uretilir, site her istekte
 * "Authorization: Bearer <token>" basligiyla gonderir.
 */
final class Sessions {

    private final Map<String, String> tokens = new ConcurrentHashMap<>();

    String create(String userId) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, userId);
        return token;
    }

    Optional<String> userId(HttpExchange ex) {
        return token(ex).map(tokens::get);
    }

    void remove(HttpExchange ex) {
        token(ex).ifPresent(tokens::remove);
    }

    private Optional<String> token(HttpExchange ex) {
        String header = ex.getRequestHeaders().getFirst("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return Optional.of(header.substring("Bearer ".length()).trim());
        }
        return Optional.empty();
    }
}