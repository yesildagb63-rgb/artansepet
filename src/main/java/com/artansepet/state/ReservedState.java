package com.artansepet.state;

import com.artansepet.model.Order;


public class ReservedState extends AbstractOrderState {

    @Override
    public OrderState markPaid(Order order) {
        return new PaidState();
    }

    @Override
    public OrderState cancel(Order order) {
        order.getBag().releaseOne();      
        return new CancelledState();
    }

    @Override
    public OrderState expire(Order order) {
        order.getBag().releaseOne();
        return new ExpiredState();
    }

    @Override
    public String displayName() {
        return "Rezerve";
    }
}