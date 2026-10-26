package com.artansepet.state;

import com.artansepet.model.Order;


public interface OrderState {

    OrderState markPaid(Order order);

    OrderState markReady(Order order);

    OrderState pickUp(Order order, String code);

    OrderState cancel(Order order);

    OrderState expire(Order order);

    
    String displayName();

    /** Son durum mu? (Teslim alindi, iptal, suresi doldu) */
    default boolean isFinal() {
        return false;
    }
}