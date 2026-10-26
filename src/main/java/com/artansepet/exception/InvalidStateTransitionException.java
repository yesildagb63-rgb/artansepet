package com.artansepet.exception;


public class InvalidStateTransitionException extends ArtanSepetException {

    public InvalidStateTransitionException(String stateName, String action) {
        super("'" + stateName + "' durumundaki sipariste '" + action + "' islemi yapilamaz");
    }
}