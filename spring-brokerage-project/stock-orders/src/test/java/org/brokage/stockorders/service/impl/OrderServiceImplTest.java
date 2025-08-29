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
import org.brokage.stockorders.model.enums.Role;
import org.brokage.stockorders.repository.AssetRepository;
import org.brokage.stockorders.repository.CustomerRepository;
import org.brokage.stockorders.repository.OrderRepository;
import org.brokage.stockorders.service.AssetFinder;
import org.brokage.stockorders.service.factory.OrderHandlerFactory;
import org.brokage.stockorders.service.handler.*;
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
    @Mock private OrderHandlerFactory orderHandlerFactory;
    @Mock private OrderMapper orderMapper;
    @Mock private AssetFinder assetFinder;

    @InjectMocks private OrderServiceImpl orderService;

    private Customer customer;
    private Asset asset;
    private Asset tryAsset;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("firstName");
        customer.setLastName("lastName");
        customerRepository.save(customer);

        asset = new Asset();
        asset.setId(2L);
        asset.setAssetName("AAPL");
        asset.setSize(new BigDecimal(100));
        asset.setUsableSize(new BigDecimal(100));
        asset.setCustomer(customer);

        tryAsset = new Asset();
        tryAsset.setId(1L);
        tryAsset.setCustomer(customer);
        tryAsset.setAssetName("TRY");
        tryAsset.setSize(new BigDecimal(10000));
        tryAsset.setUsableSize(new BigDecimal(10000));
    }

    @Test
    void createOrder_sellOrder_shouldReserveAssetAndReturnDto() {
        CreateOrderDTO request = new CreateOrderDTO(1L, "AAPL", "SELL", new BigDecimal(10), new BigDecimal(10));

        Order order = Order.create(customer, "AAPL", OrderSide.SELL, new BigDecimal(10), new BigDecimal(10));
        OrderDTO dto = new OrderDTO(1L, 1L, "AAPL", OrderSide.SELL, new BigDecimal(10), new BigDecimal(10), OrderStatus.PENDING, null);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        //when(assetRepository.findByCustomerIdAndAssetNameForUpdate(customer.getId(), "AAPL")).thenReturn(Optional.of(asset));
        when(assetFinder.findAssetForCustomerOrThrow(customer.getId(), "AAPL")).thenReturn(asset);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(dto);
        when(orderHandlerFactory.getHandler(OrderAction.CREATE, OrderSide.SELL)).thenReturn(new CreateSellOrderHandler(assetRepository, assetFinder));

        OrderDTO result = orderService.createOrder(request);

        // Verify asset usable size reduced
        assertThat(asset.getUsableSize()).isEqualTo(new BigDecimal(90));

        // Verify asset size remains same
        assertThat(asset.getSize()).isEqualTo(new BigDecimal(100));

        // Verify order saved
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(OrderStatus.PENDING);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void createOrder_buyOrder_shouldReserveAssetAndReturnDto() {
        CreateOrderDTO request = new CreateOrderDTO(1L, "AAPL", "BUY", new BigDecimal(10), new BigDecimal(10));

        Order order = Order.create(customer, "AAPL", OrderSide.SELL, new BigDecimal(10), new BigDecimal(10));
        OrderDTO dto = new OrderDTO(1L, 1L, "AAPL", OrderSide.SELL, new BigDecimal(10), new BigDecimal(10), OrderStatus.PENDING, null);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        //when(assetRepository.findByCustomerIdAndAssetNameForUpdate(customer.getId(), "AAPL")).thenReturn(Optional.of(asset));
        when(assetFinder.findAssetForCustomerOrThrow(customer.getId(), "TRY")).thenReturn(tryAsset);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(dto);
        when(orderHandlerFactory.getHandler(OrderAction.CREATE, OrderSide.BUY)).thenReturn(new CreateBuyOrderHandler(assetRepository, assetFinder));

        OrderDTO result = orderService.createOrder(request);

        // Verify TRY asset usable size reduced (order.size * price)
        assertThat(tryAsset.getUsableSize()).isEqualTo(new BigDecimal("9900.00"));

        // Verify TRY asset size remains same
        assertThat(tryAsset.getSize()).isEqualTo(new BigDecimal(10000));

        // Verify order saved
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(OrderStatus.PENDING);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void createOrder_sellOrder_insufficientAsset_shouldThrow() {
        CreateOrderDTO request = new CreateOrderDTO(1L, "AAPL", "SELL", new BigDecimal(200), new BigDecimal(10));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        //when(assetRepository.findByCustomerIdAndAssetNameForUpdate(customer.getId(), "AAPL")).thenReturn(Optional.of(asset));
        when(assetFinder.findAssetForCustomerOrThrow(customer.getId(), "AAPL")).thenReturn(asset);
        when(orderHandlerFactory.getHandler(OrderAction.CREATE, OrderSide.SELL)).thenReturn(new CreateSellOrderHandler(assetRepository, assetFinder));

        assertThrows(RuntimeException.class, () -> orderService.createOrder(request));
    }

    @Test
    void createOrder_buyOrder_insufficientAsset_shouldThrow() {
        CreateOrderDTO request = new CreateOrderDTO(1L, "AAPL", "BUY", new BigDecimal(2000), new BigDecimal(10));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        //when(assetRepository.findByCustomerIdAndAssetNameForUpdate(customer.getId(), "AAPL")).thenReturn(Optional.of(asset));
        when(assetFinder.findAssetForCustomerOrThrow(customer.getId(), "TRY")).thenReturn(tryAsset);
        when(orderHandlerFactory.getHandler(OrderAction.CREATE, OrderSide.BUY)).thenReturn(new CreateBuyOrderHandler(assetRepository, assetFinder));

        assertThrows(RuntimeException.class, () -> orderService.createOrder(request));
    }

    @Test
    void cancelOrder_sellOrder_shouldRestoreAssetAndReturnDto() {
        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setAssetName("AAPL");
        order.setOrderSide(OrderSide.SELL);
        order.setSize(new BigDecimal(10));
        order.setStatus(OrderStatus.PENDING);

        OrderDTO dto = new OrderDTO(1L, 1L, "AAPL", OrderSide.SELL, new BigDecimal(10), new BigDecimal(10), OrderStatus.CANCELED, null);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        //when(assetRepository.findByCustomerIdAndAssetNameForUpdate(customer.getId(), "AAPL")).thenReturn(Optional.of(asset));
        when(assetFinder.findAssetForCustomerOrThrow(customer.getId(), "AAPL")).thenReturn(asset);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(dto);
        when(orderHandlerFactory.getHandler(OrderAction.CANCEL, OrderSide.SELL)).thenReturn(new CancelSellOrderHandler(assetFinder, assetRepository));

        OrderDTO result = orderService.cancelOrder(1L);

        // asset restored
        assertThat(asset.getUsableSize()).isEqualTo(new BigDecimal(110));

        // asset size is not updated in this call
        assertThat(asset.getSize()).isEqualTo(new BigDecimal(100));

        // order status updated
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(result).isEqualTo(dto);
    }

    @Test
    void cancelOrder_sell_alreadyCanceled_shouldThrow() {
        Order order = Order.create(customer, "AAPL", OrderSide.SELL, new BigDecimal(10), new BigDecimal(10));
        order.setId(1L);
        order.setStatus(OrderStatus.CANCELED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderHandlerFactory.getHandler(OrderAction.CANCEL, OrderSide.SELL)).thenReturn(new CancelSellOrderHandler(assetFinder, assetRepository));

        assertThrows(OperationNotPermittedException.class, () -> orderService.cancelOrder(1L));
    }

    @Test
    void cancelOrder_buy_alreadyCanceled_shouldThrow() {
        Order order = Order.create(customer, "AAPL", OrderSide.BUY, new BigDecimal(10), new BigDecimal(10));
        order.setId(1L);
        order.setStatus(OrderStatus.CANCELED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderHandlerFactory.getHandler(OrderAction.CANCEL, OrderSide.BUY)).thenReturn(new CancelSellOrderHandler(assetFinder, assetRepository));

        assertThrows(OperationNotPermittedException.class, () -> orderService.cancelOrder(1L));
    }

    @Test
    void matchOrder_sell_pendingOrder_shouldReturnMatched() {
        Order order = Order.create(customer, "AAPL", OrderSide.SELL, new BigDecimal(10), new BigDecimal(5));
        order.setId(1L);

        OrderDTO dto = new OrderDTO(1L, 1L, "AAPL", OrderSide.SELL, new BigDecimal(10), new BigDecimal(5), OrderStatus.MATCHED, null);

        when(assetFinder.findAssetForCustomerOrThrow(customer.getId(), "TRY")).thenReturn(tryAsset);
        when(assetFinder.findAssetForCustomerOrThrow(customer.getId(), "AAPL")).thenReturn(asset);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(dto);
        when(orderHandlerFactory.getHandler(OrderAction.MATCH, OrderSide.SELL)).thenReturn(new MatchSellOrderHandler(assetFinder, assetRepository));

        OrderDTO result = orderService.matchOrder(1L);

        // asset's usable size is not updated in this call
        assertThat(asset.getUsableSize()).isEqualTo(new BigDecimal(100));
        // asset size updated (asset size - order size)
        assertThat(asset.getSize()).isEqualTo(new BigDecimal(90));

        // TRY asset's usable size updated (TRY asset size + (order size * order price))
        assertThat(tryAsset.getUsableSize()).isEqualTo(new BigDecimal(10050));
        // TRY asset size updated (TRY asset size + (order size * order price))
        assertThat(tryAsset.getSize()).isEqualTo(new BigDecimal(10050));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.MATCHED);
        assertThat(result).isEqualTo(dto);
    }

    @Test
    void matchOrder_buy_pendingOrder_shouldReturnMatched() {
        Order order = Order.create(customer, "AAPL", OrderSide.BUY, new BigDecimal(10), new BigDecimal(5));
        order.setId(1L);

        OrderDTO dto = new OrderDTO(1L, 1L, "AAPL", OrderSide.BUY, new BigDecimal(10), new BigDecimal(5), OrderStatus.MATCHED, null);

        when(assetFinder.findAssetForCustomerOrThrow(customer.getId(), "TRY")).thenReturn(tryAsset);
        //when(assetFinder.findAssetForCustomerOrThrow(customer.getId(), "AAPL")).thenReturn(asset);
        when(assetRepository.findByCustomerIdAndAssetNameForUpdate(customer.getId(), "AAPL")).thenReturn(Optional.of(asset));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(dto);
        when(orderHandlerFactory.getHandler(OrderAction.MATCH, OrderSide.BUY)).thenReturn(new MatchBuyOrderHandler(assetFinder, assetRepository));

        OrderDTO result = orderService.matchOrder(1L);

        // asset's usable size remains same
        assertThat(asset.getUsableSize()).isEqualTo(new BigDecimal(110));
        // asset size updated (asset size + order size)
        assertThat(asset.getSize()).isEqualTo(new BigDecimal(110));

        // TRY asset's usable size is not updated in this call
        assertThat(tryAsset.getUsableSize()).isEqualTo(new BigDecimal(10000));
        // TRY asset size updated (TRY asset size - (order size * order price))
        assertThat(tryAsset.getSize()).isEqualTo(new BigDecimal("9950.00"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.MATCHED);
        assertThat(result).isEqualTo(dto);
    }

    @Test
    void matchOrder_sell_nonPendingOrder_shouldThrow() {
        Order order = new Order();
        order.setOrderSide(OrderSide.SELL);
        order.setStatus(OrderStatus.CANCELED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderHandlerFactory.getHandler(OrderAction.MATCH, OrderSide.SELL)).thenReturn(new MatchSellOrderHandler(assetFinder, assetRepository));

        assertThrows(OperationNotPermittedException.class, () -> orderService.matchOrder(1L));
    }

    @Test
    void matchOrder_buy_nonPendingOrder_shouldThrow() {
        Order order = new Order();
        order.setOrderSide(OrderSide.BUY);
        order.setStatus(OrderStatus.CANCELED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderHandlerFactory.getHandler(OrderAction.MATCH, OrderSide.BUY)).thenReturn(new MatchBuyOrderHandler(assetFinder, assetRepository));

        assertThrows(OperationNotPermittedException.class, () -> orderService.matchOrder(1L));
    }
}
