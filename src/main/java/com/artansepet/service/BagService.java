package com.artansepet.service;

import com.artansepet.creation.BagFactory;
import com.artansepet.model.Business;
import com.artansepet.model.Customer;
import com.artansepet.model.SurpriseBag;
import com.artansepet.pricing.BagSortStrategy;
import com.artansepet.pricing.PricingStrategy;
import com.artansepet.repository.BagRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Paket islemleri: isletmenin paket yayinlamasi ve musterinin paket aramasi.
 * Yeni bir desen degil; Factory Method, Repository, Observer ve Strategy'yi bir araya getirir.
 */
public class BagService {

    private final BagRepository bagRepo;

    public BagService(BagRepository bagRepo) {
        this.bagRepo = Objects.requireNonNull(bagRepo);
    }

    /**
     * Isletme yeni paket yayinlar:
     * 1) turune uygun fabrika paketi olusturur (Factory Method),
     * 2) paket kaydedilir (Repository / Singleton),
     * 3) isletmenin takipcilerine haber verilir (Observer).
     */
    public SurpriseBag publishBag(Business business, BigDecimal price, int quantity,
                                  LocalDateTime pickupStart, LocalDateTime pickupEnd,
                                  PricingStrategy pricing) {
        BagFactory factory = BagFactory.forType(business.getType());
        SurpriseBag bag = factory.createBag(business, price, quantity, pickupStart, pickupEnd, pricing);
        bagRepo.save(bag);
        business.notifyObservers(bag);
        return bag;
    }

    /**
     * Musterinin gorebilecegi paketler: stokta olan ve teslim saati gecmemis olanlar,
     * secilen kurala gore siralanmis (Strategy).
     */
    public List<SurpriseBag> search(Customer customer, BagSortStrategy sort, LocalDateTime now) {
        List<SurpriseBag> uygun = bagRepo.findAvailable().stream()
                .filter(bag -> !bag.isPickupOver(now))
                .toList();
        return sort.sort(uygun, customer, now);
    }

    /** Isletmenin kendi paketleri (isletme paneli icin). */
    public List<SurpriseBag> listBusinessBags(Business business) {
        return bagRepo.findByBusiness(business);
    }
}