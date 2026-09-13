package org.openefms.explainableai;

import java.time.Clock;
import org.openefms.explainableai.messaging.ExplainableAiRabbitMqProperties;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableRabbit
@EnableConfigurationProperties(ExplainableAiRabbitMqProperties.class)
public class ExplainableAiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExplainableAiServiceApplication.class, args);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
