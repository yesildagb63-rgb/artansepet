package com.artansepet.repository;

import com.artansepet.model.Business;
import com.artansepet.model.Customer;
import com.artansepet.model.Order;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** OrderRepository'nin bellekte (DataStore) calisan hali. */
public class InMemoryOrderRepository implements OrderRepository {

    private final DataStore store = DataStore.getInstance();

    @Override
    public void save(Order order) {
        Objects.requireNonNull(order);
        store.orders().put(order.getId(), order);
    }

    @Override
    public Optional<Order> findById(String id) {
        return Optional.ofNullable(store.orders().get(id));
    }

    @Override
    public List<Order> findByCustomer(Customer customer) {
        return store.orders().values().stream()
                .filter(o -> o.getCustomer().equals(customer))
                .toList();
    }

    @Override
    public List<Order> findByBusiness(Business business) {
        return store.orders().values().stream()
                .filter(o -> o.getBag().getBusiness().equals(business))
                .toList();
    }

    @Override
    public List<Order> findOpen() {
        return store.orders().values().stream()
                .filter(o -> !o.getState().isFinal())
                .toList();
    }
}