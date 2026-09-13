package org.openefms.rulemanagement.controller;

import org.openefms.rulemanagement.service.InvalidRuleDefinitionException;
import org.openefms.rulemanagement.service.RuleDefinitionConflictException;
import org.openefms.rulemanagement.service.RuleDefinitionNotFoundException;
import org.openefms.rulemanagement.service.RuleManagementResourceConflictException;
import org.openefms.rulemanagement.service.RuleManagementResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RuleManagementErrorHandler {
    @ExceptionHandler(InvalidRuleDefinitionException.class)
    public ResponseEntity<ProblemDetail> handleInvalidRequest(InvalidRuleDefinitionException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Rule definition request is invalid");
        problem.setProperty("errors", exception.errors());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(RuleDefinitionNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(RuleDefinitionNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Rule definition was not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(RuleDefinitionConflictException.class)
    public ResponseEntity<ProblemDetail> handleConflict(RuleDefinitionConflictException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setTitle("Rule definition already exists");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(RuleManagementResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleRuleManagementResourceNotFound(RuleManagementResourceNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Rule management resource was not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(RuleManagementResourceConflictException.class)
    public ResponseEntity<ProblemDetail> handleRuleManagementResourceConflict(RuleManagementResourceConflictException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setTitle("Rule management resource already exists");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadableMessage(HttpMessageNotReadableException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request body must be valid JSON matching the rule management contract.");
        problem.setTitle("Rule management request body is invalid");
        return ResponseEntity.badRequest().body(problem);
    }
}
