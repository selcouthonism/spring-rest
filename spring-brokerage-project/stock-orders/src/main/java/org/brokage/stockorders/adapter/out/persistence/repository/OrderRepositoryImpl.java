package org.brokage.stockorders.adapter.out.persistence.repository;

import org.brokage.stockorders.adapter.out.persistence.mapper.PersistenceOrderMapper;
import org.brokage.stockorders.adapter.out.persistence.entity.OrderEntity;
import org.brokage.stockorders.adapter.out.persistence.jpa.JpaOrderRepository;
import org.brokage.stockorders.adapter.out.persistence.specification.OrderSpecifications;
import org.brokage.stockorders.application.exception.ResourceNotFoundException;
import org.brokage.stockorders.application.port.out.OrderRepository;
import org.brokage.stockorders.domain.model.order.Order;
import org.brokage.stockorders.domain.model.order.OrderStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final JpaOrderRepository repository;
    private final PersistenceOrderMapper mapper;

    public OrderRepositoryImpl(JpaOrderRepository repository, PersistenceOrderMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }


    @Override
    public Order save(Order order) {
        OrderEntity newEntity = mapper.toEntity(order);
        OrderEntity savedEntity = repository.save(newEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Order findById(Long orderId) {
        OrderEntity entity = repository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found."));

        return mapper.toDomain(entity);
    }

    @Override
    public Order findByIdAndCustomerId(Long orderId, Long customerId) {
        OrderEntity entity = repository.findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found."));

        return mapper.toDomain(entity);
    }

    @Override
    public List<Order> findByFilter(Long customerId, Instant from, Instant to, OrderStatus orderStatus) {

        //allOf(...) → AND
        //anyOf(...) → OR
        Specification<OrderEntity> spec = Specification.allOf(
                OrderSpecifications.createdAfter(from),
                OrderSpecifications.createdBefore(to),
                OrderSpecifications.hasCustomerId(customerId),
                OrderSpecifications.hasStatus(orderStatus)
        );

        List<Order> list = repository.findAll(spec).stream()
                .map(mapper::toDomain)
                .toList();

        return list;
    }
}
