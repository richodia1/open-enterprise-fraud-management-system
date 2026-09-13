package org.openefms.ingestionmanagement.messaging;

import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqPublisherConfiguration {
    private final IngestionRabbitMqProperties properties;

    public RabbitMqPublisherConfiguration(IngestionRabbitMqProperties properties) {
        this.properties = properties;
    }

    @Bean
    public MessageConverter rabbitJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public Declarables domainEventExchanges() {
        return new Declarables(
                ExchangeBuilder.topicExchange(properties.exchange()).durable(true).build(),
                ExchangeBuilder.topicExchange(properties.deadLetterExchange()).durable(true).build());
    }
}
