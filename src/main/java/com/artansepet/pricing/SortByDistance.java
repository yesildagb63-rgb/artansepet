package com.artansepet.pricing;

import com.artansepet.model.Customer;
import com.artansepet.model.SurpriseBag;

import java.time.LocalDateTime;
import java.util.Comparator;

/** Musteriye en yakin isletmenin paketi once. */
public class SortByDistance implements BagSortStrategy {

    @Override
    public Comparator<SurpriseBag> comparator(Customer customer, LocalDateTime now) {
        return Comparator.comparingDouble(
                bag -> customer.getLocation().distanceTo(bag.getBusiness().getLocation()));
    }
}