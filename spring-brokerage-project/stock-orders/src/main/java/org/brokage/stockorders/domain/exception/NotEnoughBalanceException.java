package org.brokage.stockorders.domain.exception;

import org.springframework.http.HttpStatus;

public class NotEnoughBalanceException extends DomainException {
    public NotEnoughBalanceException(String message) {

        super(message, "Not Enough Balance", HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
