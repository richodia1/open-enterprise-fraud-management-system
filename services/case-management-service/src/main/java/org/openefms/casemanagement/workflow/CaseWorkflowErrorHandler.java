package org.openefms.casemanagement.workflow;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class CaseWorkflowErrorHandler {
    @ExceptionHandler(CaseWorkflowRecordNotFoundException.class)
    ResponseEntity<ProblemDetail> handleRecordNotFound(CaseWorkflowRecordNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Case workflow record was not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(InvalidCaseWorkflowQueryException.class)
    ResponseEntity<ProblemDetail> handleInvalidQuery(InvalidCaseWorkflowQueryException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Invalid case workflow query");
        problem.setProperty("errors", exception.errors());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(CaseWorkflowPersistenceException.class)
    ResponseEntity<ProblemDetail> handlePersistenceFailure(CaseWorkflowPersistenceException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Case workflow records could not be persisted or read.");
        problem.setTitle("Case workflow persistence failed");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
