package org.openefms.frauddetection.ml;

import java.time.Clock;
import org.openefms.frauddetection.decision.FraudDecisionProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class MlScoringConfiguration {

    @Bean
    @ConditionalOnMissingBean(MlScoringClient.class)
    MlScoringClient contractOnlyMlScoringClient(FraudDecisionProperties properties, Clock clock) {
        return new ContractOnlyMlScoringClient(properties, clock);
    }
}
