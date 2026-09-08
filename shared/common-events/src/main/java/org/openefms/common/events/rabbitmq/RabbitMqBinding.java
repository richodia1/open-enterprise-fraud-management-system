package org.openefms.common.events.rabbitmq;

/**
 * Describes a queue binding without depending on a specific RabbitMQ client library.
 */
public record RabbitMqBinding(
        String queueName,
        String exchangeName,
        String routingKey
) {
    public RabbitMqBinding {
        queueName = requireText(queueName, "queueName");
        exchangeName = requireText(exchangeName, "exchangeName");
        routingKey = requireText(routingKey, "routingKey");
    }

    public static RabbitMqBinding domainEvent(String queueName, String routingKey) {
        return new RabbitMqBinding(queueName, RabbitMqExchanges.DOMAIN_EVENTS, routingKey);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
