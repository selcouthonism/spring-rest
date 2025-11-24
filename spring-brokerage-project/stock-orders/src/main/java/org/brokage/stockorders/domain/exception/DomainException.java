package org.brokage.stockorders.domain.exception;

import org.springframework.http.HttpStatus;

public class DomainException extends RuntimeException {

    private final String title;
    private final HttpStatus status;

    public DomainException(String message, String title, HttpStatus status) {
        super(message);
        this.title = title;
        this.status = status;
    }

    public DomainException(String message, Throwable cause, String title, HttpStatus status) {
        super(message, cause);
        this.title = title;
        this.status = status;
    }
}
