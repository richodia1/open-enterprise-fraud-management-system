package org.openefms.auditlogging.messaging;

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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "oefms.event-transport", havingValue = "rabbitmq", matchIfMissing = true)
public class RabbitMqAuditLoggingConfiguration {
    private final AuditLoggingRabbitMqProperties properties;

    public RabbitMqAuditLoggingConfiguration(AuditLoggingRabbitMqProperties properties) {
        this.properties = properties;
    }

    @Bean
    public MessageConverter rabbitJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public Declarables auditLoggingTopology() {
        TopicExchange domainExchange = ExchangeBuilder.topicExchange(properties.exchange()).durable(true).build();
        TopicExchange deadLetterExchange = ExchangeBuilder.topicExchange(properties.deadLetterExchange()).durable(true).build();
        Queue auditEventQueue = QueueBuilder
                .durable(RabbitMqQueues.AUDIT_LOGGING_AUDIT_EVENT_RECORDED)
                .withArgument("x-dead-letter-exchange", properties.deadLetterExchange())
                .build();
        Queue deadLetterQueue = QueueBuilder.durable(RabbitMqQueues.DEAD_LETTER).build();
        Binding auditEventBinding = BindingBuilder
                .bind(auditEventQueue)
                .to(domainExchange)
                .with(RabbitMqRoutingKeys.AUDIT_EVENT_RECORDED);
        Binding deadLetterBinding = BindingBuilder
                .bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with("#");

        return new Declarables(
                domainExchange,
                deadLetterExchange,
                auditEventQueue,
                deadLetterQueue,
                auditEventBinding,
                deadLetterBinding);
    }
}
