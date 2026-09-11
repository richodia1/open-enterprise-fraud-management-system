package org.openefms.frauddetection.messaging;

import org.openefms.common.events.rabbitmq.RabbitMqQueues;
import org.openefms.common.events.rabbitmq.RabbitMqRoutingKeys;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class RabbitMqConsumerConfiguration {
    private final FraudRabbitMqProperties properties;

    RabbitMqConsumerConfiguration(FraudRabbitMqProperties properties) {
        this.properties = properties;
    }

    @Bean
    MessageConverter rabbitJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    Declarables fraudTransactionReceivedTopology() {
        TopicExchange domainExchange = ExchangeBuilder.topicExchange(properties.exchange()).durable(true).build();
        TopicExchange deadLetterExchange = ExchangeBuilder.topicExchange(properties.deadLetterExchange()).durable(true).build();
        Queue transactionReceivedQueue = QueueBuilder
                .durable(RabbitMqQueues.FRAUD_DETECTION_TRANSACTION_RECEIVED)
                .withArgument("x-dead-letter-exchange", properties.deadLetterExchange())
                .build();
        Queue deadLetterQueue = QueueBuilder.durable(RabbitMqQueues.DEAD_LETTER).build();
        Binding transactionReceivedBinding = BindingBuilder
                .bind(transactionReceivedQueue)
                .to(domainExchange)
                .with(RabbitMqRoutingKeys.TRANSACTION_RECEIVED);
        Binding deadLetterBinding = BindingBuilder
                .bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with("#");

        return new Declarables(
                domainExchange,
                deadLetterExchange,
                transactionReceivedQueue,
                deadLetterQueue,
                transactionReceivedBinding,
                deadLetterBinding);
    }
}
