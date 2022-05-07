package org.hanrw.java.pattern.strategy;

import java.math.BigDecimal;

public class ChristmasCampaign implements Campaign {

    @Override
    public BigDecimal apply(BigDecimal price) {
        return price.multiply(new BigDecimal("0.9"));
    }
}
