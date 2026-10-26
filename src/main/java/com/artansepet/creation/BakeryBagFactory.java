package com.artansepet.creation;

import com.artansepet.model.BusinessType;
import com.artansepet.model.DietaryTag;
import com.artansepet.model.SurpriseBag;


public class BakeryBagFactory extends BagFactory {

    @Override
    protected void configure(SurpriseBag.Builder builder) {
        builder.title("Firin Surpriz Paketi")
               .tag(DietaryTag.VEGETARIAN);
    }

    @Override
    protected BusinessType supportedType() {
        return BusinessType.BAKERY;
    }
}