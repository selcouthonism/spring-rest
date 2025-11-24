package org.brokage.stockorders.application.port.out;

import org.brokage.stockorders.adapter.out.persistence.entity.Customer;

public interface CustomerRepository {

    Customer findByIdOrThrow(Long id);

    Customer save(Customer customer);
}
