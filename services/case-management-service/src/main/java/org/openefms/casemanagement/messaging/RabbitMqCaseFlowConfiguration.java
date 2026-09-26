package org.openefms.casemanagement.messaging;

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
public class RabbitMqCaseFlowConfiguration {
    private final CaseManagementRabbitMqProperties properties;

    public RabbitMqCaseFlowConfiguration(CaseManagementRabbitMqProperties properties) {
        this.properties = properties;
    }

    @Bean
    public MessageConverter rabbitJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public Declarables caseManagementExplanationPreparedTopology() {
        TopicExchange domainExchange = ExchangeBuilder.topicExchange(properties.exchange()).durable(true).build();
        TopicExchange deadLetterExchange = ExchangeBuilder.topicExchange(properties.deadLetterExchange()).durable(true).build();
        Queue explanationPreparedQueue = QueueBuilder
                .durable(RabbitMqQueues.CASE_MANAGEMENT_EXPLANATION_PREPARED)
                .withArgument("x-dead-letter-exchange", properties.deadLetterExchange())
                .build();
        Queue deadLetterQueue = QueueBuilder.durable(RabbitMqQueues.DEAD_LETTER).build();
        Binding explanationPreparedBinding = BindingBuilder
                .bind(explanationPreparedQueue)
                .to(domainExchange)
                .with(RabbitMqRoutingKeys.EXPLANATION_PREPARED);
        Binding deadLetterBinding = BindingBuilder
                .bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with("#");

        return new Declarables(
                domainExchange,
                deadLetterExchange,
                explanationPreparedQueue,
                deadLetterQueue,
                explanationPreparedBinding,
                deadLetterBinding);
    }
}
