package org.hanrw.java.pattern.strategy;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CampaignStrategyContextTest {


    @Test
    public void should_apply_christmas_discounter_when_switch_to_ChristmasCampaign() {
        CampaignStrategyContext context = new CampaignStrategyContext();
        context.setContext(new ChristmasCampaign());
        assertEquals(BigDecimal.valueOf(90.0), context.applyDiscounter(BigDecimal.valueOf(100)));
    }

    @Test
    public void should_apply_christmas_discounter() {
        BigDecimal afterDiscount = CampaignStrategyContext.applyDiscounter(BigDecimal.valueOf(100), ChristmasCampaign.class);
        assertEquals(BigDecimal.valueOf(90.0), afterDiscount);
    }

    @Test
    public void should_apply_easter_discounter() {
        BigDecimal afterDiscount = CampaignStrategyContext.applyDiscounter(BigDecimal.valueOf(100), EasterCampaign.class);
        assertEquals(BigDecimal.valueOf(80.0), afterDiscount);
    }

    @Test
    public void should_apply_all_discounter_on_original_price() {
        BigDecimal afterDiscount = CampaignStrategyContext.applyDiscounterOnOriginalPrice(BigDecimal.valueOf(100));
        assertEquals(BigDecimal.valueOf(70.0), afterDiscount);
    }

    @Test
    public void should_apply_all_discounter_on_discount_price() {
        BigDecimal afterDiscount = CampaignStrategyContext.applyDiscounterOnDiscountPrice(BigDecimal.valueOf(100));
        assertEquals(72, afterDiscount.intValue());
    }


    @Test
    public void should_apply_all_discounter_with_unary_operator() {
        BigDecimal afterDiscount = CampaignStrategyContext.applyDiscounterWithUnaryOperator(BigDecimal.valueOf(100));
        assertEquals(72, afterDiscount.intValue());
    }
}