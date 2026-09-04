package com.serviceorders.common.events;

public final class KafkaTopics {

    public static final String USER_CREATED = "user.created";

    public static final String ORDER_CREATED = "order.created";

    public static final String INVENTORY_RESERVED = "inventory.reserved";

    public static final String ORDER_STATUS_CHANGED = "order.status-changed";

    private KafkaTopics() {
    }
}