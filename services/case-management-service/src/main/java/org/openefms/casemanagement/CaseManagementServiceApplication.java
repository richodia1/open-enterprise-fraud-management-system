package org.openefms.casemanagement;

import java.time.Clock;
import org.openefms.casemanagement.client.CaseIdentityClientProperties;
import org.openefms.casemanagement.messaging.CaseManagementRabbitMqProperties;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableRabbit
@EnableConfigurationProperties({CaseManagementRabbitMqProperties.class, CaseIdentityClientProperties.class})
public class CaseManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaseManagementServiceApplication.class, args);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
