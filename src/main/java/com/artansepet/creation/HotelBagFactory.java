package com.artansepet.creation;

import com.artansepet.model.BusinessType;
import com.artansepet.model.SurpriseBag;


public class HotelBagFactory extends BagFactory {

    @Override
    protected void configure(SurpriseBag.Builder builder) {
        builder.title("Otel Bufe Paketi");
    }

    @Override
    protected BusinessType supportedType() {
        return BusinessType.HOTEL;
    }
}