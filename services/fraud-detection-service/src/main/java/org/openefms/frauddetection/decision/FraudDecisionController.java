package org.openefms.frauddetection.decision;

import org.openefms.common.models.fraud.FraudDecisionResponse;
import org.openefms.common.models.transaction.TransactionIngestionRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/fraud/decisions")
public class FraudDecisionController {
    private final FraudDecisionOrchestrator orchestrator;

    public FraudDecisionController(FraudDecisionOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public FraudDecisionResponse decide(@RequestBody TransactionIngestionRequest transaction) {
        return orchestrator.decide(transaction);
    }
}
