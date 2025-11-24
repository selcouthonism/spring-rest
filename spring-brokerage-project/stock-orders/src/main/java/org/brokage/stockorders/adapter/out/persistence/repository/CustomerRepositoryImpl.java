package org.brokage.stockorders.adapter.out.persistence.repository;

import org.brokage.stockorders.application.exception.ResourceNotFoundException;
import org.brokage.stockorders.adapter.out.persistence.entity.Customer;
import org.brokage.stockorders.application.port.out.CustomerRepository;
import org.brokage.stockorders.adapter.out.persistence.jpa.JpaCustomerRepository;
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
