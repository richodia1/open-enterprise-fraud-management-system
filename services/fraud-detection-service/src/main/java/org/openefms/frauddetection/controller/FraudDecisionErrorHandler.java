package org.openefms.frauddetection.controller;

import org.openefms.frauddetection.service.FraudDecisionPersistenceException;
import org.openefms.frauddetection.service.FraudDecisionRecordNotFoundException;
import org.openefms.frauddetection.service.InvalidFraudDecisionQueryException;
import org.openefms.frauddetection.service.MlScoringUnavailableException;
import org.openefms.frauddetection.service.UnsupportedFraudDecisionModeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class FraudDecisionErrorHandler {
    @ExceptionHandler(UnsupportedFraudDecisionModeException.class)
    public ResponseEntity<ProblemDetail> handleUnsupportedMode(UnsupportedFraudDecisionModeException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_IMPLEMENTED, exception.getMessage());
        problem.setTitle("Fraud decision mode is not available");
        problem.setProperty("mode", exception.mode().name());
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(problem);
    }

    @ExceptionHandler(MlScoringUnavailableException.class)
    public ResponseEntity<ProblemDetail> handleMlUnavailable(MlScoringUnavailableException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_IMPLEMENTED, exception.getMessage());
        problem.setTitle("ML scoring runtime is not available");
        problem.setProperty("provider", exception.provider());
        problem.setProperty("modelName", exception.modelName());
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(problem);
    }

    @ExceptionHandler(FraudDecisionRecordNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleDecisionNotFound(FraudDecisionRecordNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Fraud decision record was not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(InvalidFraudDecisionQueryException.class)
    public ResponseEntity<ProblemDetail> handleInvalidQuery(InvalidFraudDecisionQueryException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Invalid fraud decision query");
        problem.setProperty("errors", exception.errors());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(FraudDecisionPersistenceException.class)
    public ResponseEntity<ProblemDetail> handlePersistenceFailure(FraudDecisionPersistenceException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        problem.setTitle("Fraud decision record could not be stored");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
