package org.brokage.stockorders.adapter.out.persistence.jpa;

import org.brokage.stockorders.adapter.out.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCustomerRepository extends JpaRepository<CustomerEntity, Long> {
}
