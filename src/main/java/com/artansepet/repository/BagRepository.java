package com.artansepet.repository;

import com.artansepet.model.Business;
import com.artansepet.model.SurpriseBag;
import java.util.Locale;
import java.util.List;
import java.util.Optional;

/** Surpriz paket verisine erisim. */
public interface BagRepository {

    void save(SurpriseBag bag);

    Optional<SurpriseBag> findById(String id);

    /** Stokta en az bir adedi kalan paketler. */
    List<SurpriseBag> findAvailable();

    /** Bir isletmenin tum paketleri (isletme paneli icin). */
    List<SurpriseBag> findByBusiness(Business business);
}