package com.artansepet.payment;

/**
 * Bir odeme isleminin sonucu. Tum odeme sistemleri bu ortak formata cevrilir.
 */
public record PaymentResult(boolean success, String transactionId, String message) {

    public static PaymentResult success(String transactionId) {
        return new PaymentResult(true, transactionId, "Odeme basarili");
    }

    public static PaymentResult failure(String message) {
        return new PaymentResult(false, null, message);
    }
}