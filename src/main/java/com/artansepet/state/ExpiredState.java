package com.artansepet.state;


public class ExpiredState extends AbstractOrderState {

    @Override
    public String displayName() {
        return "Suresi Doldu";
    }

    @Override
    public boolean isFinal() {
        return true;
    }
}