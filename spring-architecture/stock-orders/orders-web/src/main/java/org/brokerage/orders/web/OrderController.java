package org.brokerage.orders.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @GetMapping("/health")
    public String health() {
        System.out.println("Orders service is running");
        return "Orders service is running";
    }
}
