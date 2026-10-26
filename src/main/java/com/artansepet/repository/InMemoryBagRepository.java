package com.artansepet.repository;

import com.artansepet.model.Business;
import com.artansepet.model.SurpriseBag;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** BagRepository'nin bellekte (DataStore) calisan hali. */
public class InMemoryBagRepository implements BagRepository {

    private final DataStore store = DataStore.getInstance();

    @Override
    public void save(SurpriseBag bag) {
        Objects.requireNonNull(bag);
        store.bags().put(bag.getId(), bag);
    }

    @Override
    public Optional<SurpriseBag> findById(String id) {
        return Optional.ofNullable(store.bags().get(id));
    }

    @Override
    public List<SurpriseBag> findAvailable() {
        return store.bags().values().stream()
                .filter(SurpriseBag::isAvailable)
                .toList();
    }

    @Override
    public List<SurpriseBag> findByBusiness(Business business) {
        return store.bags().values().stream()
                .filter(b -> b.getBusiness().equals(business))
                .toList();
    }
}