package org.brokage.stockorders.adapter.out.persistence.mapper;

import org.brokage.stockorders.adapter.out.persistence.entity.CustomerEntity;
import org.brokage.stockorders.adapter.out.persistence.entity.OrderEntity;
import org.brokage.stockorders.domain.model.customer.Customer;
import org.brokage.stockorders.domain.model.order.Order;
import org.springframework.stereotype.Component;

@Component
public class PersistenceOrderMapper {

    public OrderEntity toEntity(Order order) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(order.getId());
        orderEntity.setAssetName(order.getAssetName());
        orderEntity.setOrderSide(order.getOrderSide());
        orderEntity.setSize(order.getSize());
        orderEntity.setPrice(order.getPrice());
        orderEntity.setStatus(order.getStatus());
        orderEntity.setCreateDate(order.getCreateDate());
        orderEntity.setUpdateDate(order.getUpdateDate());

        // convert customer domain → customer JPA reference
        orderEntity.setCustomer(toCustomerReference(order.getCustomer()));

        return orderEntity;
    }

    private CustomerEntity toCustomerReference(Customer customer) {
        CustomerEntity ref = new CustomerEntity();
        ref.setId(customer.getId());
        return ref;
    }

    public Order toDomain(OrderEntity entity) {
        if (entity == null) return null;

        Order domain = new Order(
                toDomainCustomer(entity.getCustomer()),
                entity.getAssetName(),
                entity.getOrderSide(),
                entity.getSize(),
                entity.getPrice()
        );
        domain.setId(entity.getId());
        domain.setStatus(entity.getStatus());
        domain.setCreateDate(entity.getCreateDate());
        domain.setUpdateDate(entity.getUpdateDate());
        return domain;
    }

    private Customer toDomainCustomer(CustomerEntity entity) {
        Customer customer = new Customer(entity.getId());
        customer.setFirstName(entity.getFirstName());
        customer.setLastName(entity.getLastName());
        customer.setPhoneNumber(entity.getPhoneNumber());
        customer.setEmail(entity.getEmail());
        customer.setCreateDate(entity.getCreateDate());

        return customer;
    }
}
