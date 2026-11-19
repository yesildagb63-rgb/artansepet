package com.artansepet;

import com.artansepet.ui.AppContext;
import com.artansepet.ui.ConsoleApp;
import com.artansepet.ui.ConsoleIO;
import com.artansepet.ui.DemoData;

import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {
        AppContext ctx = new AppContext();
        DemoData.load(ctx, LocalDateTime.now());
        new ConsoleApp(ctx, new ConsoleIO(System.in, System.out)).run();
    }

    static String message() {
        return "Artan Sepet çalışıyor";
    }
}