package org.brokage.stockorders.application.exception;

import org.springframework.http.HttpStatus;

public class OperationNotPermittedException extends AppException {
    public OperationNotPermittedException(String message) {
        super(message, "Operation Not Permitted", HttpStatus.CONFLICT);
    }
}
