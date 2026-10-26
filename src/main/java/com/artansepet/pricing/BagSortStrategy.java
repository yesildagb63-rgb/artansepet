package com.artansepet.pricing;

import com.artansepet.model.Customer;
import com.artansepet.model.SurpriseBag;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Strategy pattern: paket listesinin nasil siralanacagi.
 * Alt siniflar sadece karsilastirma kuralini (Comparator) verir.
 */
public interface BagSortStrategy {

    Comparator<SurpriseBag> comparator(Customer customer, LocalDateTime now);

    /** Listeyi bozmadan, siralanmis yeni bir liste dondurur. */
    default List<SurpriseBag> sort(List<SurpriseBag> bags, Customer customer, LocalDateTime now) {
        return bags.stream()
                .sorted(comparator(customer, now))
                .toList();
    }
}