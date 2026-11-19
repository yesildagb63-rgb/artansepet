package com.artansepet.ui;

import com.artansepet.exception.ArtanSepetException;
import com.artansepet.model.Business;
import com.artansepet.model.Order;
import com.artansepet.model.SurpriseBag;
import com.artansepet.pricing.FixedDiscountStrategy;
import com.artansepet.pricing.LastHourDiscountStrategy;
import com.artansepet.pricing.PricingStrategy;
import com.artansepet.pricing.WeekendDiscountStrategy;
import com.artansepet.state.PaidState;
import com.artansepet.state.ReadyForPickupState;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Isletme menusu. Paket yayinlama BagService, siparis islemleri OrderFacade uzerinden yapilir.
 */
public class BusinessMenu {

    private final AppContext ctx;
    private final Business me;
    private final ConsoleIO io;

    public BusinessMenu(AppContext ctx, Business me, ConsoleIO io) {
        this.ctx = ctx;
        this.me = me;
        this.io = io;
    }

    public void run() {
        io.println("\nHos geldin, " + me.getName() + "! (takipci: " + me.getFollowerCount() + ")");
        while (true) {
            ctx.expirationService.expireOverdueOrders(LocalDateTime.now());
            io.println("\n--- Isletme Menusu ---");
            io.println("1) Yeni paket yayinla");
            io.println("2) Paketlerim");
            io.println("3) Gelen siparisler");
            io.println("4) Siparisi hazir isaretle");
            io.println("5) Teslim kodunu dogrula");
            io.println("0) Cikis yap");
            int secim = io.askInt("Seciminiz", 5);
            if (secim == 0) {
                return;
            }
            try {
                switch (secim) {
                    case 1 -> paketYayinla();
                    case 2 -> paketlerim();
                    case 3 -> siparisleriGoster(o -> true);
                    case 4 -> hazirIsaretle();
                    case 5 -> teslimDogrula();
                    default -> { }
                }
            } catch (ArtanSepetException | IllegalArgumentException | IllegalStateException e) {
                io.println("Hata: " + e.getMessage());
            }
        }
    }

    private void paketYayinla() {
        BigDecimal fiyat = io.askMoney("Normal fiyat (TL)");
        int adet = io.askInt("Adet (0 = vazgec)", 100);
        if (adet == 0) {
            return;
        }
        int saat = io.askInt("Teslim kac saat surecek? (1-12, 0 = vazgec)", 12);
        if (saat == 0) {
            return;
        }
        io.println("Indirim: 1) Yok  2) Sabit  3) Son saat ek indirim  4) Hafta sonu");
        PricingStrategy strateji = switch (io.askInt("Seciminiz", 4)) {
            case 2 -> new FixedDiscountStrategy(yuzde("Indirim yuzdesi"));
            case 3 -> new LastHourDiscountStrategy(yuzde("Temel indirim yuzdesi"),
                                                   yuzde("Son saatteki ek indirim yuzdesi"));
            case 4 -> new WeekendDiscountStrategy(yuzde("Hafta sonu indirim yuzdesi"));
            default -> PricingStrategy.noDiscount();
        };

        LocalDateTime now = LocalDateTime.now();
        SurpriseBag paket = ctx.bagService.publishBag(me, fiyat, adet, now, now.plusHours(saat), strateji);
        io.println("Yayinlandi: " + paket.getTitle() + " | simdiki fiyat " + paket.getCurrentPrice(now)
                + " TL | " + me.getFollowerCount() + " takipciye bildirim gitti.");
    }

    /** Kullanicidan yuzde alir, orana cevirir (30 -> 0.30). */
    private double yuzde(String soru) {
        return io.askInt(soru + " (0-90)", 90) / 100.0;
    }
    
    private void paketlerim() {
        List<SurpriseBag> paketler = ctx.bagService.listBusinessBags(me);
        if (paketler.isEmpty()) {
            io.println("Henuz paketiniz yok.");
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (SurpriseBag b : paketler) {
            io.println("- " + b.getTitle() + " | " + b.getCurrentPrice(now) + " TL | kalan "
                    + b.getQuantity() + "/" + b.getInitialQuantity() + " | teslim "
                    + b.getPickupStart().format(CustomerMenu.SAAT) + "-"
                    + b.getPickupEnd().format(CustomerMenu.SAAT)
                    + (b.isPickupOver(now) ? " (suresi gecti)" : ""));
        }
    }

    /**
     * Filtreye uyan siparisleri numarali listeler.
     * Teslim kodu isletmeye GOSTERILMEZ: kodu musteri soyleyecek.
     */
    private List<Order> siparisleriGoster(Predicate<Order> filtre) {
        List<Order> liste = ctx.orders.findByBusiness(me).stream()
                .filter(filtre)
                .sorted(Comparator.comparing(Order::getCreatedAt))
                .toList();
        if (liste.isEmpty()) {
            io.println("Uygun siparis yok.");
            return liste;
        }
        for (int i = 0; i < liste.size(); i++) {
            Order o = liste.get(i);
            io.println((i + 1) + ") " + o.getCustomer().getName() + " | " + o.getBag().getTitle()
                    + " | " + o.getPrice() + " TL | " + o.getStateName());
        }
        return liste;
    }

    private void hazirIsaretle() {
        List<Order> odenmis = siparisleriGoster(o -> o.getState() instanceof PaidState);
        if (odenmis.isEmpty()) {
            return;
        }
        int n = io.askInt("Hazirlanan siparis (0 = vazgec)", odenmis.size());
        if (n == 0) {
            return;
        }
        ctx.orderFacade.markReady(odenmis.get(n - 1).getId());
        io.println("Siparis hazir olarak isaretlendi, musteriye bildirim gitti.");
    }

    private void teslimDogrula() {
        List<Order> hazir = siparisleriGoster(o -> o.getState() instanceof ReadyForPickupState);
        if (hazir.isEmpty()) {
            return;
        }
        int n = io.askInt("Teslim edilecek siparis (0 = vazgec)", hazir.size());
        if (n == 0) {
            return;
        }
        String kod = io.ask("Musterinin soyledigi 6 haneli kod");
        ctx.orderFacade.confirmPickup(hazir.get(n - 1).getId(), kod);
        io.println("Teslim tamamlandi. Bir paket daha israftan kurtuldu!");
    }
}