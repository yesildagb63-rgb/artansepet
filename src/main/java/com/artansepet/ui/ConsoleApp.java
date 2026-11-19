package com.artansepet.ui;

import com.artansepet.exception.ArtanSepetException;
import com.artansepet.model.Business;
import com.artansepet.model.Customer;
import com.artansepet.model.Location;
import com.artansepet.model.User;

import java.util.Optional;

/**
 * Uygulamanin giris ekrani: giris, kayit ve role gore dogru menuye yonlendirme.
 */
public class ConsoleApp {

    private static final String[] SEMTLER = {"Kadikoy", "Uskudar", "Besiktas", "Taksim"};
    private static final Location[] KONUMLAR = {
            new Location(40.9903, 29.0290),
            new Location(41.0260, 29.0150),
            new Location(41.0430, 29.0050),
            new Location(41.0370, 28.9850)
    };

    private final AppContext ctx;
    private final ConsoleIO io;

    public ConsoleApp(AppContext ctx, ConsoleIO io) {
        this.ctx = ctx;
        this.io = io;
    }

    public void run() {
        io.println("=== Artan Sepet: gida israfina karsi surpriz paketler ===");
        io.println("Demo hesaplar (sifre: 123456)");
        io.println("  Musteri: ayse@ornek.com, mehmet@ornek.com");
        io.println("  Isletme: firin@artansepet.com, pastane@artansepet.com,");
        io.println("           restoran@artansepet.com, manav@artansepet.com");
        while (true) {
            io.println("\n1) Giris yap   2) Musteri olarak kayit ol   0) Cikis");
            int secim = io.askInt("Seciminiz", 2);
            if (secim == 0) {
                io.println("Gule gule!");
                return;
            }
            try {
                if (secim == 1) {
                    girisYap();
                } else {
                    kayitOl();
                }
            } catch (ArtanSepetException | IllegalArgumentException | IllegalStateException e) {
                io.println("Hata: " + e.getMessage());
            }
        }
    }

    private void girisYap() {
        String email = io.ask("E-posta");
        String sifre = io.ask("Sifre");
        Optional<User> kullanici = ctx.users.findByEmail(email)
                .filter(u -> u.checkPassword(sifre));
        if (kullanici.isEmpty()) {
            // Hangisinin yanlis oldugunu soylemiyoruz (guvenlik)
            io.println("E-posta veya sifre hatali.");
            return;
        }
        menuyeYonlendir(kullanici.get());
    }

    private void kayitOl() {
        String isim = io.ask("Ad soyad");
        String email = io.ask("E-posta");
        String sifre = io.ask("Sifre (en az 6 karakter)");
        for (int i = 0; i < SEMTLER.length; i++) {
            io.println((i + 1) + ") " + SEMTLER[i]);
        }
        int semt = io.askInt("Semtiniz (0 = vazgec)", SEMTLER.length);
        if (semt == 0) {
            return;
        }
        Customer yeni = new Customer(isim, email, sifre, KONUMLAR[semt - 1]);
        ctx.users.save(yeni);                  // ayni e-posta varsa hata firlar
        io.println("Kayit tamamlandi.");
        menuyeYonlendir(yeni);
    }

    private void menuyeYonlendir(User u) {
        if (u instanceof Customer musteri) {
            new CustomerMenu(ctx, musteri, io).run();
        } else if (u instanceof Business isletme) {
            new BusinessMenu(ctx, isletme, io).run();
        }
    }
}