package com.artansepet.exception;


public class PaymentFailedException extends ArtanSepetException {

    public PaymentFailedException(String reason) {
        super("Odeme basarisiz: " + reason);
    }
}