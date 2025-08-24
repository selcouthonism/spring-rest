package org.brokage.stockorders.exceptions;

import org.springframework.http.HttpStatus;

public class UnallowedAccessException extends AppException {
    public UnallowedAccessException(String message) {

      super(message, "Unallowed access", HttpStatus.FORBIDDEN);
    }
}
