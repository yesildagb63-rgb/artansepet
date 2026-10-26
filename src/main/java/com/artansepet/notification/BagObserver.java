package com.artansepet.notification;

import com.artansepet.model.SurpriseBag;

/**
 * Observer pattern: gozlemci.
 * Takip ettigi isletme yeni paket ekleyince haberdar edilir.
 */
public interface BagObserver {

    void onNewBag(SurpriseBag bag);
}