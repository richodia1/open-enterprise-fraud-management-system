package org.openefms.explainableai.controller;

import org.openefms.common.models.explanation.ExplanationResponse;
import org.openefms.common.models.feedback.TransactionFraudFeedbackResponse;
import org.openefms.common.models.fraud.FraudDecisionResponse;
import org.openefms.common.models.intelligence.ManagementQuestionAnswerResponse;
import org.openefms.common.models.intelligence.ManagementQuestionRequest;
import org.openefms.explainableai.service.ExplanationOrchestrator;
import org.openefms.explainableai.service.ManagementQuestionAnswerService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/explanations")
public class FraudDecisionExplanationController {
    private final ExplanationOrchestrator orchestrator;
    private final ManagementQuestionAnswerService questionAnswerService;

    public FraudDecisionExplanationController(
            ExplanationOrchestrator orchestrator,
            ManagementQuestionAnswerService questionAnswerService) {
        this.orchestrator = orchestrator;
        this.questionAnswerService = questionAnswerService;
    }

    @PostMapping(path = "/fraud-decisions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ExplanationResponse explain(@RequestBody FraudDecisionResponse decision) {
        return orchestrator.explain(decision);
    }

    @PostMapping(path = "/fraud-decisions/feedback", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TransactionFraudFeedbackResponse feedback(@RequestBody FraudDecisionResponse decision) {
        return orchestrator.feedback(decision);
    }

    @PostMapping(path = "/management/questions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ManagementQuestionAnswerResponse answerManagementQuestion(@RequestBody ManagementQuestionRequest request) {
        return questionAnswerService.answer(request);
    }
}
