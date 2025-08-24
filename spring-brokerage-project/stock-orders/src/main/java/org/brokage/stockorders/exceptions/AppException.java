package org.brokage.stockorders.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {

    private final String title;
    private final HttpStatus status;

    public AppException(String message, String title, HttpStatus status) {
        super(message);
        this.title = title;
        this.status = status;
    }

}
