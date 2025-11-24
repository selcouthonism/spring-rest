package org.brokerage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.brokerage")
public class BrokerageApplication {
    public static void main(String[] args) {
        SpringApplication.run(BrokerageApplication.class, args);
    }
}