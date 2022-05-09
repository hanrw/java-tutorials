package org.hanrw.java.pattern.spi;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class PaymentProviders {
    private static List<PaymentProvider> providers;

    public static void load() {
        providers = StreamSupport.stream(ServiceLoader.load(PaymentProvider.class).spliterator(), false).collect(Collectors.toList());
    }

    public static List<PaymentProvider> getProviders() {
        return providers;
    }

    public static PaymentProvider get(String providerName) {
        return providers.stream().filter(p -> p.getName().equals(providerName)).findFirst().orElse(null);
    }

    public static PaymentProvider get() {
        return providers.stream().filter(p -> "DefaultPaymentProvider".equals(p.getName())).findFirst().orElse(null);
    }
}
