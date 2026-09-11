package org.openefms.frauddetection.messaging;

import java.util.Objects;
import org.openefms.common.events.rabbitmq.RabbitMqExchanges;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oefms.rabbitmq")
public record FraudRabbitMqProperties(
        String exchange,
        String deadLetterExchange
) {
    public FraudRabbitMqProperties {
        exchange = textOrDefault(exchange, RabbitMqExchanges.DOMAIN_EVENTS);
        deadLetterExchange = textOrDefault(deadLetterExchange, RabbitMqExchanges.DEAD_LETTER);
    }

    private static String textOrDefault(String value, String defaultValue) {
        Objects.requireNonNull(defaultValue, "defaultValue must not be null");
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
