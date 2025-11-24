package org.brokage.stockorders.repository.jpa;

import org.brokage.stockorders.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JpaOrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    boolean existsByIdAndCustomerId(Long orderId, Long customerId);
}
