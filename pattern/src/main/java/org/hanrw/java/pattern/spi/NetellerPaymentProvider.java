package org.hanrw.java.pattern.spi;

import java.math.BigDecimal;

public class NetellerPaymentProvider implements PaymentProvider {
    private BigDecimal depositFee = BigDecimal.valueOf(0.1);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public DepositResponse deposit(BigDecimal amount) {
        return new DepositResponse(amount.subtract(depositFee));
    }
}
