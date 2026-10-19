package com.artansepet.exception;


public class BagSoldOutException extends ArtanSepetException {

    public BagSoldOutException(String bagTitle) {
        super("Paket tukendi: " + bagTitle);
    }
}