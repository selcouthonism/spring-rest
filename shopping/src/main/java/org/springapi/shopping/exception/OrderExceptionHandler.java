package org.springapi.shopping.exception;

import org.springapi.shopping.model.Order;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class OrderExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String orderNotFoundHandler(OrderNotFoundException ex) {
        return ex.getMessage();
    }

    /*
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Problem> handleNotFound(OrderNotFoundException ex) {
        Problem problem = Problem.create()
                .withTitle("Order not found")
                .withDetail(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                .body(problem);
    }
     */

    @ExceptionHandler(OrderStatusException.class)
    public ResponseEntity<Problem> handleOrderStatus(OrderStatusException ex) {
        Order order = ex.getOrder();
        String action = ex.getAction();

        Problem problem = Problem.create() //
                .withTitle("Method not allowed") //
                .withDetail("You can't " + action + " an order that is in the " + order.getStatus() + " status");

        return ResponseEntity //
                .status(HttpStatus.METHOD_NOT_ALLOWED) //
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE) //
                .body(problem);
    }

}
