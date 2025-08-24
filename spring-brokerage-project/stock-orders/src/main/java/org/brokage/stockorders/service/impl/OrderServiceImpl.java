package org.brokage.stockorders.service.impl;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.dto.CreateOrderDTO;
import org.brokage.stockorders.dto.OrderDTO;
import org.brokage.stockorders.mapper.OrderMapper;
import org.brokage.stockorders.repository.OrderSpecifications;
import org.brokage.stockorders.service.OrderService;
import org.brokage.stockorders.model.entity.Asset;
import org.brokage.stockorders.repository.AssetRepository;
import org.brokage.stockorders.model.entity.Customer;
import org.brokage.stockorders.repository.CustomerRepository;
import org.brokage.stockorders.model.entity.Order;
import org.brokage.stockorders.repository.OrderRepository;
import org.brokage.stockorders.model.enums.OrderSide;
import org.brokage.stockorders.model.enums.OrderStatus;
import org.brokage.stockorders.exceptions.OperationNotPermittedException;
import org.brokage.stockorders.exceptions.ResourceNotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;
    private final CustomerRepository customerRepository;

    private final OrderMapper orderMapper;

    /**
     * Create a new order with PENDING status.
     * If SELL -> check usableSize of asset and reduce it.
     */
    @Override
    @Transactional
    public OrderDTO createOrder(CreateOrderDTO request) {
        Long customerId = request.customerId();
        return createOrder(request, customerId);
    }

    @Override
    @Transactional
    public OrderDTO createOrder(CreateOrderDTO request, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        validateOrder(request, customer);

        OrderSide orderSide = OrderSide.valueOf(request.orderSide().toUpperCase());

        handleOrder(request, orderSide, customer);

        Order newOrder = Order.builder()
                .customer(customer)
                .assetName(request.assetName())
                .orderSide(orderSide)
                .size(request.size())
                .price(request.price())
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(newOrder);
        return orderMapper.toDto(savedOrder);
    }

    /**
     * Find order with given orderId.
     */
    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrder(Long orderId) {
        Order order = findOrderById(orderId);
        return orderMapper.toDto(order);
    }

    /**
     * Find order with given orderId and customerId.
     */
    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrder(Long orderId, Long customerId) {
        Order order = findOrderById(orderId);
        validateCustomerAccess(order, customerId);
        return orderMapper.toDto(order);
    }

    /**
     * List orders for customer within date range (optional filters).
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> listOrders(Long customerId, Instant from, Instant to, OrderStatus orderStatus) {

        //allOf(...) → AND
        //anyOf(...) → OR
        Specification<Order> spec = Specification.allOf(
                OrderSpecifications.createdAfter(from),
                OrderSpecifications.createdBefore(to),
                OrderSpecifications.hasCustomerId(customerId),
                OrderSpecifications.hasStatus(orderStatus)
        );

        return orderRepository.findAll(spec).stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public OrderDTO cancelOrder(Long orderId) {
        Order order = findOrderById(orderId);
        return processCancellation(order);
    }

    @Override
    @Transactional
    public OrderDTO cancelOrder(Long orderId, Long customerId) {
        Order order = findOrderById(orderId);
        validateCustomerAccess(order, customerId);
        return processCancellation(order);
    }

    @Override
    public OrderDTO matchOrder(Long orderId) {
        //TODO: need further clarification
        Order order = findOrderById(orderId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OperationNotPermittedException("Cannot match order. Status is: " + order.getStatus());
        }

        order.setStatus(OrderStatus.MATCHED);
        Order matchedOrder = orderRepository.save(order);
        return orderMapper.toDto(matchedOrder);
    }

    private void validateOrder(CreateOrderDTO request, Customer customer) {
        // Security Check: Ensure the customer owns this order
        if (!request.customerId().equals(customer.getId())) {
            throw new ValidationException("Invalid customer id.");
        }

        if (request.size() <= 0 || request.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Order size and price must be positive.");
        }
    }

    private void handleOrder(CreateOrderDTO request, OrderSide orderSide, Customer customer) {
        switch (orderSide){
            case SELL:
                handleSellOrder(request, customer);
                break;
            case BUY:
                handleBuyOrder(request, customer);
                break;
            default:
                throw new ValidationException("Order side not exist");
        }
    }

    private void handleSellOrder(CreateOrderDTO request, Customer customer) {
        Asset asset = assetRepository.findByCustomerAndAssetName(customer, request.assetName())
                .orElseThrow(() -> new ResourceNotFoundException("No asset found for: " + request.assetName()));

        if (asset.getUsableSize().compareTo(request.size()) < 0) {
            throw new ValidationException("Insufficient usable shares. Have: " + asset.getUsableSize() + ", Need: " + request.size());
        }

        // Reserve usable size
        asset.setUsableSize(asset.getUsableSize() - request.size());
        assetRepository.save(asset);
    }

    private void handleBuyOrder(CreateOrderDTO request, Customer customer) {
        //TODO: need further clarification
        throw new UnsupportedOperationException("need further clarification");
    }

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for ID:" + orderId));
    }

    private void validateCustomerAccess(Order order, Long customerId) {
        // Security Check: Ensure the customer owns this order
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new OperationNotPermittedException("Order customer not permitted");
        }
    }

    /**
     * Cancel an order if still PENDING.
     * If SELL -> restore asset usable size.
     */
    private OrderDTO processCancellation(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OperationNotPermittedException("Cannot cancel order. Status is: " + order.getStatus());
        }

        if (order.getOrderSide() == OrderSide.SELL) {
            // Restore usable shares
            Asset asset = assetRepository.findByCustomerAndAssetName(order.getCustomer(), order.getAssetName())
                    .orElseThrow(() -> new IllegalStateException("Asset missing for a SELL order cancellation. Data integrity issue."));

            asset.setUsableSize(asset.getUsableSize() + order.getSize());
            assetRepository.save(asset);
        } else {
            // todo: Restore cash balance
        }

        order.setStatus(OrderStatus.CANCELED);
        Order canceledOrder = orderRepository.save(order);
        return orderMapper.toDto(canceledOrder);
    }
}
