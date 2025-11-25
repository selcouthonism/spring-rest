package org.brokage.stockorders.adapter.in.web.controller;

import org.brokage.stockorders.application.port.out.AssetRepository;
import org.brokage.stockorders.application.port.out.CustomerRepository;
import org.brokage.stockorders.application.port.out.OrderRepository;
import org.brokage.stockorders.domain.model.asset.Asset;
import org.brokage.stockorders.domain.model.customer.Customer;
import org.brokage.stockorders.domain.model.order.Order;
import org.brokage.stockorders.domain.model.order.OrderSide;
import org.brokage.stockorders.domain.model.Role;
import org.brokage.stockorders.security.CustomUserDetails;
import org.brokage.stockorders.security.JwtService;
import org.brokage.stockorders.adapter.out.persistence.entity.UserCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
    private JwtService jwtService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AssetRepository assetRepository;

    private CustomUserDetails mockUser;
    private Long customerId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        // ensure test DB has a customer
        Customer customer = new Customer();
        customer.setFirstName("testUser");
        customer.setLastName("lastName");
        customer = customerRepository.save(customer);
        customerId = customer.getId();

        UserCredentials credential = UserCredentials.of(customer.getId(), "username", "password", Role.CUSTOMER, true);
        mockUser = new CustomUserDetails(credential);

        assetRepository.save(new Asset(customer, "AAPL", new BigDecimal(1000), new BigDecimal(1000)));

        // 🔑 generate token
        jwtToken = jwtService.generateToken(mockUser);
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

        mockMvc.perform(post("/api/v1/customers/{customerId}/orders", customerId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(SecurityMockMvcRequestPostProcessors.user(mockUser))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/customers/"+customerId+"/orders/")))
                .andExpect(jsonPath("$.assetName").value("AAPL"))
                .andExpect(jsonPath("$.orderSide").value("SELL"))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.price").value(100))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }


    @Test
    void getOrder_shouldReturnOrder() throws Exception {
        // persist an order directly
        Order order = orderRepository.save(new Order(customerRepository.findByIdOrThrow(customerId),
                "AAPL", OrderSide.SELL, new BigDecimal(5), new BigDecimal("200")
        ));

        mockMvc.perform(get("/api/v1/customers/{customerId}/orders/{id}", customerId, order.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(SecurityMockMvcRequestPostProcessors.user(mockUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()))
                .andExpect(jsonPath("$.assetName").value("AAPL"))
                .andExpect(jsonPath("$.orderSide").value("SELL"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void cancelOrder_shouldReturnNoContent() throws Exception {
        Order order = orderRepository.save(new Order(customerRepository.findByIdOrThrow(customerId),
                "AAPL", OrderSide.SELL, new BigDecimal(3), new BigDecimal("150")
        ));

        mockMvc.perform(delete("/api/v1/customers/{customerId}/orders/{id}", customerId, order.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(SecurityMockMvcRequestPostProcessors.user(mockUser))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());
    }
}