package com.artansepet.ui;

import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Konsoldan okuma/yazma. Menulerde 0 her zaman "geri / vazgec" demektir.
 */
public class ConsoleIO {

    private final Scanner in;
    private final PrintStream out;

    public ConsoleIO(InputStream in, PrintStream out) {
        this.in = new Scanner(in, StandardCharsets.UTF_8);
        this.out = out;
    }

    public void println(String text) {
        out.println(text);
    }

    /** Bir satir okur. Girdi biterse "0" doner (menulerden cikilir). */
    public String ask(String prompt) {
        out.print(prompt + ": ");
        return in.hasNextLine() ? in.nextLine().trim() : "0";
    }

    /** 0 ile max arasinda bir sayi okur, gecersizse tekrar sorar. */
    public int askInt(String prompt, int max) {
        while (true) {
            String s = ask(prompt);
            try {
                int v = Integer.parseInt(s);
                if (v >= 0 && v <= max) {
                    return v;
                }
            } catch (NumberFormatException ignored) {
                // asagida uyari verilir
            }
            out.println("Lutfen 0 ile " + max + " arasinda bir sayi girin.");
        }
    }

    /** Sifirdan buyuk bir tutar okur ("99,90" de kabul edilir). */
    public BigDecimal askMoney(String prompt) {
        while (true) {
            try {
                BigDecimal v = new BigDecimal(ask(prompt).replace(',', '.'));
                if (v.signum() > 0) {
                    return v;
                }
            } catch (NumberFormatException ignored) {
                // asagida uyari verilir
            }
            out.println("Gecerli bir tutar girin (ornek: 120 veya 99.90).");
        }
    }
}