package org.brokage.stockorders.adapter.out.persistence.repository;

import org.brokage.stockorders.adapter.out.persistence.entity.CustomerEntity;
import org.brokage.stockorders.adapter.out.persistence.mapper.PersistenceCustomerMapper;
import org.brokage.stockorders.application.exception.ResourceNotFoundException;
import org.brokage.stockorders.application.port.out.CustomerRepository;
import org.brokage.stockorders.adapter.out.persistence.jpa.JpaCustomerRepository;
import org.brokage.stockorders.domain.model.customer.Customer;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerRepositoryImpl implements CustomerRepository {

    private final JpaCustomerRepository jpaCustomerRepository;
    private final PersistenceCustomerMapper mapper;

    public CustomerRepositoryImpl(JpaCustomerRepository jpaCustomerRepository, PersistenceCustomerMapper mapper) {
        this.jpaCustomerRepository = jpaCustomerRepository;
        this.mapper = mapper;
    }

    @Override
    public Customer findByIdOrThrow(Long customerId) {
        CustomerEntity entity = jpaCustomerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found."));

        return mapper.toDomain(entity);
    }

    @Override
    public Customer save(Customer customer) {
        CustomerEntity newEntity = mapper.toEntity(customer);
        CustomerEntity savedEntity = jpaCustomerRepository.save(newEntity);
        return mapper.toDomain(savedEntity);
    }
}
