package org.openefms.frauddetection;

import java.time.Clock;
import org.openefms.frauddetection.messaging.FraudRabbitMqProperties;
import org.openefms.frauddetection.service.FraudDecisionProperties;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableRabbit
@EnableKafka
@EnableConfigurationProperties({FraudDecisionProperties.class, FraudRabbitMqProperties.class})
public class FraudDetectionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FraudDetectionServiceApplication.class, args);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
