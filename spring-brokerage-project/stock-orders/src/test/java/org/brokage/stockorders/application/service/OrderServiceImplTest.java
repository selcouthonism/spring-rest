package org.brokage.stockorders.application.service;

import org.brokage.stockorders.adapter.in.web.dto.CreateOrderDTO;
import org.brokage.stockorders.adapter.in.web.dto.OrderDTO;
import org.brokage.stockorders.adapter.in.web.mapper.WebOrderMapper;
import org.brokage.stockorders.adapter.out.persistence.mapper.PersistenceOrderMapper;
import org.brokage.stockorders.application.port.out.OrderRepository;
import org.brokage.stockorders.application.service.handler.*;
import org.brokage.stockorders.application.exception.OperationNotPermittedException;
import org.brokage.stockorders.domain.model.asset.Asset;
import org.brokage.stockorders.domain.model.customer.Customer;
import org.brokage.stockorders.domain.model.order.Order;
import org.brokage.stockorders.domain.model.order.OrderSide;
import org.brokage.stockorders.domain.model.order.OrderStatus;
import org.brokage.stockorders.application.port.out.AssetRepository;
import org.brokage.stockorders.application.port.out.CustomerRepository;
import org.brokage.stockorders.application.service.factory.OrderHandlerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

//@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderHandlerFactory orderHandlerFactory;
    @Mock private WebOrderMapper orderMapper;

    @Mock private AssetRepository assetRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private OrderRepository orderRepository;

    @InjectMocks private OrderServiceImpl orderService;

    private Customer customer;
    private Asset asset;
    private Asset tryAsset;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        customer = new Customer(1L);
        customer.setFirstName("firstName");
        customer.setLastName("lastName");

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
    void createOrder_sell_shouldReserveAssetAndReturnDto() {
        CreateOrderDTO request = new CreateOrderDTO(customer.getId(), "AAPL", OrderSide.SELL, new BigDecimal(10), new BigDecimal(10));

        Order order = new Order(customer, "AAPL", OrderSide.SELL, new BigDecimal(10), new BigDecimal(10));
        OrderDTO dto = new OrderDTO(1L, customer.getId(), "AAPL", OrderSide.SELL, new BigDecimal(10), new BigDecimal(10), OrderStatus.PENDING, Instant.now());

        when(customerRepository.findByIdOrThrow(customer.getId())).thenReturn(customer);
        //when(assetRepository.findByCustomerIdAndAssetNameForUpdate(customer.getId(), "AAPL")).thenReturn(Optional.of(asset));
        when(assetRepository.lockAssetForCustomer( "AAPL", customer.getId())).thenReturn(asset);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDomain(request)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(dto);
        when(orderHandlerFactory.getHandler(OrderAction.CREATE, OrderSide.SELL)).thenReturn(new CreateSellOrderHandler(customerRepository, assetRepository, orderRepository));

        OrderDTO result = orderService.create(request);

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
    void createOrder_buy_shouldReserveAssetAndReturnDto() {
        CreateOrderDTO request = new CreateOrderDTO(1L, "AAPL", OrderSide.BUY, new BigDecimal(10), new BigDecimal(10));

        Order order = new Order(customer, "AAPL", OrderSide.SELL, new BigDecimal(10), new BigDecimal(10));
        OrderDTO dto = new OrderDTO(1L, 1L, "AAPL", OrderSide.SELL, new BigDecimal(10), new BigDecimal(10), OrderStatus.PENDING, null);

        when(customerRepository.findByIdOrThrow(1L)).thenReturn(customer);
        //when(assetRepository.findByCustomerIdAndAssetNameForUpdate(customer.getId(), "AAPL")).thenReturn(Optional.of(asset));
        when(assetRepository.lockAssetForCustomer( "TRY", customer.getId())).thenReturn(tryAsset);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDomain(request)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(dto);
        when(orderHandlerFactory.getHandler(OrderAction.CREATE, OrderSide.BUY)).thenReturn(new CreateBuyOrderHandler(customerRepository, assetRepository, orderRepository));

        OrderDTO result = orderService.create(request);

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
    void createOrder_sell_insufficientAsset_shouldThrow() {
        CreateOrderDTO request = new CreateOrderDTO(1L, "AAPL", OrderSide.SELL, new BigDecimal(200), new BigDecimal(10));

        when(customerRepository.findByIdOrThrow(1L)).thenReturn(customer);
        //when(assetRepository.findByCustomerIdAndAssetNameForUpdate(customer.getId(), "AAPL")).thenReturn(Optional.of(asset));
        when(assetRepository.lockAssetForCustomer("AAPL", customer.getId())).thenReturn(asset);
        when(orderHandlerFactory.getHandler(OrderAction.CREATE, OrderSide.SELL)).thenReturn(new CreateSellOrderHandler(customerRepository, assetRepository, orderRepository));

        assertThrows(RuntimeException.class, () -> orderService.create(request));
    }

    @Test
    void createOrder_buy_insufficientAsset_shouldThrow() {
        CreateOrderDTO request = new CreateOrderDTO(1L, "AAPL", OrderSide.BUY, new BigDecimal(2000), new BigDecimal(10));

        when(customerRepository.findByIdOrThrow(1L)).thenReturn(customer);
        //when(assetRepository.findByCustomerIdAndAssetNameForUpdate(customer.getId(), "AAPL")).thenReturn(Optional.of(asset));
        when(assetRepository.lockAssetForCustomer("TRY", customer.getId())).thenReturn(tryAsset);
        when(orderHandlerFactory.getHandler(OrderAction.CREATE, OrderSide.BUY)).thenReturn(new CreateBuyOrderHandler(customerRepository, assetRepository, orderRepository));

        assertThrows(RuntimeException.class, () -> orderService.create(request));
    }

    @Test
    void cancelOrder_sell_shouldRestoreAssetAndReturnDto() {
        Order order = new Order(customer, "AAPL", OrderSide.SELL, new BigDecimal(10), new BigDecimal(10));
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);

        OrderDTO dto = new OrderDTO(1L, 1L, "AAPL", OrderSide.SELL, new BigDecimal(10), new BigDecimal(10), OrderStatus.CANCELED, null);

        when(orderRepository.findByIdAndCustomerId(1L, customer.getId())).thenReturn(order);
        //when(assetRepository.findByCustomerIdAndAssetNameForUpdate(customer.getId(), "AAPL")).thenReturn(Optional.of(asset));
        when(assetRepository.lockAssetForCustomer("AAPL", customer.getId())).thenReturn(asset);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(dto);
        when(orderHandlerFactory.getHandler(OrderAction.CANCEL, OrderSide.SELL)).thenReturn(new CancelSellOrderHandler(assetRepository, orderRepository));

        OrderDTO result = orderService.cancel(1L, customer.getId());

        // asset restored
        assertThat(asset.getUsableSize()).isEqualTo(new BigDecimal(110));

        // asset size is not updated in this call
        assertThat(asset.getSize()).isEqualTo(new BigDecimal(100));

        // order status updated
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(result).isEqualTo(dto);
    }

    @Test
    void cancel_sell_alreadyCanceled_shouldThrow() {
        Order order = new Order(customer, "AAPL", OrderSide.SELL, new BigDecimal(10), new BigDecimal(10));
        order.setId(1L);
        order.setStatus(OrderStatus.CANCELED);

        when(orderRepository.findByIdAndCustomerId(1L, customer.getId())).thenReturn(order);
        when(orderHandlerFactory.getHandler(OrderAction.CANCEL, OrderSide.SELL)).thenReturn(new CancelSellOrderHandler(assetRepository, orderRepository));

        assertThrows(OperationNotPermittedException.class, () -> orderService.cancel(1L, customer.getId()));
        //assertThrows(IllegalStateException.class, () -> orderService.cancel(1L, customer.getId()));
    }

    @Test
    void cancel_buy_alreadyCanceled_shouldThrow() {
        Order order = new Order(customer, "AAPL", OrderSide.BUY, new BigDecimal(10), new BigDecimal(10));
        order.setId(1L);
        order.setStatus(OrderStatus.CANCELED);

        when(orderRepository.findByIdAndCustomerId(1L, customer.getId())).thenReturn(order);
        when(orderHandlerFactory.getHandler(OrderAction.CANCEL, OrderSide.BUY)).thenReturn(new CancelSellOrderHandler(assetRepository, orderRepository));

        assertThrows(OperationNotPermittedException.class, () -> orderService.cancel(1L, customer.getId()));
        //assertThrows(IllegalStateException.class, () -> orderService.cancel(1L, customer.getId()));
    }

    @Test
    void matchOrder_sell_pendingOrder_shouldReturnMatched() {
        Order order = new Order(customer, "AAPL", OrderSide.SELL, new BigDecimal(10), new BigDecimal(5));
        order.setId(1L);

        OrderDTO dto = new OrderDTO(1L, 1L, "AAPL", OrderSide.SELL, new BigDecimal(10), new BigDecimal(5), OrderStatus.MATCHED, null);

        when(assetRepository.lockAssetForCustomer("TRY", customer.getId())).thenReturn(tryAsset);
        when(assetRepository.lockAssetForCustomer("AAPL", customer.getId())).thenReturn(asset);
        when(orderRepository.findById(1L)).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(dto);
        when(orderHandlerFactory.getHandler(OrderAction.MATCH, OrderSide.SELL)).thenReturn(new MatchSellOrderHandler(assetRepository, orderRepository));

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
        Order order = new Order(customer, "AAPL", OrderSide.BUY, new BigDecimal(10), new BigDecimal(5));
        order.setId(1L);

        OrderDTO dto = new OrderDTO(1L, 1L, "AAPL", OrderSide.BUY, new BigDecimal(10), new BigDecimal(5), OrderStatus.MATCHED, null);

        when(assetRepository.lockAssetForCustomer("TRY", customer.getId())).thenReturn(tryAsset);
        //when(assetFinder.findAssetForCustomerOrThrow(customer.getId(), "AAPL")).thenReturn(asset);
        when(assetRepository.findOrCreateAssetForUpdate( "AAPL", customer.getId())).thenReturn(asset);
        when(orderRepository.findById(1L)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(dto);
        when(orderHandlerFactory.getHandler(OrderAction.MATCH, OrderSide.BUY)).thenReturn(new MatchBuyOrderHandler(assetRepository, orderRepository));

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

        when(orderRepository.findById(1L)).thenReturn(order);
        when(orderHandlerFactory.getHandler(OrderAction.MATCH, OrderSide.SELL)).thenReturn(new MatchSellOrderHandler(assetRepository, orderRepository));

        assertThrows(OperationNotPermittedException.class, () -> orderService.matchOrder(1L));
        //assertThrows(IllegalStateException.class, () -> orderService.matchOrder(1L));
    }

    @Test
    void matchOrder_buy_nonPendingOrder_shouldThrow() {
        Order order = new Order();
        order.setOrderSide(OrderSide.BUY);
        order.setStatus(OrderStatus.CANCELED);

        when(orderRepository.findById(1L)).thenReturn(order);
        when(orderHandlerFactory.getHandler(OrderAction.MATCH, OrderSide.BUY)).thenReturn(new MatchBuyOrderHandler(assetRepository, orderRepository));

        assertThrows(OperationNotPermittedException.class, () -> orderService.matchOrder(1L));
        //assertThrows(IllegalStateException.class, () -> orderService.matchOrder(1L));
    }
}
