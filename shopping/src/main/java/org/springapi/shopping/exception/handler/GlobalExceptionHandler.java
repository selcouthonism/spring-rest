package org.springapi.shopping.exception.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springapi.shopping.exception.general.AppException;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AppException.class)
    public ResponseEntity<Problem> handleAppExceptions(AppException ex) {
        log.error("Application error", ex);

        Problem problem = Problem.create()
                .withTitle(ex.getTitle())
                .withDetail(ex.getMessage())
                .withStatus(ex.getStatus());

        return ResponseEntity
                .status(ex.getStatus())
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                .body(problem);
    }

    //Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.error("Validation error", ex);

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(errors);
    }

    //fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Problem> handleUnhandledExceptions(Exception ex) {
        log.error("Unexpected error", ex);

        Problem problem = Problem.create()
                .withTitle("Internal Server Error")
                .withDetail("An unexpected error occurred.");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                .body(problem);
    }
}
