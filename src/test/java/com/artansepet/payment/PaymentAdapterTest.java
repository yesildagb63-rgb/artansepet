package com.artansepet.payment;

import com.artansepet.exception.PaymentFailedException;
import com.artansepet.model.Customer;
import com.artansepet.model.Location;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentAdapterTest {

    private final Customer ayse = new Customer("Ayse", "ayse@ornek.com", "gizli123",
            new Location(40.99, 29.03));
    private final BigDecimal tutar = new BigDecimal("75.50");

    // ---------------- iyzico ----------------

    @Test
    void iyzicoBasariliOdemeyiCevirir() {
        PaymentResult sonuc = new IyzicoAdapter(new IyzicoClient()).charge(ayse, tutar, "SIP-1");
        assertTrue(sonuc.success());
        assertTrue(sonuc.transactionId().startsWith("IYZ-"));
    }

    @Test
    void iyzicoServisCokunceBasarisizSonucDoner() {
        IyzicoClient client = new IyzicoClient();
        client.setAvailable(false);
        PaymentResult sonuc = new IyzicoAdapter(client).charge(ayse, tutar, "SIP-1");
        assertFalse(sonuc.success());
        assertNull(sonuc.transactionId());
    }

    @Test
    void iyzicoIadesiIptalOlarakIletilir() {
        IyzicoClient client = new IyzicoClient();
        new IyzicoAdapter(client).refund("IYZ-123", tutar);
        assertEquals(List.of("IYZ-123"), client.getCancelledPayments());
    }

    @Test
    void iyzicoIadesiBasarisizsaBizimHataTurumuzFirlar() {
        IyzicoClient client = new IyzicoClient();
        client.setAvailable(false);
        assertThrows(PaymentFailedException.class,
                () -> new IyzicoAdapter(client).refund("IYZ-123", tutar));
    }

    // ---------------- PayTR ----------------

    @Test
    void paytrBasariliOdemeyiCevirir() {
        PaymentResult sonuc = new PaytrAdapter(new PaytrGateway()).charge(ayse, tutar, "SIP-1");
        assertTrue(sonuc.success());
        assertEquals("PAYTR-SIP-1", sonuc.transactionId());
    }

    @Test
    void paytrServisCokunceBasarisizSonucDoner() {
        PaytrGateway gateway = new PaytrGateway();
        gateway.setAvailable(false);
        PaymentResult sonuc = new PaytrAdapter(gateway).charge(ayse, tutar, "SIP-1");
        assertFalse(sonuc.success());
        assertTrue(sonuc.message().contains("kullanilamiyor"));
    }

    @Test
    void paytrIadesindeOnekTemizlenir() {
        PaytrGateway gateway = new PaytrGateway();
        new PaytrAdapter(gateway).refund("PAYTR-SIP-1", tutar);
        assertEquals(List.of("SIP-1"), gateway.getReversedTransactions());
    }

    @Test
    void paytrHatasiBizimHataTurumuzeCevrilir() {
        PaytrGateway gateway = new PaytrGateway();
        gateway.setAvailable(false);
        assertThrows(PaymentFailedException.class,
                () -> new PaytrAdapter(gateway).refund("PAYTR-SIP-1", tutar));
    }

    // ---------------- Desenin ozu ----------------

    @Test
    void ikiAdaptorAyniArayuzleBirbirininYerineGecer() {
        // Kodun geri kalani sadece PaymentProcessor'i bilir
        List<PaymentProcessor> sistemler = List.of(
                new IyzicoAdapter(new IyzicoClient()),
                new PaytrAdapter(new PaytrGateway()));

        for (PaymentProcessor sistem : sistemler) {
            assertTrue(sistem.charge(ayse, tutar, "SIP-1").success(),
                    sistem.getClass().getSimpleName() + " basarisiz oldu");
        }
    }
}