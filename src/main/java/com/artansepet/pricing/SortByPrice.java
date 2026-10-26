package com.artansepet.pricing;

import com.artansepet.model.Customer;
import com.artansepet.model.SurpriseBag;

import java.time.LocalDateTime;
import java.util.Comparator;

/** En ucuz paket once. Indirim stratejisi uygulanmis GUNCEL fiyata bakar. */
public class SortByPrice implements BagSortStrategy {

    @Override
    public Comparator<SurpriseBag> comparator(Customer customer, LocalDateTime now) {
        return Comparator.comparing(bag -> bag.getCurrentPrice(now));
    }
}