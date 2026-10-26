package com.artansepet.payment;

import com.artansepet.exception.PaymentFailedException;
import com.artansepet.model.Customer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;

/**
 * Adapter pattern: iyzico'nun Map tabanli API'sini PaymentProcessor arayuzune uyarlar.
 */
public class IyzicoAdapter implements PaymentProcessor {

    private final IyzicoClient client;

    public IyzicoAdapter(IyzicoClient client) {
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public PaymentResult charge(Customer customer, BigDecimal amount, String orderId) {
        Objects.requireNonNull(customer);
        Objects.requireNonNull(amount);
        Objects.requireNonNull(orderId);

        // Bizim dilimiz -> iyzico'nun dili
        Map<String, Object> request = Map.of(
                "price", amount.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                "currency", "TRY",
                "basketId", orderId,
                "buyerEmail", customer.getEmail());

        Map<String, Object> response = client.createPayment(request);

        // iyzico'nun dili -> bizim dilimiz
        if ("success".equals(response.get("status"))) {
            return PaymentResult.success((String) response.get("paymentId"));
        }
        return PaymentResult.failure(String.valueOf(response.get("errorMessage")));
    }

    @Override
    public void refund(String transactionId, BigDecimal amount) {
        // iyzico kismi iade desteklemiyor (sahte istemcide), tum tutar iptal edilir
        if (!client.cancelPayment(transactionId)) {
            throw new PaymentFailedException("iyzico iadesi yapilamadi: " + transactionId);
        }
    }
}