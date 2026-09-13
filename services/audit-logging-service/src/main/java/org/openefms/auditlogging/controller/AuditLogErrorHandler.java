package org.openefms.auditlogging.controller;

import org.openefms.auditlogging.service.AuditLogPersistenceException;
import org.openefms.auditlogging.service.AuditLogRecordNotFoundException;
import org.openefms.auditlogging.service.InvalidAuditLogQueryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuditLogErrorHandler {
    @ExceptionHandler(AuditLogRecordNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleRecordNotFound(AuditLogRecordNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Audit event record was not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(InvalidAuditLogQueryException.class)
    public ResponseEntity<ProblemDetail> handleInvalidQuery(InvalidAuditLogQueryException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Invalid audit log query");
        problem.setProperty("errors", exception.errors());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(AuditLogPersistenceException.class)
    public ResponseEntity<ProblemDetail> handlePersistenceFailure(AuditLogPersistenceException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Audit event records could not be persisted or read.");
        problem.setTitle("Audit log persistence failed");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
