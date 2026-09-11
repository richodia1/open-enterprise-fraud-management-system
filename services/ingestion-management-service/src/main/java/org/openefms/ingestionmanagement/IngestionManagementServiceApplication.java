package org.openefms.ingestionmanagement;

import java.time.Clock;
import org.openefms.ingestionmanagement.messaging.IngestionRabbitMqProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(IngestionRabbitMqProperties.class)
public class IngestionManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IngestionManagementServiceApplication.class, args);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
