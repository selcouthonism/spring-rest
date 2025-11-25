package org.brokage.stockorders.adapter.out.persistence.specification;

import org.brokage.stockorders.adapter.out.persistence.entity.OrderEntity;
import org.brokage.stockorders.domain.model.order.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class OrderSpecifications {

    public static Specification<OrderEntity> createdAfter(Instant from) {
        return (root, query, cb) -> from != null
                ? cb.greaterThanOrEqualTo(root.get("createDate"), from)
                : null;
    }

    public static Specification<OrderEntity> createdBefore(Instant to) {
        return (root, query, cb) -> to != null
                ? cb.lessThanOrEqualTo(root.get("createDate"), to)
                : null;
    }

    public static Specification<OrderEntity> hasCustomerId(Long customerId) {
        return (root, query, cb) -> customerId != null
                ? cb.equal(root.get("customer").get("id"), customerId)
                : null;
    }

    public static Specification<OrderEntity> hasStatus(OrderStatus status) {
        return (root, query, cb) -> status != null
                ? cb.equal(root.get("status"), status)
                : null;
    }
}
