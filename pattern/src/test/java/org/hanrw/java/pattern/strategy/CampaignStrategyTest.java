package org.hanrw.java.pattern.strategy;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CampaignStrategyTest {
    @Test
    public void should_apply_easter_discounter() {
        BigDecimal afterDiscount = CampaignStrategy.applyDiscounter(BigDecimal.valueOf(100), "EasterCampaign");
        assertEquals(BigDecimal.valueOf(80.0), afterDiscount);
    }

    @Test
    public void should_apply_christmas_discounter() {
        BigDecimal afterDiscount = CampaignStrategy.applyDiscounter(BigDecimal.valueOf(100), "ChristmasCampaign");
        assertEquals(BigDecimal.valueOf(90.0), afterDiscount);
    }
}
