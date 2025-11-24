package org.brokage.stockorders.application.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String message) {
        super(message, "Resource Not Found", HttpStatus.NOT_FOUND);
    }
}
