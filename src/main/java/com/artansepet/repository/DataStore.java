package com.artansepet.repository;

import com.artansepet.model.SurpriseBag;
import com.artansepet.model.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton pattern.
 * Uygulamanin tum verisini bellekte tutan tek depo.
 * Dogrudan kullanilmaz; repository siniflari araciligiyla erisilir.
 */
public final class DataStore {

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, SurpriseBag> bags = new ConcurrentHashMap<>();

    // private: disaridan "new DataStore()" yazilamaz
    private DataStore() { }

    /**
     * Holder deseni: Holder sinifi ilk kez getInstance() cagrildiginda yuklenir.
     * Java sinif yuklemeyi thread-safe yaptigi icin ekstra synchronized gerekmez.
     */
    private static class Holder {
        private static final DataStore INSTANCE = new DataStore();
    }

    public static DataStore getInstance() {
        return Holder.INSTANCE;
    }

    // Repository'lerin kullanacagi haritalar (paket ici erisim)
    Map<String, User> users() { return users; }
    Map<String, SurpriseBag> bags() { return bags; }

    /** Tum veriyi siler. Sadece testlerde, testler birbirini etkilemesin diye kullanilir. */
    public void clear() {
        users.clear();
        bags.clear();
    }
}