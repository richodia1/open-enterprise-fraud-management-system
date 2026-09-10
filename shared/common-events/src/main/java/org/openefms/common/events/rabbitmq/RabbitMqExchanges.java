package org.openefms.common.events.rabbitmq;

/**
 * Shared RabbitMQ exchange names for OEFMS integration events.
 */
public final class RabbitMqExchanges {
    public static final String DOMAIN_EVENTS = "oefms.events";
    public static final String DEAD_LETTER = "oefms.events.dlx";

    private RabbitMqExchanges() {
    }
}
