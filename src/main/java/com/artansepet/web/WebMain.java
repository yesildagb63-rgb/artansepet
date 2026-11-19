package com.artansepet;

import com.artansepet.ui.AppContext;
import com.artansepet.ui.DemoData;
import com.artansepet.web.ApiServer;

import java.io.IOException;
import java.time.LocalDateTime;

/** Web surumunun giris noktasi. Konsol surumu icin Main hala calisir. */
public class WebMain {

    public static void main(String[] args) throws IOException {
        AppContext ctx = new AppContext();
        DemoData.load(ctx, LocalDateTime.now());
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        new ApiServer(ctx).start(port);
        System.out.println("Artan Sepet API calisiyor: http://localhost:" + port + "/api/health");
    }
}