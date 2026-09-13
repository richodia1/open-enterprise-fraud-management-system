package org.openefms.rulemanagement.rule;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class RuleManagementErrorHandler {
    @ExceptionHandler(InvalidRuleDefinitionException.class)
    ResponseEntity<ProblemDetail> handleInvalidRequest(InvalidRuleDefinitionException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Rule definition request is invalid");
        problem.setProperty("errors", exception.errors());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(RuleDefinitionNotFoundException.class)
    ResponseEntity<ProblemDetail> handleNotFound(RuleDefinitionNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Rule definition was not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(RuleDefinitionConflictException.class)
    ResponseEntity<ProblemDetail> handleConflict(RuleDefinitionConflictException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setTitle("Rule definition already exists");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(RuleManagementResourceNotFoundException.class)
    ResponseEntity<ProblemDetail> handleRuleManagementResourceNotFound(RuleManagementResourceNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Rule management resource was not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(RuleManagementResourceConflictException.class)
    ResponseEntity<ProblemDetail> handleRuleManagementResourceConflict(RuleManagementResourceConflictException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setTitle("Rule management resource already exists");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ProblemDetail> handleUnreadableMessage(HttpMessageNotReadableException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request body must be valid JSON matching the rule management contract.");
        problem.setTitle("Rule management request body is invalid");
        return ResponseEntity.badRequest().body(problem);
    }
}
