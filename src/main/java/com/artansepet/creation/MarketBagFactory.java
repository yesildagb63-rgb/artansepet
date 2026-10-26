package com.artansepet.creation;

import com.artansepet.model.BusinessType;
import com.artansepet.model.SurpriseBag;

/** Market paketleri: karisik urunler, sabit bir etiket verilmez. */
public class MarketBagFactory extends BagFactory {

    @Override
    protected void configure(SurpriseBag.Builder builder) {
        builder.title("Market Surpriz Paketi");
    }

    @Override
    protected BusinessType supportedType() {
        return BusinessType.MARKET;
    }
}