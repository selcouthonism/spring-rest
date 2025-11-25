package org.brokage.stockorders.adapter.out.persistence.mapper;

import org.brokage.stockorders.adapter.out.persistence.entity.CustomerEntity;
import org.brokage.stockorders.domain.model.customer.Customer;
import org.springframework.stereotype.Component;

@Component
public class PersistenceCustomerMapper {
    public CustomerEntity toEntity(Customer customer) {
        CustomerEntity entity = new CustomerEntity();
        entity.setId(customer.getId());
        entity.setFirstName(customer.getFirstName());
        entity.setLastName(customer.getLastName());
        entity.setEmail(customer.getEmail());
        entity.setPhoneNumber(customer.getPhoneNumber());
        entity.setCreateDate(customer.getCreateDate());

        return entity;
    }

    public Customer toDomain(CustomerEntity entity) {
        if (entity == null) return null;

        Customer domain = new Customer(entity.getId());
        domain.setFirstName(entity.getFirstName());
        domain.setLastName(entity.getLastName());
        domain.setPhoneNumber(entity.getPhoneNumber());
        domain.setEmail(entity.getEmail());
        domain.setCreateDate(entity.getCreateDate());

        return domain;
    }

}
