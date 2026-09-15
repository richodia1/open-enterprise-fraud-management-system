package org.openefms.casemanagement.controller;

import org.openefms.casemanagement.client.CaseAssignmentDirectoryUnavailableException;
import org.openefms.casemanagement.service.CaseWorkflowPersistenceException;
import org.openefms.casemanagement.service.CaseWorkflowRecordNotFoundException;
import org.openefms.casemanagement.service.InvalidCaseLifecycleActionException;
import org.openefms.casemanagement.service.InvalidCaseWorkflowQueryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CaseWorkflowErrorHandler {
    @ExceptionHandler(CaseWorkflowRecordNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleRecordNotFound(CaseWorkflowRecordNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Case workflow record was not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(InvalidCaseWorkflowQueryException.class)
    public ResponseEntity<ProblemDetail> handleInvalidQuery(InvalidCaseWorkflowQueryException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Invalid case workflow query");
        problem.setProperty("errors", exception.errors());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(InvalidCaseLifecycleActionException.class)
    public ResponseEntity<ProblemDetail> handleInvalidLifecycleAction(InvalidCaseLifecycleActionException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Invalid case lifecycle action");
        problem.setProperty("errors", exception.errors());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(CaseAssignmentDirectoryUnavailableException.class)
    public ResponseEntity<ProblemDetail> handleAssignmentDirectoryUnavailable(
            CaseAssignmentDirectoryUnavailableException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getMessage());
        problem.setTitle("Case assignment directory is unavailable");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem);
    }

    @ExceptionHandler(CaseWorkflowPersistenceException.class)
    public ResponseEntity<ProblemDetail> handlePersistenceFailure(CaseWorkflowPersistenceException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Case workflow records could not be persisted or read.");
        problem.setTitle("Case workflow persistence failed");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
