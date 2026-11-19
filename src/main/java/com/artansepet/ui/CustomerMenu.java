package com.artansepet.ui;

import com.artansepet.exception.ArtanSepetException;
import com.artansepet.model.Business;
import com.artansepet.model.Customer;
import com.artansepet.model.Order;
import com.artansepet.model.SurpriseBag;
import com.artansepet.pricing.BagSortStrategy;
import com.artansepet.pricing.SortByDistance;
import com.artansepet.pricing.SortByPickupTime;
import com.artansepet.pricing.SortByPrice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Musteri menusu. Siparis islemleri sadece OrderFacade, paket islemleri sadece BagService uzerinden yapilir.
 */
public class CustomerMenu {

    static final DateTimeFormatter SAAT = DateTimeFormatter.ofPattern("HH:mm");

    private final AppContext ctx;
    private final Customer me;
    private final ConsoleIO io;
    private List<SurpriseBag> sonListe = List.of();

    public CustomerMenu(AppContext ctx, Customer me, ConsoleIO io) {
        this.ctx = ctx;
        this.me = me;
        this.io = io;
    }

    public void run() {
        io.println("\nHos geldin, " + me.getName() + "!");
        while (true) {
            ctx.expirationService.expireOverdueOrders(LocalDateTime.now());
            io.println("\n--- Musteri Menusu ---");
            io.println("1) Paketleri listele");
            io.println("2) Siparis ver");
            io.println("3) Siparislerim");
            io.println("4) Siparis iptal et");
            io.println("5) Isletme takip et");
            io.println("6) Bildirimlerim (" + me.getNotifications().size() + ")");
            io.println("0) Cikis yap");
            int secim = io.askInt("Seciminiz", 6);
            if (secim == 0) {
                return;
            }
            try {
                switch (secim) {
                    case 1 -> paketleriListele();
                    case 2 -> siparisVer();
                    case 3 -> siparisleriGoster(false);
                    case 4 -> siparisIptal();
                    case 5 -> isletmeTakipEt();
                    case 6 -> bildirimler();
                    default -> { }
                }
            } catch (ArtanSepetException | IllegalArgumentException | IllegalStateException e) {
                io.println("Hata: " + e.getMessage());
            }
        }
    }

    private void paketleriListele() {
        io.println("Siralama: 1) Fiyat  2) Mesafe  3) Teslim saati");
        int s = io.askInt("Seciminiz", 3);
        if (s == 0) {
            return;
        }
        BagSortStrategy strateji = switch (s) {
            case 1 -> new SortByPrice();
            case 2 -> new SortByDistance();
            default -> new SortByPickupTime();
        };
        LocalDateTime now = LocalDateTime.now();
        sonListe = ctx.bagService.search(me, strateji, now);
        if (sonListe.isEmpty()) {
            io.println("Su an musait paket yok.");
            return;
        }
        for (int i = 0; i < sonListe.size(); i++) {
            SurpriseBag b = sonListe.get(i);
            double km = me.getLocation().distanceTo(b.getBusiness().getLocation());
            io.println(String.format(Locale.ROOT,
                    "%d) %s - %s | %s TL (normal %s TL) | %.1f km | kalan %d | teslim %s-%s %s",
                    i + 1, b.getTitle(), b.getBusiness().getName(),
                    b.getCurrentPrice(now), b.getOriginalPrice(), km, b.getQuantity(),
                    b.getPickupStart().format(SAAT), b.getPickupEnd().format(SAAT),
                    b.getTags().isEmpty() ? "" : b.getTags().toString()));
        }
    }
    
    private void siparisVer() {
        if (sonListe.isEmpty()) {
            paketleriListele();
            if (sonListe.isEmpty()) {
                return;
            }
        }
        int n = io.askInt("Paket numarasi (0 = vazgec)", sonListe.size());
        if (n == 0) {
            return;
        }
        SurpriseBag b = sonListe.get(n - 1);
        Order o = ctx.orderFacade.placeOrder(me.getId(), b.getId(), LocalDateTime.now());
        io.println("Siparisiniz alindi ve odendi: " + o.getPrice() + " TL");
        io.println("Teslim kodunuz: " + o.getPickupCode() + " (teslimde isletmeye soyleyin)");
    }

    /** Siparisleri numarali listeler. sadeceAcik=true ise kapanmislari gostermez. */
    private List<Order> siparisleriGoster(boolean sadeceAcik) {
        List<Order> liste = ctx.orders.findByCustomer(me).stream()
                .filter(o -> !sadeceAcik || !o.getState().isFinal())
                .sorted(Comparator.comparing(Order::getCreatedAt))
                .toList();
        if (liste.isEmpty()) {
            io.println(sadeceAcik ? "Acik siparisiniz yok." : "Henuz siparisiniz yok.");
            return liste;
        }
        for (int i = 0; i < liste.size(); i++) {
            Order o = liste.get(i);
            io.println((i + 1) + ") " + o.getBag().getTitle() + " - " + o.getBag().getBusiness().getName()
                    + " | " + o.getPrice() + " TL | " + o.getStateName()
                    + (o.getState().isFinal() ? "" : " | kod: " + o.getPickupCode()));
        }
        return liste;
    }

    private void siparisIptal() {
        List<Order> acik = siparisleriGoster(true);
        if (acik.isEmpty()) {
            return;
        }
        int n = io.askInt("Iptal edilecek siparis (0 = vazgec)", acik.size());
        if (n == 0) {
            return;
        }
        ctx.orderFacade.cancelOrder(acik.get(n - 1).getId());
        io.println("Siparis iptal edildi.");
    }

    private void isletmeTakipEt() {
        List<Business> isletmeler = ctx.users.findAll().stream()
                .filter(Business.class::isInstance)
                .map(Business.class::cast)
                .sorted(Comparator.comparing(Business::getName))
                .toList();
        for (int i = 0; i < isletmeler.size(); i++) {
            Business b = isletmeler.get(i);
            io.println((i + 1) + ") " + b.getName() + " (" + b.getType() + ") - " + b.getAddress());
        }
        int n = io.askInt("Takip edilecek isletme (0 = vazgec)", isletmeler.size());
        if (n == 0) {
            return;
        }
        Business secilen = isletmeler.get(n - 1);
        me.follow(secilen);
        io.println(secilen.getName() + " takip ediliyor. Yeni paket ekleyince bildirim alacaksiniz.");
    }

    private void bildirimler() {
        if (me.getNotifications().isEmpty()) {
            io.println("Bildiriminiz yok.");
            return;
        }
        me.getNotifications().forEach(m -> io.println("- " + m));
    }
}