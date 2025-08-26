package org.brokage.stockorders.controller;

import org.brokage.stockorders.model.entity.Asset;
import org.brokage.stockorders.model.entity.Customer;
import org.brokage.stockorders.model.entity.Order;
import org.brokage.stockorders.model.enums.OrderSide;
import org.brokage.stockorders.model.enums.Role;
import org.brokage.stockorders.repository.AssetRepository;
import org.brokage.stockorders.repository.CustomerRepository;
import org.brokage.stockorders.repository.OrderRepository;
import org.brokage.stockorders.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AssetRepository assetRepository;

    private CustomUserDetails mockUser;
    private Long customerId;

    @BeforeEach
    void setUp() {
        // ensure test DB has a customer
        Customer customer = new Customer();
        customer.setUsername("testUser");
        customer.setPasswordHash(new BCryptPasswordEncoder().encode("password"));
        customer.setRole(Role.CUSTOMER);
        customer.setActive(true);

        customerRepository.save(customer);
        customerId = customer.getId();
        mockUser = new CustomUserDetails(customer);

        assetRepository.save(new Asset(customer, "AAPL", new BigDecimal(1000), new BigDecimal(1000)));
    }

    @Test
    void createOrder_shouldReturnCreated() throws Exception {
        String json = """
            {
              "customerId": %d,
              "assetName": "AAPL",
              "orderSide": "SELL",
              "size": 10,
              "price": 100
            }
            """.formatted(customerId);

        mockMvc.perform(post("/api/v1/orders")
                        .with(SecurityMockMvcRequestPostProcessors.user(mockUser))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/orders/")))
                .andExpect(jsonPath("$.assetName").value("AAPL"))
                .andExpect(jsonPath("$.orderSide").value("SELL"))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.price").value(100))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }


    @Test
    void getOrder_shouldReturnOrder() throws Exception {
        // persist an order directly
        Order order = orderRepository.save(Order.create(customerRepository.findById(customerId).get(),
                "AAPL", OrderSide.SELL, new BigDecimal(5), new BigDecimal("200")
        ));

        mockMvc.perform(get("/api/v1/orders/{id}", order.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user(mockUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()))
                .andExpect(jsonPath("$.assetName").value("AAPL"))
                .andExpect(jsonPath("$.orderSide").value("SELL"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void cancelOrder_shouldReturnNoContent() throws Exception {
        Order order = orderRepository.save(Order.create(customerRepository.findById(customerId).get(),
                "AAPL", OrderSide.SELL, new BigDecimal(3), new BigDecimal("150")
        ));

        mockMvc.perform(delete("/api/v1/orders/{id}", order.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user(mockUser))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());
    }
}