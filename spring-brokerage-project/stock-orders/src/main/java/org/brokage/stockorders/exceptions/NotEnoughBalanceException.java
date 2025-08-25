package org.brokage.stockorders.exceptions;

import org.springframework.http.HttpStatus;

public class NotEnoughBalanceException extends AppException {
    public NotEnoughBalanceException(String message) {

        super(message, "Not Enough Balance", HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
