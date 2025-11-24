package org.brokage.stockorders.repository;

import org.brokage.stockorders.model.entity.Customer;
import org.brokage.stockorders.repository.jpa.CustomerJpaRepository;
import org.brokage.stockorders.repository.jpa.UserCredentialJpaRepository;
import org.brokage.stockorders.security.UserCredentials;
import org.brokage.stockorders.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;


import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserCredentialsRepositoryTest {

    @Autowired
    private UserCredentialJpaRepository credentialRepository;

    @Autowired
    private CustomerJpaRepository customerJpaRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = customerJpaRepository.save(Customer.of("testUser", "lastName"));
    }

    @Test
    void saveAndFindUserCredential() {
        UserCredentials credential = UserCredentials.of(customer.getId(), "username", "password", Role.CUSTOMER, true);

        UserCredentials saved = credentialRepository.save(credential);
        UserCredentials found = credentialRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getUsername()).isEqualTo("username");
        assertThat(found.getPasswordHash()).isEqualTo("password");
        assertThat(found.getCustomerId()).isEqualTo(customer.getId());
    }
}
