package org.hanrw.java.pattern.spi;

import java.math.BigDecimal;

public class DefaultPaymentProvider implements PaymentProvider {
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public DepositResponse deposit(BigDecimal amount) {
        return new DepositResponse(amount);
    }
}
