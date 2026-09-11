package org.openefms.frauddetection.decision;

import org.openefms.frauddetection.ml.MlScoringUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class FraudDecisionErrorHandler {
    @ExceptionHandler(UnsupportedFraudDecisionModeException.class)
    ResponseEntity<ProblemDetail> handleUnsupportedMode(UnsupportedFraudDecisionModeException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_IMPLEMENTED, exception.getMessage());
        problem.setTitle("Fraud decision mode is not available");
        problem.setProperty("mode", exception.mode().name());
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(problem);
    }

    @ExceptionHandler(MlScoringUnavailableException.class)
    ResponseEntity<ProblemDetail> handleMlUnavailable(MlScoringUnavailableException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_IMPLEMENTED, exception.getMessage());
        problem.setTitle("ML scoring runtime is not available");
        problem.setProperty("provider", exception.provider());
        problem.setProperty("modelName", exception.modelName());
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(problem);
    }
}
