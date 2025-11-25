package org.brokage.stockorders.adapter.out.persistence.jpa;

import org.brokage.stockorders.adapter.out.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface JpaOrderRepository extends JpaRepository<OrderEntity, Long>, JpaSpecificationExecutor<OrderEntity> {

    Optional<OrderEntity> findByIdAndCustomerId(Long orderId, Long customerId);
    boolean existsByIdAndCustomerId(Long orderId, Long customerId);
}
