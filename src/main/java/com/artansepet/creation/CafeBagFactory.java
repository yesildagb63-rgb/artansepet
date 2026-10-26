package com.artansepet.creation;

import com.artansepet.model.BusinessType;
import com.artansepet.model.SurpriseBag;

/** Kafe paketleri: sandvic, kek; icerik karisik oldugu icin sabit etiket yok. */
public class CafeBagFactory extends BagFactory {

    @Override
    protected void configure(SurpriseBag.Builder builder) {
        builder.title("Kafe Surpriz Paketi");
    }

    @Override
    protected BusinessType supportedType() {
        return BusinessType.CAFE;
    }
}