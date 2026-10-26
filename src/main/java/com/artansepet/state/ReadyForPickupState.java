package com.artansepet.state;

import com.artansepet.exception.InvalidPickupCodeException;
import com.artansepet.model.Order;

/** Paket hazir, musterinin gelip kodla teslim almasi bekleniyor. */
public class ReadyForPickupState extends AbstractOrderState {

    @Override
    public OrderState pickUp(Order order, String code) {
        if (code == null || !code.equals(order.getPickupCode())) {
            // Durum degismez, siparis hazir beklemeye devam eder
            throw new InvalidPickupCodeException();
        }
        return new PickedUpState();
    }

    @Override
    public OrderState expire(Order order) {
        order.getBag().releaseOne();
        return new ExpiredState();
    }

    // cancel() ezilmedi: isletme paketi hazirladiktan sonra iptal edilemez

    @Override
    public String displayName() {
        return "Teslime Hazir";
    }
}