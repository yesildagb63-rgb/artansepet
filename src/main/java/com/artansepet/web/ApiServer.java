package com.artansepet.web;

import com.artansepet.exception.ArtanSepetException;
import com.artansepet.exception.NotFoundException;
import com.artansepet.model.Business;
import com.artansepet.model.Customer;
import com.artansepet.model.Location;
import com.artansepet.model.Order;
import com.artansepet.model.SurpriseBag;
import com.artansepet.model.User;
import com.artansepet.pricing.BagSortStrategy;
import com.artansepet.pricing.FixedDiscountStrategy;
import com.artansepet.pricing.LastHourDiscountStrategy;
import com.artansepet.pricing.PricingStrategy;
import com.artansepet.pricing.SortByDistance;
import com.artansepet.pricing.SortByPickupTime;
import com.artansepet.pricing.SortByPrice;
import com.artansepet.pricing.WeekendDiscountStrategy;
import com.artansepet.ui.AppContext;
import com.artansepet.web.Dtos.AuthResponse;
import com.artansepet.web.Dtos.BagDto;
import com.artansepet.web.Dtos.BusinessDto;
import com.artansepet.web.Dtos.LoginRequest;
import com.artansepet.web.Dtos.OrderDto;
import com.artansepet.web.Dtos.OrderRequest;
import com.artansepet.web.Dtos.PickupRequest;
import com.artansepet.web.Dtos.PublishRequest;
import com.artansepet.web.Dtos.RegisterRequest;
import com.artansepet.web.Dtos.UserDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * REST API. Konsol menuleri gibi sadece OrderFacade ve servislerle konusur;
 * is kurallarini bilmez, sadece istegi cevirir ve yetkiyi kontrol eder.
 */
public class ApiServer {

    private static final Map<String, Location> SEMTLER = Map.of(
            "Kadikoy", new Location(40.9903, 29.0290),
            "Uskudar", new Location(41.0260, 29.0150),
            "Besiktas", new Location(41.0430, 29.0050),
            "Taksim", new Location(41.0370, 28.9850));

    private final AppContext ctx;
    private final Sessions sessions = new Sessions();
    private HttpServer server;

