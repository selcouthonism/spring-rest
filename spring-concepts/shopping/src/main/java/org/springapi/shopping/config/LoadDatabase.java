package org.springapi.shopping.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springapi.shopping.model.Order;
import org.springapi.shopping.repository.OrderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    //@Profile("test")
    CommandLineRunner initDatabase(OrderRepository orderRepository) {

        return args -> {

            orderRepository.save(new Order("MacBook Pro", 1));
            orderRepository.save(new Order("iPhone", 2));
            orderRepository.save(new Order("iPad", 3));

            orderRepository.findAll().forEach(order -> {
                log.info("Preloaded " + order);
            });

        };
    }
}
