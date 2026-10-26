package com.artansepet.pricing;

import com.artansepet.model.Customer;
import com.artansepet.model.SurpriseBag;

import java.time.LocalDateTime;
import java.util.Comparator;

/** Teslim suresi en erken bitecek paket once ("kacirmadan al"). */
public class SortByPickupTime implements BagSortStrategy {

    @Override
    public Comparator<SurpriseBag> comparator(Customer customer, LocalDateTime now) {
        return Comparator.comparing(SurpriseBag::getPickupEnd);
    }
}