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

    @ExceptionHandler(FraudDecisionRecordNotFoundException.class)
    ResponseEntity<ProblemDetail> handleDecisionNotFound(FraudDecisionRecordNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Fraud decision record was not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(InvalidFraudDecisionQueryException.class)
    ResponseEntity<ProblemDetail> handleInvalidQuery(InvalidFraudDecisionQueryException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Invalid fraud decision query");
        problem.setProperty("errors", exception.errors());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(FraudDecisionPersistenceException.class)
    ResponseEntity<ProblemDetail> handlePersistenceFailure(FraudDecisionPersistenceException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        problem.setTitle("Fraud decision record could not be stored");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
