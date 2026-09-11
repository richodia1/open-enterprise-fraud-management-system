package org.openefms.ingestionmanagement.transaction;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class TransactionIngestionErrorHandler {
    @ExceptionHandler(InvalidTransactionIngestionRequestException.class)
    ResponseEntity<ProblemDetail> handleInvalidRequest(InvalidTransactionIngestionRequestException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Transaction ingestion request is invalid");
        problem.setProperty("errors", exception.errors());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ProblemDetail> handleUnreadableMessage(HttpMessageNotReadableException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request body must be valid JSON matching the transaction ingestion contract.");
        problem.setTitle("Transaction ingestion request body is invalid");
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(TransactionEventPublicationException.class)
    ResponseEntity<ProblemDetail> handlePublicationFailure(TransactionEventPublicationException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Transaction was accepted by the API layer but could not be published to the event broker.");
        problem.setTitle("Transaction event publication failed");
        problem.setProperty("eventType", exception.eventType());
        problem.setProperty("exchange", exception.exchangeName());
        problem.setProperty("routingKey", exception.routingKey());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem);
    }
}
