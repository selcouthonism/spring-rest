package org.brokage.stockorders.application.port.out;


import org.brokage.stockorders.domain.model.customer.Customer;

public interface CustomerRepository {

    Customer findByIdOrThrow(Long id);

    Customer save(Customer customer);
}
