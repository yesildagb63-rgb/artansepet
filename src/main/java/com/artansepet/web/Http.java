package com.artansepet.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** JSON istek/cevap yardimcilari. */
final class Http {

    static final ObjectMapper JSON = new ObjectMapper();

    private Http() { }

    /** Istek govdesindeki JSON'u verilen tipe cevirir. */
    static <T> T body(HttpExchange ex, Class<T> type) throws IOException {
        return JSON.readValue(ex.getRequestBody(), type);
    }

    /** Nesneyi JSON olarak gonderir. */
    static void json(HttpExchange ex, int status, Object data) throws IOException {
        byte[] bytes = JSON.writeValueAsBytes(data);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    /** Hata mesajini {"error": "..."} olarak gonderir. */
    static void error(HttpExchange ex, int status, String message) throws IOException {
        json(ex, status, Map.of("error", message == null ? "Bilinmeyen hata" : message));
    }

    /** Adresteki ?sort=price gibi parametreleri okur. */
    static Map<String, String> query(HttpExchange ex) {
        Map<String, String> m = new HashMap<>();
        String q = ex.getRequestURI().getRawQuery();
        if (q == null) {
            return m;
        }
        for (String part : q.split("&")) {
            int i = part.indexOf('=');
            if (i > 0) {
                m.put(URLDecoder.decode(part.substring(0, i), StandardCharsets.UTF_8),
                      URLDecoder.decode(part.substring(i + 1), StandardCharsets.UTF_8));
            }
        }
        return m;
    }
}