package org.hanrw.java.pattern.strategy;

import java.math.BigDecimal;

public class CampaignStrategy {
    public static BigDecimal applyDiscounter(BigDecimal amount, String campaign) {
        if ("EasterCampaign".equals(campaign)) {
            return amount.multiply(new BigDecimal("0.8"));
        }
        if ("ChristmasCampaign".equals(campaign)) {
            return amount.multiply(new BigDecimal("0.9"));
        }
        return amount;
    }
}
