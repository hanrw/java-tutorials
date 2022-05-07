package org.hanrw.java.pattern.strategy;

import java.math.BigDecimal;
import java.util.function.UnaryOperator;

public interface Campaign extends UnaryOperator<BigDecimal> {

    default Campaign combine(Campaign after) {
        return value -> after.apply(this.apply(value));
    }

    BigDecimal apply(BigDecimal price);
}
