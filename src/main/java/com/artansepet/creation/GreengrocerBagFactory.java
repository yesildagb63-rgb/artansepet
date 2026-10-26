package com.artansepet.creation;

import com.artansepet.model.BusinessType;
import com.artansepet.model.DietaryTag;
import com.artansepet.model.SurpriseBag;


public class GreengrocerBagFactory extends BagFactory {

    @Override
    protected void configure(SurpriseBag.Builder builder) {
        builder.title("Manav Surpriz Paketi")
               .tag(DietaryTag.VEGAN)
               .tag(DietaryTag.GLUTEN_FREE);
    }

    @Override
    protected BusinessType supportedType() {
        return BusinessType.GREENGROCER;
    }
}