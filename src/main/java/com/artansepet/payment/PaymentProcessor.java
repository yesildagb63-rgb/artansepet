package com.artansepet.payment;

import com.artansepet.model.Customer;

import java.math.BigDecimal;

/**
 * Adapter pattern: uygulamanin bekledigi (hedef) arayuz.
 * Projenin geri kalani sadece bunu bilir; iyzico veya PayTR'yi hic bilmez.
 */
public interface PaymentProcessor {

    PaymentResult charge(Customer customer, BigDecimal amount, String orderId);

    void refund(String transactionId, BigDecimal amount);
}