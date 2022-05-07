package org.hanrw.java.pattern.spi;

public class NetellerPaymentProvider implements PaymentProvider {
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
