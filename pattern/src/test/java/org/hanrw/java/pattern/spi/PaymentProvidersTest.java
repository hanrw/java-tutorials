package org.hanrw.java.pattern.spi;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentProvidersTest {

    @BeforeAll
    public static void init() {
        PaymentProviders.load();
    }

    @Test
    public void should_return_all_payment_providers() {
        List<PaymentProvider> paymentProviders = PaymentProviders.getProviders();
        assertEquals(2, paymentProviders.size());
        assertEquals("DefaultPaymentProvider", paymentProviders.get(0).getName());
        assertEquals("NetellerPaymentProvider", paymentProviders.get(1).getName());
    }

    @Test
    public void should_return_correct_payment_provider() {
        PaymentProvider paymentProvider = PaymentProviders.get("NetellerPaymentProvider");
        assertNotNull(paymentProvider);
        assertEquals("NetellerPaymentProvider", paymentProvider.getName());
    }

    @Test
    public void should_return_payment_provider_null_if_not_found() {
        PaymentProvider paymentProvider = PaymentProviders.get("some-name");
        assertNull(paymentProvider);
    }
}
