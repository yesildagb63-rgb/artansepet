package com.artansepet.ui;

import com.artansepet.payment.PaymentProcessor;
import com.artansepet.payment.PaytrAdapter;
import com.artansepet.payment.PaytrGateway;
import com.artansepet.repository.BagRepository;
import com.artansepet.repository.InMemoryBagRepository;
import com.artansepet.repository.InMemoryOrderRepository;
import com.artansepet.repository.InMemoryUserRepository;
import com.artansepet.repository.OrderRepository;
import com.artansepet.repository.UserRepository;
import com.artansepet.service.BagService;
import com.artansepet.service.ExpirationService;
import com.artansepet.service.OrderFacade;
import com.artansepet.service.PickupCodeGenerator;

/**
 * Uygulamanin tum parcalarini bir kez olusturup birbirine baglar.
 * Odeme sistemini degistirmek icin sadece burada tek satir degisir (Adapter).
 */
public class AppContext {

    public final UserRepository users = new InMemoryUserRepository();
    public final BagRepository bags = new InMemoryBagRepository();
    public final OrderRepository orders = new InMemoryOrderRepository();

    // iyzico'ya gecmek icin: new IyzicoAdapter(new IyzicoClient())
    public final PaymentProcessor payment = new PaytrAdapter(new PaytrGateway());

    public final OrderFacade orderFacade =
            new OrderFacade(users, bags, orders, payment, new PickupCodeGenerator());
    public final BagService bagService = new BagService(bags);
    public final ExpirationService expirationService = new ExpirationService(orders);
}