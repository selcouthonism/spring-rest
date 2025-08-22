package org.springapi.shopping.exception.specific;

import org.springapi.shopping.exception.general.AppException;
import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends AppException {

    public OrderNotFoundException(Long id) {
        super("Order with ID " + id + " not found", "Order Not Found", HttpStatus.NOT_FOUND);
    }
}
