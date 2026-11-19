package com.artansepet.exception;

/**
 * Istenen kayit (kullanici, paket, siparis) bulunamazsa firlatilir.
 */
public class NotFoundException extends ArtanSepetException {

    public NotFoundException(String what, String id) {
        super(what + " bulunamadi: " + id);
    }
}