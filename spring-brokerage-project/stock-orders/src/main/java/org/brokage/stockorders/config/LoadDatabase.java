package org.brokage.stockorders.config;

import org.brokage.stockorders.model.entity.Asset;
import org.brokage.stockorders.model.entity.Customer;
import org.brokage.stockorders.model.entity.Order;
import org.brokage.stockorders.model.enums.OrderSide;
import org.brokage.stockorders.model.enums.OrderStatus;
import org.brokage.stockorders.model.enums.Role;
import org.brokage.stockorders.repository.jpa.JpaAssetRepository;
import org.brokage.stockorders.repository.jpa.JpaCustomerRepository;
import org.brokage.stockorders.repository.jpa.JpaOrderRepository;
import org.brokage.stockorders.repository.jpa.JpaUserCredentialRepository;
import org.brokage.stockorders.security.UserCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class LoadDatabase {
    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    //@Profile("test")
    CommandLineRunner initDatabase(JpaCustomerRepository jpaCustomerRepository, JpaAssetRepository jpaAssetRepository, JpaOrderRepository jpaOrderRepository, JpaUserCredentialRepository credentialRepository, JpaUserCredentialRepository jpaUserCredentialRepository) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return args -> {

            Customer customer1 = jpaCustomerRepository.save(Customer.of("customer1FN", "lastName1"));
            Customer customer2 = jpaCustomerRepository.save(Customer.of("customer2FN", "lastName1"));
            Customer customer3 = jpaCustomerRepository.save(Customer.of("customer3FN", "lastName1"));
            Customer admin1 = jpaCustomerRepository.save(Customer.of("admin1FN", "lastName1"));

            credentialRepository.save(UserCredentials.of(customer1.getId(), "customer1", encoder.encode("password1"), Role.CUSTOMER, true));
            credentialRepository.save(UserCredentials.of(customer2.getId(), "customer2", encoder.encode("password2"), Role.CUSTOMER, true));
            credentialRepository.save(UserCredentials.of(customer3.getId(), "customer3", encoder.encode("password3"), Role.CUSTOMER, true));
            credentialRepository.save(UserCredentials.of(admin1.getId(), "admin1", encoder.encode("admin_password1"), Role.ADMIN, true));

            //Every customer has 10000 TRY as an asset
            jpaAssetRepository.save(new Asset(customer1, "TRY", new BigDecimal(10000), new BigDecimal(10000)));
            jpaAssetRepository.save(new Asset(customer2, "TRY", new BigDecimal(10000), new BigDecimal(10000)));
            jpaAssetRepository.save(new Asset(customer3, "TRY", new BigDecimal(10000), new BigDecimal(10000)));

            jpaAssetRepository.save(new Asset(customer1, "AAPL", new BigDecimal(5000), new BigDecimal(5000)));
            jpaAssetRepository.save(new Asset(customer2, "AAPL", new BigDecimal(5000), new BigDecimal(5000)));
            jpaAssetRepository.save(new Asset(customer3, "AAPL", new BigDecimal(5000), new BigDecimal(5000)));


            Order order1 = Order.create(customer1, "TRY", OrderSide.SELL, new BigDecimal(100), new BigDecimal("1.0"));
            order1.setStatus(OrderStatus.CANCELED);

            Order order2 = Order.create(customer2, "TRY", OrderSide.SELL, new BigDecimal(100), new BigDecimal("1.0"));
            order2.setStatus(OrderStatus.CANCELED);

            jpaOrderRepository.saveAll(List.of(order1, order2));

            jpaUserCredentialRepository.findAll().forEach(credentials -> {
                log.info("Preloaded " + credentials);
            });
        };
    }
}
