package org.brokage.stockorders.service.impl;

import org.brokage.stockorders.dto.CreateOrderDTO;
import org.brokage.stockorders.dto.OrderDTO;
import org.brokage.stockorders.exceptions.OperationNotPermittedException;
import org.brokage.stockorders.mapper.OrderMapper;
import org.brokage.stockorders.model.entity.Asset;
import org.brokage.stockorders.model.entity.Customer;
import org.brokage.stockorders.model.entity.Order;
import org.brokage.stockorders.model.enums.OrderSide;
import org.brokage.stockorders.model.enums.OrderStatus;
import org.brokage.stockorders.repository.AssetRepository;
import org.brokage.stockorders.repository.CustomerRepository;
import org.brokage.stockorders.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private AssetRepository assetRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private OrderMapper orderMapper;

    @InjectMocks private OrderServiceImpl orderService;

    private Customer customer;
    private Asset asset;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        customer = new Customer();
        customer.setId(1L);

        asset = new Asset();
        asset.setAssetName("AAPL");
        asset.setUsableSize(100L);
        asset.setCustomer(customer);
    }

    @Test
    void createOrder_sellOrder_shouldReserveAssetAndReturnDto() {
        CreateOrderDTO request = new CreateOrderDTO(1L, "AAPL", "SELL", 10L, new BigDecimal("10"));
        Order order = new Order();
        order.setCustomer(customer);
        order.setAssetName("AAPL");
        order.setOrderSide(OrderSide.SELL);
        order.setSize(10L);
        order.setPrice(new BigDecimal("10"));
        order.setStatus(OrderStatus.PENDING);

        OrderDTO dto = new OrderDTO(1L, 1L, "AAPL", OrderSide.SELL, 10L, new BigDecimal("10"), OrderStatus.PENDING, null);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(assetRepository.findByCustomerAndAssetName(customer, "AAPL")).thenReturn(Optional.of(asset));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(dto);

        OrderDTO result = orderService.createOrder(request);

        // Verify asset usable size reduced
        assertThat(asset.getUsableSize()).isEqualTo(90L);

        // Verify order saved
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(OrderStatus.PENDING);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void createOrder_sellOrder_insufficientAsset_shouldThrow() {
        CreateOrderDTO request = new CreateOrderDTO(1L, "AAPL", "SELL", 200L, new BigDecimal("10"));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(assetRepository.findByCustomerAndAssetName(customer, "AAPL")).thenReturn(Optional.of(asset));

        assertThrows(RuntimeException.class, () -> orderService.createOrder(request));
    }

    @Test
    void cancelOrder_sellOrder_shouldRestoreAssetAndReturnDto() {
        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setAssetName("AAPL");
        order.setOrderSide(OrderSide.SELL);
        order.setSize(10L);
        order.setStatus(OrderStatus.PENDING);

        OrderDTO dto = new OrderDTO(1L, 1L, "AAPL", OrderSide.SELL, 10L, new BigDecimal("10"), OrderStatus.CANCELED, null);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(assetRepository.findByCustomerAndAssetName(customer, "AAPL")).thenReturn(Optional.of(asset));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(dto);

        OrderDTO result = orderService.cancelOrder(1L);

        // asset restored
        assertThat(asset.getUsableSize()).isEqualTo(110L);

        // order status updated
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(result).isEqualTo(dto);
    }

    @Test
    void cancelOrder_alreadyCanceled_shouldThrow() {
        Order order = new Order();
        order.setStatus(OrderStatus.CANCELED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(OperationNotPermittedException.class, () -> orderService.cancelOrder(1L));
    }

    @Test
    void matchOrder_pendingOrder_shouldReturnMatched() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);

        OrderDTO dto = new OrderDTO(1L, 1L, "AAPL", OrderSide.SELL, 10L, new BigDecimal("10"), OrderStatus.MATCHED, null);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(dto);

        OrderDTO result = orderService.matchOrder(1L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.MATCHED);
        assertThat(result).isEqualTo(dto);
    }

    @Test
    void matchOrder_nonPendingOrder_shouldThrow() {
        Order order = new Order();
        order.setStatus(OrderStatus.CANCELED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(OperationNotPermittedException.class, () -> orderService.matchOrder(1L));
    }
}
