package com.artansepet.exception;


public class InvalidPickupCodeException extends ArtanSepetException {

    public InvalidPickupCodeException() {
        super("Teslim kodu hatali");
    }
}