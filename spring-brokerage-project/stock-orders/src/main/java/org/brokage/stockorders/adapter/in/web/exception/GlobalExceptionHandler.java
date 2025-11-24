package org.brokage.stockorders.adapter.in.web.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ValidationException;
import org.brokage.stockorders.application.exception.AppException;
import org.brokage.stockorders.domain.exception.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ProblemDetail buildProblemDetail(String title, String detail, HttpStatus status) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problem.setTitle(title);
        //problem.setType(URI.create("https://example.com/errors/forbidden"));
        problem.setDetail(detail);
        problem.setStatus(status);

        return problem;
    }

    private ResponseEntity<Problem> buildProblem(String title, String detail, HttpStatus status, Exception exception) {
        log.warn("Exception caught in GlobalExceptionHandler", exception.getMessage());

        Problem problem = Problem.create()
                .withTitle(title)
                .withDetail(detail)
                .withStatus(status);

        return ResponseEntity
                .status(status.value())
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                .body(problem);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<Problem> handleAppExceptions(AppException ex) {
        return buildProblem(ex.getTitle(), ex.getMessage(), ex.getStatus(), ex);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Problem> handleDomainExceptions(AppException ex) {
        return buildProblem(ex.getTitle(), ex.getMessage(), ex.getStatus(), ex);
    }

    //Validation
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Problem> handleValidationException(ValidationException ex) {
        return buildProblem("Validation error", ex.getMessage(), HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Problem> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return buildProblem("Validation error",errors.toString(), HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Problem> handleEnumConversionError(MethodArgumentTypeMismatchException ex) {

        Map<String, String> errors = new HashMap<>();
        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            String allowedValues = Arrays.stream(ex.getRequiredType().getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));

            errors = Map.of(ex.getName() +":", "Invalid value '" + ex.getValue() + "'. Allowed values: " + allowedValues);
        }

        return buildProblem("Invalid request parameter", errors.toString(), HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException invalidFormatEx) {
            Map<String, String> errors = new HashMap<>();

            String field = invalidFormatEx.getPath().get(0).getFieldName();
            Class<?> targetType = invalidFormatEx.getTargetType();
            String allowedValues = targetType.isEnum() ?
                    Arrays.stream(targetType.getEnumConstants())
                            .map(Object::toString)
                            .collect(Collectors.joining(", "))
                    : "unknown";

            errors.put(field, "Invalid value. Allowed values: " + allowedValues);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        // fallback for other malformed JSON
        Map<String, String> fallback = Map.of("error", "Malformed JSON request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(fallback);
    }


    /*
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Problem> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return buildProblem("Message Not Readable error", "Malformed JSON request", HttpStatus.BAD_REQUEST, ex);
    }
     */


    // 🔹 Security
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Problem> handleAccessDenied(AccessDeniedException ex) {
        return buildProblem("Access denied", "You do not have permission to access this resource.", HttpStatus.FORBIDDEN, ex);
    }

    //fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Problem> handleUnhandledExceptions(Exception ex) {
        log.error("Unexpected error", ex);
        return buildProblem("Unhandled exception", "An unexpected internal server error occurred.", HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }

    /*
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnhandledExceptions(Exception ex) {
        log.error("Unexpected error", ex);

        return buildProblemDetail("Unhandled exception", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    */


}
