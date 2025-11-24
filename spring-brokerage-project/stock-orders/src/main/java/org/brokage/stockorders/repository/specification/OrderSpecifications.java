package org.brokage.stockorders.repository.specification;

import org.brokage.stockorders.model.entity.Order;
import org.brokage.stockorders.model.enums.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class OrderSpecifications {

    public static Specification<Order> createdAfter(Instant from) {
        return (root, query, cb) -> from != null
                ? cb.greaterThanOrEqualTo(root.get("createDate"), from)
                : null;
    }

    public static Specification<Order> createdBefore(Instant to) {
        return (root, query, cb) -> to != null
                ? cb.lessThanOrEqualTo(root.get("createDate"), to)
                : null;
    }

    public static Specification<Order> hasCustomerId(Long customerId) {
        return (root, query, cb) -> customerId != null
                ? cb.equal(root.get("customer").get("id"), customerId)
                : null;
    }

    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, cb) -> status != null
                ? cb.equal(root.get("status"), status)
                : null;
    }
}