    public ApiServer(AppContext ctx) {
        this.ctx = ctx;
    }

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api", this::handle);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
    }

    public int port() {
        return server.getAddress().getPort();
    }

    public void stop() {
        server.stop(0);
    }

    /** Her istek buradan gecer: once sure dolumu, sonra yonlendirme, hatalar tek yerde JSON'a cevrilir. */
    private void handle(HttpExchange ex) throws IOException {
        try {
            ctx.expirationService.expireOverdueOrders(LocalDateTime.now());
            route(ex, ex.getRequestMethod(), ex.getRequestURI().getPath());
        } catch (Unauthorized e) {
            Http.error(ex, 401, "Lütfen giriş yapın");
        } catch (Forbidden e) {
            Http.error(ex, 403, "Bu işlem için yetkiniz yok");
        } catch (NotFoundException e) {
            Http.error(ex, 404, e.getMessage());
        } catch (JsonProcessingException e) {
            Http.error(ex, 400, "Geçersiz istek verisi");
        } catch (ArtanSepetException | IllegalArgumentException | IllegalStateException e) {
            Http.error(ex, 400, e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            Http.error(ex, 500, "Sunucu hatası");
        } finally {
            ex.close();
        }
    }

    private void route(HttpExchange ex, String method, String path) throws IOException {
        switch (method + " " + path) {
            case "GET /api/health" -> Http.json(ex, 200, Map.of("status", "ok"));
            case "POST /api/login" -> login(ex);
            case "POST /api/register" -> register(ex);
            case "POST /api/logout" -> {
                sessions.remove(ex);
                Http.json(ex, 200, Map.of("ok", true));
            }
            case "GET /api/me" -> Http.json(ex, 200, UserDto.of(currentUser(ex)));
            case "GET /api/bags" -> listBags(ex);
            case "POST /api/bags" -> publishBag(ex);
            case "GET /api/my-bags" -> myBags(ex);
            case "GET /api/orders" -> listOrders(ex);
            case "POST /api/orders" -> placeOrder(ex);
            case "GET /api/businesses" -> listBusinesses(ex);
            case "GET /api/notifications" -> Http.json(ex, 200, customer(ex).getNotifications());
            default -> routeWithId(ex, method, path.split("/"));
        }
    }

    /** /api/orders/{id}/cancel gibi adresler. parts: ["", "api", "orders", id, islem] */
    private void routeWithId(HttpExchange ex, String method, String[] parts) throws IOException {
        if ("POST".equals(method) && parts.length == 5) {
            String id = parts[3];
            switch (parts[2] + "/" + parts[4]) {
                case "orders/cancel" -> cancelOrder(ex, id);
                case "orders/ready" -> markReady(ex, id);
                case "orders/pickup" -> confirmPickup(ex, id);
                case "businesses/follow" -> follow(ex, id, true);
                case "businesses/unfollow" -> follow(ex, id, false);
                default -> Http.error(ex, 404, "Adres bulunamadı");
            }
            return;
        }
        Http.error(ex, 404, "Adres bulunamadı");
    }
    
    // ---------------- Giris ve kayit ----------------

    private void login(HttpExchange ex) throws IOException {
        LoginRequest req = Http.body(ex, LoginRequest.class);
        var user = ctx.users.findByEmail(req.email()).filter(u -> u.checkPassword(req.password()));
        if (user.isEmpty()) {
            Http.error(ex, 401, "E-posta veya şifre hatalı");
            return;
        }
        Http.json(ex, 200, new AuthResponse(sessions.create(user.get().getId()), UserDto.of(user.get())));
    }

    private void register(HttpExchange ex) throws IOException {
        RegisterRequest req = Http.body(ex, RegisterRequest.class);
        Location konum = req.district() == null
                ? SEMTLER.get("Kadikoy")
                : SEMTLER.getOrDefault(req.district(), SEMTLER.get("Kadikoy"));
        Customer yeni = new Customer(req.name(), req.email(), req.password(), konum);
        ctx.users.save(yeni);
        Http.json(ex, 201, new AuthResponse(sessions.create(yeni.getId()), UserDto.of(yeni)));
    }

    // ---------------- Paketler ----------------

    private void listBags(HttpExchange ex) throws IOException {
        Customer me = customer(ex);
        BagSortStrategy sort = switch (Http.query(ex).getOrDefault("sort", "price")) {
            case "distance" -> new SortByDistance();
            case "time" -> new SortByPickupTime();
            default -> new SortByPrice();
        };
        LocalDateTime now = LocalDateTime.now();
        List<BagDto> list = ctx.bagService.search(me, sort, now).stream()
                .map(b -> BagDto.of(b, now, me.getLocation()))
                .toList();
        Http.json(ex, 200, list);
    }

    private void publishBag(HttpExchange ex) throws IOException {
        Business me = business(ex);
        PublishRequest req = Http.body(ex, PublishRequest.class);
        if (req.hours() < 1 || req.hours() > 12) {
            throw new IllegalArgumentException("Teslim süresi 1-12 saat olmalı");
        }
        PricingStrategy strateji = switch (req.discount() == null ? "none" : req.discount()) {
            case "fixed" -> new FixedDiscountStrategy(req.rate() / 100.0);
            case "lastHour" -> new LastHourDiscountStrategy(req.rate() / 100.0, req.extraRate() / 100.0);
            case "weekend" -> new WeekendDiscountStrategy(req.rate() / 100.0);
            default -> PricingStrategy.noDiscount();
        };
        LocalDateTime now = LocalDateTime.now();
        SurpriseBag bag = ctx.bagService.publishBag(me, req.price(), req.quantity(),
                now, now.plusHours(req.hours()), strateji);
        Http.json(ex, 201, BagDto.of(bag, now, null));
    }

    private void myBags(HttpExchange ex) throws IOException {
        Business me = business(ex);
        LocalDateTime now = LocalDateTime.now();
        Http.json(ex, 200, ctx.bagService.listBusinessBags(me).stream()
                .sorted(Comparator.comparing(SurpriseBag::getPickupEnd).reversed())
                .map(b -> BagDto.of(b, now, null))
                .toList());
    }

    // ---------------- Siparisler ----------------

    private void listOrders(HttpExchange ex) throws IOException {
        User u = currentUser(ex);
        Comparator<Order> yeniOnce = Comparator.comparing(Order::getCreatedAt).reversed();
        List<OrderDto> list = (u instanceof Business b)
                ? ctx.orders.findByBusiness(b).stream().sorted(yeniOnce).map(OrderDto::forBusiness).toList()
                : ctx.orders.findByCustomer((Customer) u).stream().sorted(yeniOnce).map(OrderDto::forCustomer).toList();
        Http.json(ex, 200, list);
    }

    private void placeOrder(HttpExchange ex) throws IOException {
        Customer me = customer(ex);
        OrderRequest req = Http.body(ex, OrderRequest.class);
        Order o = ctx.orderFacade.placeOrder(me.getId(), req.bagId(), LocalDateTime.now());
        Http.json(ex, 201, OrderDto.forCustomer(o));
    }

    private void cancelOrder(HttpExchange ex, String id) throws IOException {
        Customer me = customer(ex);
        Order o = order(id);
        if (!o.getCustomer().equals(me)) {
            throw new Forbidden();               // baskasinin siparisi iptal edilemez
        }
        ctx.orderFacade.cancelOrder(id);
        Http.json(ex, 200, OrderDto.forCustomer(o));
    }

    private void markReady(HttpExchange ex, String id) throws IOException {
        Order o = ordersOfMyBusiness(ex, id);
        ctx.orderFacade.markReady(id);
        Http.json(ex, 200, OrderDto.forBusiness(o));
    }

    private void confirmPickup(HttpExchange ex, String id) throws IOException {
        Order o = ordersOfMyBusiness(ex, id);
        PickupRequest req = Http.body(ex, PickupRequest.class);
        ctx.orderFacade.confirmPickup(id, req.code());
        Http.json(ex, 200, OrderDto.forBusiness(o));
    }

    // ---------------- Takip ----------------

    private void listBusinesses(HttpExchange ex) throws IOException {
        Customer me = customer(ex);
        Http.json(ex, 200, ctx.users.findAll().stream()
                .filter(Business.class::isInstance)
                .map(Business.class::cast)
                .sorted(Comparator.comparing(Business::getName))
                .map(b -> BusinessDto.of(b, me))
                .toList());
    }

    private void follow(HttpExchange ex, String businessId, boolean takipEt) throws IOException {
        Customer me = customer(ex);
        Business b = ctx.users.findById(businessId)
                .filter(Business.class::isInstance)
                .map(Business.class::cast)
                .orElseThrow(() -> new NotFoundException("Isletme", businessId));
        if (takipEt) {
            me.follow(b);
        } else {
            me.unfollow(b);
        }
        Http.json(ex, 200, BusinessDto.of(b, me));
    }

    // ---------------- Yetki kontrolu ----------------

    private User currentUser(HttpExchange ex) {
        return sessions.userId(ex).flatMap(ctx.users::findById).orElseThrow(Unauthorized::new);
    }

    private Customer customer(HttpExchange ex) {
        if (currentUser(ex) instanceof Customer c) {
            return c;
        }
        throw new Forbidden();
    }

    private Business business(HttpExchange ex) {
        if (currentUser(ex) instanceof Business b) {
            return b;
        }
        throw new Forbidden();
    }

    private Order order(String id) {
        return ctx.orders.findById(id).orElseThrow(() -> new NotFoundException("Siparis", id));
    }

    /** Siparis bu isletmenin paketine mi ait? Degilse baska isletme dokunamaz. */
    private Order ordersOfMyBusiness(HttpExchange ex, String id) {
        Business me = business(ex);
        Order o = order(id);
        if (!o.getBag().getBusiness().equals(me)) {
            throw new Forbidden();
        }
        return o;
    }

    private static class Unauthorized extends RuntimeException { }

    private static class Forbidden extends RuntimeException { }
}