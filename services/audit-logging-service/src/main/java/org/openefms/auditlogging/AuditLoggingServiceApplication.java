package org.openefms.auditlogging;

import org.openefms.auditlogging.messaging.AuditLoggingRabbitMqProperties;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableRabbit
@EnableConfigurationProperties(AuditLoggingRabbitMqProperties.class)
public class AuditLoggingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditLoggingServiceApplication.class, args);
    }
}
