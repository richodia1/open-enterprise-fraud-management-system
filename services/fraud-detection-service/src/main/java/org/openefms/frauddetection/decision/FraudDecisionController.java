package org.openefms.frauddetection.decision;

import java.util.List;
import java.util.UUID;
import org.openefms.common.models.fraud.FraudDecisionResponse;
import org.openefms.common.models.transaction.TransactionIngestionRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping(path = "/{decisionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public FraudDecisionRecord decision(@PathVariable UUID decisionId) {
        return orchestrator.decision(decisionId);
    }

    @GetMapping(params = {"transactionId", "!tenantId"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FraudDecisionRecord> decisionsForTransaction(@RequestParam UUID transactionId) {
        return orchestrator.decisionsForTransaction(transactionId);
    }

    @GetMapping(params = {"tenantId", "!transactionId"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FraudDecisionRecord> decisionsForTenant(@RequestParam String tenantId) {
        return orchestrator.decisionsForTenant(tenantId);
    }
}
