package org.brokage.stockorders.infrastructure.config;

import org.brokage.stockorders.adapter.out.persistence.entity.AssetEntity;
import org.brokage.stockorders.adapter.out.persistence.entity.CustomerEntity;
import org.brokage.stockorders.adapter.out.persistence.entity.OrderEntity;
import org.brokage.stockorders.domain.model.order.OrderSide;
import org.brokage.stockorders.domain.model.order.OrderStatus;
import org.brokage.stockorders.domain.model.Role;
import org.brokage.stockorders.adapter.out.persistence.jpa.JpaAssetRepository;
import org.brokage.stockorders.adapter.out.persistence.jpa.JpaCustomerRepository;
import org.brokage.stockorders.adapter.out.persistence.jpa.JpaOrderRepository;
import org.brokage.stockorders.adapter.out.persistence.jpa.JpaUserCredentialRepository;
import org.brokage.stockorders.adapter.out.persistence.entity.UserCredentials;
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

            CustomerEntity customer1 = jpaCustomerRepository.save(CustomerEntity.of("customer1FN", "lastName1"));
            CustomerEntity customer2 = jpaCustomerRepository.save(CustomerEntity.of("customer2FN", "lastName1"));
            CustomerEntity customer3 = jpaCustomerRepository.save(CustomerEntity.of("customer3FN", "lastName1"));
            CustomerEntity admin1 = jpaCustomerRepository.save(CustomerEntity.of("admin1FN", "lastName1"));

            credentialRepository.save(UserCredentials.of(customer1.getId(), "customer1", encoder.encode("password1"), Role.CUSTOMER, true));
            credentialRepository.save(UserCredentials.of(customer2.getId(), "customer2", encoder.encode("password2"), Role.CUSTOMER, true));
            credentialRepository.save(UserCredentials.of(customer3.getId(), "customer3", encoder.encode("password3"), Role.CUSTOMER, true));
            credentialRepository.save(UserCredentials.of(admin1.getId(), "admin1", encoder.encode("admin_password1"), Role.ADMIN, true));

            //Every customer has 10000 TRY as an asset
            jpaAssetRepository.save(new AssetEntity(customer1, "TRY", new BigDecimal(10000), new BigDecimal(10000)));
            jpaAssetRepository.save(new AssetEntity(customer2, "TRY", new BigDecimal(10000), new BigDecimal(10000)));
            jpaAssetRepository.save(new AssetEntity(customer3, "TRY", new BigDecimal(10000), new BigDecimal(10000)));

            jpaAssetRepository.save(new AssetEntity(customer1, "AAPL", new BigDecimal(5000), new BigDecimal(5000)));
            jpaAssetRepository.save(new AssetEntity(customer2, "AAPL", new BigDecimal(5000), new BigDecimal(5000)));
            jpaAssetRepository.save(new AssetEntity(customer3, "AAPL", new BigDecimal(5000), new BigDecimal(5000)));


            OrderEntity order1 = OrderEntity.create(customer1, "TRY", OrderSide.SELL, new BigDecimal(100), new BigDecimal("1.0"));
            order1.setStatus(OrderStatus.CANCELED);

            OrderEntity order2 = OrderEntity.create(customer2, "TRY", OrderSide.SELL, new BigDecimal(100), new BigDecimal("1.0"));
            order2.setStatus(OrderStatus.CANCELED);

            jpaOrderRepository.saveAll(List.of(order1, order2));

            jpaUserCredentialRepository.findAll().forEach(credentials -> {
                log.info("Preloaded " + credentials);
            });
        };
    }
}
