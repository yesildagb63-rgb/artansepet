package com.artansepet.state;

import com.artansepet.exception.InvalidStateTransitionException;
import com.artansepet.model.Order;


public abstract class AbstractOrderState implements OrderState {

    @Override
    public OrderState markPaid(Order order) {
        throw new InvalidStateTransitionException(displayName(), "odeme");
    }

    @Override
    public OrderState markReady(Order order) {
        throw new InvalidStateTransitionException(displayName(), "hazir isaretleme");
    }

    @Override
    public OrderState pickUp(Order order, String code) {
        throw new InvalidStateTransitionException(displayName(), "teslim alma");
    }

    @Override
    public OrderState cancel(Order order) {
        throw new InvalidStateTransitionException(displayName(), "iptal");
    }

    @Override
    public OrderState expire(Order order) {
        throw new InvalidStateTransitionException(displayName(), "sure dolumu");
    }

    @Override
    public String toString() {
        return displayName();
    }
}