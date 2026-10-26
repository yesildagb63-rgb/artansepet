package com.artansepet.payment;

import com.artansepet.exception.PaymentFailedException;
import com.artansepet.model.Customer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Adapter pattern: PayTR'nin kurus + durum kodu API'sini PaymentProcessor arayuzune uyarlar.
 */
public class PaytrAdapter implements PaymentProcessor {

    private static final String PREFIX = "PAYTR-";

    private final PaytrGateway gateway;

    public PaytrAdapter(PaytrGateway gateway) {
        this.gateway = Objects.requireNonNull(gateway);
    }

    @Override
    public PaymentResult charge(Customer customer, BigDecimal amount, String orderId) {
        Objects.requireNonNull(customer);
        Objects.requireNonNull(amount);
        Objects.requireNonNull(orderId);

        // TL -> kurus: 100.50 TL -> 10050
        long kurus = amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();

        int status = gateway.sendTransaction(orderId, kurus);

        // Durum kodu -> bizim sonuc nesnemiz
        return switch (status) {
            case PaytrGateway.STATUS_OK -> PaymentResult.success(PREFIX + orderId);
            case PaytrGateway.STATUS_DECLINED -> PaymentResult.failure("PayTR odemeyi reddetti");
            case PaytrGateway.STATUS_SERVICE_DOWN -> PaymentResult.failure("PayTR servisi kullanilamiyor");
            default -> PaymentResult.failure("Bilinmeyen PayTR kodu: " + status);
        };
    }

    @Override
    public void refund(String transactionId, BigDecimal amount) {
        // PayTR islemi bizim siparis numaramizla takip ediyor, on eki temizliyoruz
        String merchantOid = transactionId.startsWith(PREFIX)
                ? transactionId.substring(PREFIX.length())
                : transactionId;
        try {
            gateway.reverse(merchantOid);
        } catch (IllegalStateException e) {
            // PayTR'nin kendi hatasini bizim hata turumuze ceviriyoruz
            throw new PaymentFailedException("PayTR iadesi yapilamadi: " + e.getMessage());
        }
    }
}