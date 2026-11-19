package com.artansepet.repository;

import com.artansepet.model.Business;
import com.artansepet.model.Customer;
import com.artansepet.model.Order;

import java.util.List;
import java.util.Optional;

/** Siparis verisine erisim. */
public interface OrderRepository {

    void save(Order order);

    Optional<Order> findById(String id);

    /** Musterinin "Siparislerim" ekrani icin. */
    List<Order> findByCustomer(Customer customer);

    /** Isletmenin gelen siparisleri gormesi icin. */
    List<Order> findByBusiness(Business business);

    /** Henuz kapanmamis (son durumda olmayan) siparisler. */
    List<Order> findOpen();
}