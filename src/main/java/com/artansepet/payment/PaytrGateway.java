package com.artansepet.payment;

import java.util.ArrayList;
import java.util.List;

/**
 * SAHTE PayTR gecidi (gercek API'ye baglanmaz).
 * Gercek PayTR gibi: tutar KURUS cinsinden long, sonuc bir DURUM KODU.
 * Islem, bizim verdigimiz siparis numarasi (merchantOid) ile takip edilir.
 */
public class PaytrGateway {

    public static final int STATUS_OK = 100;
    public static final int STATUS_DECLINED = 200;
    public static final int STATUS_SERVICE_DOWN = 500;

    private boolean available = true;
    private final List<String> reversedTransactions = new ArrayList<>();

    public int sendTransaction(String merchantOid, long amountInKurus) {
        if (!available) {
            return STATUS_SERVICE_DOWN;
        }
        if (merchantOid == null || amountInKurus <= 0) {
            return STATUS_DECLINED;
        }
        return STATUS_OK;
    }

    public void reverse(String merchantOid) {
        if (!available) {
            throw new IllegalStateException("PayTR servisine ulasilamiyor");
        }
        reversedTransactions.add(merchantOid);
    }

    // ---- Testler icin yardimci metotlar ----

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public List<String> getReversedTransactions() {
        return List.copyOf(reversedTransactions);
    }
}