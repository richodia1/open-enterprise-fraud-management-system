package org.openefms.configurationmanagement.controller;

import org.openefms.common.models.management.ManagedResourceConflictException;
import org.openefms.common.models.management.ManagedResourceNotFoundException;
import org.openefms.common.models.management.ManagedResourceValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ConfigurationResourceErrorHandler {
    @ExceptionHandler(ManagedResourceValidationException.class)
    public ResponseEntity<ProblemDetail> handleInvalidRequest(ManagedResourceValidationException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Configuration resource request is invalid");
        problem.setProperty("errors", exception.errors());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(ManagedResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ManagedResourceNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Configuration resource was not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(ManagedResourceConflictException.class)
    public ResponseEntity<ProblemDetail> handleConflict(ManagedResourceConflictException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setTitle("Configuration resource already exists");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadableMessage(HttpMessageNotReadableException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request body must be valid JSON matching the configuration resource contract.");
        problem.setTitle("Configuration resource request body is invalid");
        return ResponseEntity.badRequest().body(problem);
    }
}
