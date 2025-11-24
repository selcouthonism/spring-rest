package org.brokage.stockorders.repository.impl;

import org.brokage.stockorders.exceptions.ResourceNotFoundException;
import org.brokage.stockorders.model.entity.Customer;
import org.brokage.stockorders.repository.CustomerRepository;
import org.brokage.stockorders.repository.jpa.JpaCustomerRepository;
import org.springframework.stereotype.Component;

@Component
public class CustomerRepositoryImpl implements CustomerRepository {

    private final JpaCustomerRepository jpaCustomerRepository;

    public CustomerRepositoryImpl(JpaCustomerRepository jpaCustomerRepository) {
        this.jpaCustomerRepository = jpaCustomerRepository;
    }

    @Override
    public Customer findByIdOrThrow(Long customerId) {
        return jpaCustomerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found."));
    }

    @Override
    public Customer save(Customer customer) {
        return jpaCustomerRepository.save(customer);
    }
}
