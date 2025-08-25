package org.brokage.stockorders.controller;

import org.brokage.stockorders.assembler.OrderModelAssembler;
import org.brokage.stockorders.dto.CreateOrderDTO;
import org.brokage.stockorders.dto.OrderDTO;
import org.brokage.stockorders.exceptions.ResourceNotFoundException;
import org.brokage.stockorders.model.entity.Customer;
import org.brokage.stockorders.model.enums.OrderSide;
import org.brokage.stockorders.model.enums.OrderStatus;
import org.brokage.stockorders.model.enums.Role;
import org.brokage.stockorders.security.CustomUserDetails;
import org.brokage.stockorders.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(OrderController.class)
@Import(OrderControllerTest.TestConfig.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderModelAssembler assembler;

    private CustomUserDetails mockUser;

    private final Long customerId = 101L;

    @BeforeEach
    void setupUser() {
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setUsername("testUser");
        customer.setPasswordHash("password");
        customer.setRole(Role.CUSTOMER);

        mockUser = new CustomUserDetails(customer);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        OrderService orderService() {
            return Mockito.mock(OrderService.class);
        }

        @Bean
        OrderModelAssembler orderModelAssembler() {
            return Mockito.mock(OrderModelAssembler.class);
        }
    }


    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getOrder_shouldReturnOrder() throws Exception {
        OrderDTO dto = new OrderDTO(1L, 1L, "AAPL", OrderSide.SELL, new BigDecimal(10), new BigDecimal(10L), OrderStatus.PENDING, Instant.now());
        EntityModel<OrderDTO> model = EntityModel.of(dto);

        Mockito.when(orderService.getOrder(eq(1L), eq(mockUser.getId())))
                .thenReturn(dto);
        Mockito.when(assembler.toModel(dto)).thenReturn(model);

        mockMvc.perform(get("/api/v1/orders/{id}", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.user(mockUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.assetName").value("AAPL"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createOrder_shouldReturnCreated() throws Exception {
        //CreateOrderDTO request = new CreateOrderDTO(customerId, "AAPL", OrderSide.SELL.name(), 10L, new BigDecimal(10L));
        OrderDTO dto = new OrderDTO(1L, customerId, "AAPL", OrderSide.SELL, new BigDecimal(10), new BigDecimal(10L), OrderStatus.PENDING, Instant.now());
        EntityModel<OrderDTO> model = EntityModel.of(dto);

        Mockito.when(orderService.createOrder(any(CreateOrderDTO.class), eq(mockUser.getId())))
                .thenReturn(dto);
        Mockito.when(assembler.toModel(dto)).thenReturn(model);

        mockMvc.perform(post("/api/v1/orders")
                        .with(SecurityMockMvcRequestPostProcessors.user(mockUser))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "customerId": %d,
                              "assetName": "AAPL",
                              "orderSide": "SELL",
                              "size": 10,
                              "price": 10
                            }
                            """.formatted(customerId)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/orders/1")))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andExpect(jsonPath("$.assetName").value("AAPL"))
                .andExpect(jsonPath("$.orderSide").value("SELL"))
                .andExpect(jsonPath("$.size").value(10L))
                .andExpect(jsonPath("$.price").value(10L));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void listOrders_shouldReturnList() throws Exception {
        OrderDTO dto1 = new OrderDTO(1L, customerId, "AAPL", OrderSide.SELL, new BigDecimal(10), new BigDecimal(10L), OrderStatus.PENDING, Instant.now());
        OrderDTO dto2 = new OrderDTO(2L, customerId, "ALK", OrderSide.SELL, new BigDecimal(10), new BigDecimal(10L), OrderStatus.CANCELED, Instant.now());

        Mockito.when(orderService.listOrders(eq(mockUser.getId()), any(), any(), any()))
                .thenReturn(List.of(dto1, dto2));
        Mockito.when(assembler.toModel(dto1)).thenReturn(EntityModel.of(dto1));
        Mockito.when(assembler.toModel(dto2)).thenReturn(EntityModel.of(dto2));

        mockMvc.perform(get("/api/v1/orders")
                        .with(SecurityMockMvcRequestPostProcessors.user(mockUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void cancelOrder_shouldReturnNoContent() throws Exception {
        OrderDTO canceled = new OrderDTO(1L, customerId, "AAPL", OrderSide.SELL, new BigDecimal(10), new BigDecimal(10L), OrderStatus.CANCELED, Instant.now());

        Mockito.when(orderService.cancelOrder(eq(1L), eq(mockUser.getId())))
                .thenReturn(canceled);

        mockMvc.perform(delete("/api/v1/orders/{orderId}", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.user(mockUser))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());
    }

    /*
    @Test
    @WithMockUser(roles = "CUSTOMER")
    void cancelOrder_whenOrderNotFound_shouldReturnNotFound() throws Exception {
        Mockito.when(orderService.cancelOrder(eq(999L), eq(mockUser.getId())))
                .thenThrow(new ResourceNotFoundException("Order with id 999 not found"));

        mockMvc.perform(delete("/api/v1/orders/{orderId}", 999L)
                        .with(SecurityMockMvcRequestPostProcessors.user(mockUser))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.detail").value("Order with id 999 not found"));
    }
     */
}
