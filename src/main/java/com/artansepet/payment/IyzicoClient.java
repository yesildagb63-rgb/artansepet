package com.artansepet.payment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SAHTE iyzico istemcisi (gercek API'ye baglanmaz).
 * Gercek iyzico gibi: istek Map olarak gelir, fiyat METIN ("100.00"),
 * cevap yine Map olarak doner. Bizim PaymentProcessor arayuzumuze benzemez.
 */
public class IyzicoClient {

    private boolean available = true;
    private final List<String> cancelledPayments = new ArrayList<>();

    public Map<String, Object> createPayment(Map<String, Object> request) {
        if (!available) {
            return Map.of("status", "failure", "errorMessage", "iyzico servisi kullanilamiyor");
        }
        Object price = request.get("price");
        if (!(price instanceof String p) || new BigDecimal(p).signum() <= 0) {
            return Map.of("status", "failure", "errorMessage", "Gecersiz tutar");
        }
        String paymentId = "IYZ-" + UUID.randomUUID().toString().substring(0, 8);
        return Map.of("status", "success", "paymentId", paymentId);
    }

    /** iyzico'da iptal her zaman tum tutar icindir. */
    public boolean cancelPayment(String paymentId) {
        if (!available) {
            return false;
        }
        cancelledPayments.add(paymentId);
        return true;
    }

    // ---- Testler icin yardimci metotlar ----

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public List<String> getCancelledPayments() {
        return List.copyOf(cancelledPayments);
    }
}