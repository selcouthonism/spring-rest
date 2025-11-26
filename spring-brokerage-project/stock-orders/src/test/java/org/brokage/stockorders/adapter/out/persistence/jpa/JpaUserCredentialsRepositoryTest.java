package org.brokage.stockorders.adapter.out.persistence.jpa;

import org.brokage.stockorders.adapter.out.persistence.entity.CustomerEntity;
import org.brokage.stockorders.adapter.out.persistence.jpa.JpaCustomerRepository;
import org.brokage.stockorders.adapter.out.persistence.jpa.JpaUserCredentialRepository;
import org.brokage.stockorders.adapter.out.persistence.entity.UserCredentials;
import org.brokage.stockorders.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;


import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class JpaUserCredentialsRepositoryTest {

    @Autowired
    private JpaUserCredentialRepository credentialRepository;

    @Autowired
    private JpaCustomerRepository jpaCustomerRepository;

    private CustomerEntity customer;

    @BeforeEach
    void setUp() {
        customer = jpaCustomerRepository.save(CustomerEntity.of("testUser", "lastName"));
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
