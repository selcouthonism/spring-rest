package org.brokage.stockorders.repository;

import org.brokage.stockorders.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Finds a customer by their unique username.
     * This will be essential for the authentication process.
     *
     * @param username the username to search for.
     * @return an Optional containing the customer if found.
     */
    Optional<Customer> findByUsername(String username);

    boolean existsByUsername(String username);
}
