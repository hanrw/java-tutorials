package org.hanrw.java.pattern.spi;

import java.math.BigDecimal;

interface PaymentProvider {
    String getName();

    DepositResponse deposit(BigDecimal amount);
}
