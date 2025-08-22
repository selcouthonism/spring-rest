package org.springapi.shopping.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springapi.shopping.dto.OrderDto;
import org.springapi.shopping.enums.Status;
import org.springapi.shopping.model.Order;
import org.springapi.shopping.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Order testOrder;

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();

        testOrder = new Order("Laptop", 3);
        testOrder.setStatus(Status.IN_PROGRESS);
        testOrder.setCreatedAt(Instant.now());
        testOrder.setUpdatedAt(Instant.now());

        orderRepository.save(testOrder);
    }

    @Test
    void shouldReturnOrderById() throws Exception {
        mockMvc.perform(get("/orders/" + testOrder.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(testOrder.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.product").value("Laptop"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.quantity").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(MockMvcResultMatchers.jsonPath("$._links.self.href").exists());
    }

    @Test
    void shouldCreateNewOrder() throws Exception {
        OrderDto newOrder = new OrderDto(
                null,
                "Phone",
                2,
                null,
                null,
                null
        );

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newOrder)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.product").value("Phone"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.quantity").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$._links.self.href").exists());
    }

    @Test
    void shouldCompleteOrder() throws Exception {
        mockMvc.perform(put("/orders/" + testOrder.getId() + "/complete"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldCancelOrder() throws Exception {
        mockMvc.perform(delete("/orders/" + testOrder.getId() + "/cancel"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("CANCELLED"));
    }
}