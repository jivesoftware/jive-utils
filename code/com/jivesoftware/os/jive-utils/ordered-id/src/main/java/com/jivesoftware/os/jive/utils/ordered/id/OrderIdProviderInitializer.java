package com.jivesoftware.os.jive.utils.ordered.id;

/**
 *
 *
 */
public class OrderIdProviderInitializer {
    public OrderIdProvider initialize(OrderIdProviderConfig config) {
        return new OrderIdProviderImpl(config.getWriterId());
    }
}
