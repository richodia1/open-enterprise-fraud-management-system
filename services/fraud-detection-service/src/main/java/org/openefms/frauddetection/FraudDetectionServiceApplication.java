package org.openefms.frauddetection;

import java.time.Clock;
import org.openefms.frauddetection.decision.FraudDecisionProperties;
import org.openefms.frauddetection.messaging.FraudRabbitMqProperties;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableRabbit
@EnableConfigurationProperties({FraudDecisionProperties.class, FraudRabbitMqProperties.class})
public class FraudDetectionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FraudDetectionServiceApplication.class, args);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
