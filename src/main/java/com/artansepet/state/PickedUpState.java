package com.artansepet.state;

/** Musteri paketi teslim aldi. Son durum: hicbir islem yapilamaz. */
public class PickedUpState extends AbstractOrderState {

    @Override
    public String displayName() {
        return "Teslim Alindi";
    }

    @Override
    public boolean isFinal() {
        return true;
    }
}