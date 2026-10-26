package com.artansepet.state;

import com.artansepet.model.Order;


public class PaidState extends AbstractOrderState {

    @Override
    public OrderState markReady(Order order) {
        return new ReadyForPickupState();
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
        return "Odendi";
    }
}