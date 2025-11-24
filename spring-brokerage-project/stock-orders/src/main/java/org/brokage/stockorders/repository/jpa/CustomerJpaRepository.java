package org.brokage.stockorders.repository.jpa;

import org.brokage.stockorders.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerJpaRepository extends JpaRepository<Customer, Long> {
}
