package org.brokage.stockorders.repository;

import org.brokage.stockorders.model.entity.Customer;

public interface CustomerRepository {

    Customer findByIdOrThrow(Long id);

    Customer save(Customer customer);
}
