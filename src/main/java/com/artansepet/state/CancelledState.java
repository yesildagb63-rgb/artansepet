package com.artansepet.state;


public class CancelledState extends AbstractOrderState {

    @Override
    public String displayName() {
        return "Iptal";
    }

    @Override
    public boolean isFinal() {
        return true;
    }
}