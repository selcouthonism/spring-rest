package org.brokage.stockorders.adapter.out.persistence.jpa;

import org.brokage.stockorders.adapter.out.persistence.entity.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaUserCredentialRepository extends JpaRepository<UserCredentials, Long> {

    /**
     * Finds a user credentials by their unique username.
     * This will be essential for the authentication process.
     *
     * @param username the username to search for.
     * @return an Optional containing the customer if found.
     */
    Optional<UserCredentials> findByUsername(String username);
}
