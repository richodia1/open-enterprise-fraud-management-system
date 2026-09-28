package org.openefms.explainableai.messaging;

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
public class RabbitMqConsumerConfiguration {
    private final ExplainableAiRabbitMqProperties properties;

    public RabbitMqConsumerConfiguration(ExplainableAiRabbitMqProperties properties) {
        this.properties = properties;
    }

    @Bean
    public MessageConverter rabbitJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public Declarables explainableAiFraudDecisionTopology() {
        TopicExchange domainExchange = ExchangeBuilder.topicExchange(properties.exchange()).durable(true).build();
        TopicExchange deadLetterExchange = ExchangeBuilder.topicExchange(properties.deadLetterExchange()).durable(true).build();
        Queue fraudDecisionQueue = QueueBuilder
                .durable(RabbitMqQueues.EXPLAINABLE_AI_FRAUD_DECISION_PRODUCED)
                .withArgument("x-dead-letter-exchange", properties.deadLetterExchange())
                .build();
        Queue transactionCoreFeedbackQueue = QueueBuilder
                .durable(RabbitMqQueues.TRANSACTION_CORE_FRAUD_FEEDBACK_READY)
                .withArgument("x-dead-letter-exchange", properties.deadLetterExchange())
                .build();
        Queue deadLetterQueue = QueueBuilder.durable(RabbitMqQueues.DEAD_LETTER).build();
        Binding fraudDecisionBinding = BindingBuilder
                .bind(fraudDecisionQueue)
                .to(domainExchange)
                .with(RabbitMqRoutingKeys.FRAUD_DECISION_PRODUCED);
        Binding transactionCoreFeedbackBinding = BindingBuilder
                .bind(transactionCoreFeedbackQueue)
                .to(domainExchange)
                .with(RabbitMqRoutingKeys.FRAUD_FEEDBACK_READY);
        Binding deadLetterBinding = BindingBuilder
                .bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with("#");

        return new Declarables(
                domainExchange,
                deadLetterExchange,
                fraudDecisionQueue,
                transactionCoreFeedbackQueue,
                deadLetterQueue,
                fraudDecisionBinding,
                transactionCoreFeedbackBinding,
                deadLetterBinding);
    }
}
