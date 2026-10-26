package com.artansepet.notification;

import com.artansepet.model.SurpriseBag;

/**
 * Observer pattern: gozlenen.
 * Takipci listesi tutar ve yeni paket eklenince hepsine haber verir.
 */
public interface BagSubject {

    void subscribe(BagObserver observer);

    void unsubscribe(BagObserver observer);

    void notifyObservers(SurpriseBag bag);
}