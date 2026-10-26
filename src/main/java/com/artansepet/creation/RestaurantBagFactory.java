package com.artansepet.creation;

import com.artansepet.model.BusinessType;
import com.artansepet.model.DietaryTag;
import com.artansepet.model.SurpriseBag;


public class RestaurantBagFactory extends BagFactory {

    @Override
    protected void configure(SurpriseBag.Builder builder) {
        builder.title("Restoran Surpriz Paketi")
               .tag(DietaryTag.HALAL);
    }

    @Override
    protected BusinessType supportedType() {
        return BusinessType.RESTAURANT;
    }
}