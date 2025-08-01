package org.springapi.shopping.exception.handler;

import org.springapi.shopping.exception.general.AppException;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<Problem> handleAppExceptions(AppException ex) {
        Problem problem = Problem.create()
                .withTitle(ex.getTitle())
                .withDetail(ex.getMessage())
                .withStatus(ex.getStatus());

        return ResponseEntity
                .status(ex.getStatus())
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                .body(problem);
    }

    //fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Problem> handleUnhandledExceptions(Exception ex) {
        Problem problem = Problem.create()
                .withTitle("Internal Server Error")
                .withDetail("An unexpected error occurred: " + ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                .body(problem);
    }
}
