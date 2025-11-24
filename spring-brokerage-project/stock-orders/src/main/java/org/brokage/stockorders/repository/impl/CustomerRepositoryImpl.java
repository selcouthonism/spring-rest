package org.brokage.stockorders.repository.impl;

import org.brokage.stockorders.exceptions.ResourceNotFoundException;
import org.brokage.stockorders.model.entity.Customer;
import org.brokage.stockorders.repository.CustomerRepository;
import org.brokage.stockorders.repository.jpa.CustomerJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class CustomerRepositoryImpl implements CustomerRepository {

    private final CustomerJpaRepository customerJpaRepository;

    public CustomerRepositoryImpl(CustomerJpaRepository customerJpaRepository) {
        this.customerJpaRepository = customerJpaRepository;
    }

    @Override
    public Customer findByIdOrThrow(Long customerId) {
        return customerJpaRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found."));
    }

    @Override
    public Customer save(Customer customer) {
        return customerJpaRepository.save(customer);
    }
}
