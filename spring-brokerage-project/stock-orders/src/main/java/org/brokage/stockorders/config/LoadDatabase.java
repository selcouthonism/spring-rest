package org.brokage.stockorders.config;

import org.brokage.stockorders.model.entity.Asset;
import org.brokage.stockorders.model.entity.Customer;
import org.brokage.stockorders.model.entity.Order;
import org.brokage.stockorders.model.enums.OrderSide;
import org.brokage.stockorders.model.enums.OrderStatus;
import org.brokage.stockorders.model.enums.Role;
import org.brokage.stockorders.repository.AssetRepository;
import org.brokage.stockorders.repository.CustomerRepository;
import org.brokage.stockorders.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class LoadDatabase {
    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    //@Profile("test")
    CommandLineRunner initDatabase(CustomerRepository customerRepository, AssetRepository assetRepository, OrderRepository orderRepository) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return args -> {

            Customer customer1 = customerRepository.save(new Customer("customer1", encoder.encode("password1"), Role.CUSTOMER, true));
            Customer customer2 = customerRepository.save(new Customer("customer2", encoder.encode("password2"), Role.CUSTOMER, true));
            Customer customer3 = customerRepository.save(new Customer("customer3", encoder.encode("password3"), Role.CUSTOMER, true));
            Customer admin1 = customerRepository.save(new Customer("admin1", encoder.encode("admin_password1"), Role.ADMIN, true));

            //Every customer has 10000 TRY as an asset
            assetRepository.save(new Asset(customer1, "TRY", 10000L, 10000L));
            assetRepository.save(new Asset(customer2, "TRY", 10000L, 10000L));
            assetRepository.save(new Asset(customer3, "TRY", 10000L, 10000L));


            orderRepository.save(new Order(customer1, "TRY", OrderSide.SELL, 100L, new BigDecimal("1.0"), OrderStatus.CANCELED));
            orderRepository.save(new Order(customer2, "TRY", OrderSide.SELL, 100L, new BigDecimal("1.0"), OrderStatus.CANCELED));

            customerRepository.findAll().forEach(customer -> {
                log.info("Preloaded " + customer);
            });
        };
    }
}
