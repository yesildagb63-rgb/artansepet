package com.artansepet.service;

import com.artansepet.model.Order;
import com.artansepet.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Teslim saati gecmis acik siparisleri "Suresi Doldu" durumuna gecirir.
 * Gercek uygulamada bir zamanlayici bunu belirli araliklarla cagirir.
 */
public class ExpirationService {

    private final OrderRepository orderRepo;

    public ExpirationService(OrderRepository orderRepo) {
        this.orderRepo = Objects.requireNonNull(orderRepo);
    }

    /** @return kac siparisin suresinin doldugu */
    public int expireOverdueOrders(LocalDateTime now) {
        int sayac = 0;
        for (Order order : orderRepo.findOpen()) {
            if (order.isOverdue(now)) {
                order.expire();               // State: stok geri birakilir
                orderRepo.save(order);
                order.getCustomer().addNotification("Siparisinizin teslim suresi doldu: "
                        + order.getBag().getTitle());
                sayac++;
            }
        }
        return sayac;
    }
}