package org.hanrw.java.pattern.strategy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class CampaignStrategyContext {
    private Campaign context;
    private static final Map<Class<? extends Campaign>, Campaign> CAMPAIGNS = Map.of(
            ChristmasCampaign.class, new ChristmasCampaign(),
            EasterCampaign.class, new EasterCampaign()
    );

    private static final Map<Class<? extends Campaign>, Campaign> CAMPAIGNS_LAMBDA = Map.of(
            ChristmasCampaign.class, amount -> amount.multiply(BigDecimal.valueOf(0.9)),
            EasterCampaign.class, amount -> amount.multiply(BigDecimal.valueOf(0.8))
    );

    static List<Campaign> ALL_CAMPAIGNS = List.of(
            amount -> amount.multiply(BigDecimal.valueOf(0.9)),
            amount -> amount.multiply(BigDecimal.valueOf(0.8))
    );
    private static Campaign combinedCampaign = ALL_CAMPAIGNS.stream().reduce(v -> v, Campaign::combine);


    public BigDecimal applyDiscounter(BigDecimal price) {
        return context.apply(price);
    }

    public static BigDecimal applyDiscounter(BigDecimal price, Class<? extends Campaign> campaign) {
        return CAMPAIGNS.get(campaign).apply(price);
    }

    public static BigDecimal applyDiscounterOnOriginalPrice(BigDecimal price) {
        return price.subtract(ALL_CAMPAIGNS.stream().map(campaign -> price.subtract(campaign.apply(price))).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public static BigDecimal applyDiscounterOnDiscountPrice(BigDecimal price) {
        AtomicReference<BigDecimal> discountPrice = new AtomicReference<>(price);
        ALL_CAMPAIGNS.forEach(campaign -> discountPrice.set(campaign.apply(discountPrice.get())));
        return discountPrice.get();
    }


    public static BigDecimal applyDiscounterWithUnaryOperator(BigDecimal price) {
        return combinedCampaign.apply(price);
    }

    public void setContext(Campaign context) {
        this.context = context;
    }
}