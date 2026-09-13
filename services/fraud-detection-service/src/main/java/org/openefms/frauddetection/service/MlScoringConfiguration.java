package org.openefms.frauddetection.service;

import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MlScoringConfiguration {

    @Bean
    @ConditionalOnMissingBean(MlScoringClient.class)
    public MlScoringClient contractOnlyMlScoringClient(FraudDecisionProperties properties, Clock clock) {
        return new ContractOnlyMlScoringClient(properties, clock);
    }
}
