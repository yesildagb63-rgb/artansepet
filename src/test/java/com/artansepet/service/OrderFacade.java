package com.artansepet.service;

import com.artansepet.exception.NotFoundException;
import com.artansepet.exception.PaymentFailedException;
import com.artansepet.model.Customer;
import com.artansepet.model.Order;
import com.artansepet.model.SurpriseBag;
import com.artansepet.payment.PaymentProcessor;
import com.artansepet.payment.PaymentResult;
import com.artansepet.repository.BagRepository;
import com.artansepet.repository.OrderRepository;
import com.artansepet.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Facade pattern.
 * Siparis surecinin tum adimlarini (stok, teslim kodu, odeme, kayit, bildirim)
 * tek bir sade arayuzun arkasinda toplar. Arayuz kodu sadece bu sinifi bilir.
 */
public class OrderFacade {

    private final UserRepository userRepo;
    private final BagRepository bagRepo;
    private final OrderRepository orderRepo;
    private final PaymentProcessor payment;
    private final PickupCodeGenerator codeGenerator;

    public OrderFacade(UserRepository userRepo, BagRepository bagRepo, OrderRepository orderRepo,
                       PaymentProcessor payment, PickupCodeGenerator codeGenerator) {
        this.userRepo = Objects.requireNonNull(userRepo);
        this.bagRepo = Objects.requireNonNull(bagRepo);
        this.orderRepo = Objects.requireNonNull(orderRepo);
        this.payment = Objects.requireNonNull(payment);
        this.codeGenerator = Objects.requireNonNull(codeGenerator);
    }

    /**
     * Siparis verir: stok ayirir, teslim kodu uretir, odemeyi alir, kaydeder, bildirir.
     * Odeme basarisiz olursa siparis iptal edilir ve stok geri birakilir.
     */
    public Order placeOrder(String customerId, String bagId, LocalDateTime now) {
        Customer customer = findCustomer(customerId);
        SurpriseBag bag = bagRepo.findById(bagId)
                .orElseThrow(() -> new NotFoundException("Paket", bagId));

        // 1-2. Teslim kodu uret, siparisi olustur ve stok ayir (stok yoksa burada hata firlar)
        Order order = Order.create(customer, bag, codeGenerator.generate(), now);

        // 3. Odemeyi al (Adapter uzerinden; hangi odeme sistemi oldugu bilinmez)
        PaymentResult result = payment.charge(customer, order.getPrice(), order.getId());

        // 4. Odeme basarisizsa geri al: siparisi iptal et, stok geri doner
        if (!result.success()) {
            order.cancel();
            orderRepo.save(order);
            throw new PaymentFailedException(result.message());
        }

        // 5-6. Odendi olarak isaretle ve kaydet
        order.markPaid(result.transactionId());
        orderRepo.save(order);

        // 7. Musteriye bildirim
        customer.addNotification("Siparisiniz alindi: " + bag.getTitle()
                + " (" + order.getPrice() + " TL). Teslim kodunuz: " + order.getPickupCode());
        return order;
    }
    
    /**
     * Siparisi iptal eder. Odenmis bir siparisse iade de yapilir.
     * Once durum gecisi denenir; izin yoksa (or. teslime hazir) iade hic yapilmaz.
     */
    public void cancelOrder(String orderId) {
        Order order = findOrder(orderId);
        boolean odenmisti = order.isPaid();

        order.cancel();                       // izin yoksa InvalidStateTransitionException
        if (odenmisti) {
            payment.refund(order.getTransactionId(), order.getPrice());
        }
        orderRepo.save(order);
        order.getCustomer().addNotification("Siparisiniz iptal edildi: " + order.getBag().getTitle()
                + (odenmisti ? ". Odemeniz iade edildi." : "."));
    }

    /** Isletme paketi hazirladiginda cagirir. */
    public void markReady(String orderId) {
        Order order = findOrder(orderId);
        order.markReady();
        orderRepo.save(order);
        order.getCustomer().addNotification("Paketiniz hazir: " + order.getBag().getTitle()
                + ". Teslim saatinde kodunuzla alabilirsiniz.");
    }

    /** Isletme, musterinin soyledigi teslim kodunu girer. */
    public void confirmPickup(String orderId, String code) {
        Order order = findOrder(orderId);
        order.pickUp(code);                   // yanlis kodda InvalidPickupCodeException
        orderRepo.save(order);
    }

    // ---------------- Yardimci metotlar ----------------

    private Customer findCustomer(String id) {
        return userRepo.findById(id)
                .filter(Customer.class::isInstance)
                .map(Customer.class::cast)
                .orElseThrow(() -> new NotFoundException("Musteri", id));
    }

    private Order findOrder(String id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Siparis", id));
    }
}