package org.brokage.stockorders.service.impl;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.dto.CreateOrderDTO;
import org.brokage.stockorders.dto.OrderDTO;
import org.brokage.stockorders.exceptions.NotEnoughBalanceException;
import org.brokage.stockorders.exceptions.UnallowedAccessException;
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
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;
    private final CustomerRepository customerRepository;

    private final OrderMapper orderMapper;

    private final static String ASSET_TRY = "TRY";

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
    @Transactional
    public OrderDTO matchOrder(Long orderId) {
        Order order = findOrderById(orderId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OperationNotPermittedException("Cannot match order. Status is: " + order.getStatus());
        }

        matchOrder(order);

        order.setStatus(OrderStatus.MATCHED);
        return orderMapper.toDto(orderRepository.save(order));
    }

    //Create Order methods
    private void validateOrder(CreateOrderDTO request, Customer customer) {
        // Security Check: Ensure the customer owns this order
        if (!request.customerId().equals(customer.getId())) {
            throw new ValidationException("Invalid customer id.");
        }

        if(request.assetName().equals("TRY")) {
            throw new OperationNotPermittedException("Cannot buy or sell TRY assets.");
        }

        if (request.size().compareTo(BigDecimal.ZERO) <= 0 || request.price().compareTo(BigDecimal.ZERO) <= 0) {
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
        Asset asset = findAssetForCustomer(request.customerId(), request.assetName());

        if (asset.getUsableSize().compareTo(request.size()) < 0) {
            throw new ValidationException("Insufficient usable shares. Have: " + asset.getUsableSize() + ", Need: " + request.size());
        }

        // Reserve usable size
        asset.setUsableSize(asset.getUsableSize().subtract(request.size()));
        assetRepository.save(asset);
    }

    private void handleBuyOrder(CreateOrderDTO request, Customer customer) {
        Asset tryAsset = findAssetForCustomer(request.customerId(), "TRY");

        BigDecimal requiredTRY = request.size().multiply(request.price()).setScale(2, RoundingMode.HALF_UP);;
        if (tryAsset.getUsableSize().compareTo(requiredTRY) < 0) {
            throw new NotEnoughBalanceException("Not enough TRY balance");
        }

        tryAsset.setUsableSize(tryAsset.getUsableSize().subtract(requiredTRY));
        assetRepository.save(tryAsset);
    }


    private Asset findAssetForCustomer(Long customerId, String assetName) {
        return assetRepository.findByCustomerIdAndAssetNameForUpdate(customerId, assetName)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found for customer: " + customerId + ", assetName: " + assetName));
    }

    //Utility
    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for ID:" + orderId));
    }

    private void validateCustomerAccess(Order order, Long customerId) {
        // Security Check: Ensure the customer owns this order
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new UnallowedAccessException("Order customer not permitted");
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

        if (order.getOrderSide() == OrderSide.BUY) {
            BigDecimal requiredTRY = order.getSize().multiply(order.getPrice()).setScale(2, RoundingMode.HALF_UP);;
            Asset tryAsset = findAssetForCustomer(order.getCustomer().getId(), ASSET_TRY);
            tryAsset.setUsableSize(tryAsset.getUsableSize().add(requiredTRY));
            assetRepository.save(tryAsset);
        } else { //SELL
            // Restore usable shares
            Asset asset = assetRepository.findByCustomerIdAndAssetNameForUpdate(order.getCustomer().getId(), order.getAssetName())
                    .orElseThrow(() -> new IllegalStateException("Asset not found"));

            asset.setUsableSize(asset.getUsableSize().add(order.getSize()));
            assetRepository.save(asset);
        }

        order.setStatus(OrderStatus.CANCELED);
        return orderMapper.toDto(orderRepository.save(order));
    }

    //Match methods
    private void matchOrder(Order order) {
        switch (order.getOrderSide()){
            case SELL:
                matchSellOrder(order);
                break;
            case BUY:
                matchBuyOrder(order);
                break;
            default:
                throw new ValidationException("Order side not exist");
        }
    }

    private void matchSellOrder(Order order) {
        Asset tryAsset = findAssetForCustomer(order.getCustomer().getId(), ASSET_TRY);
        Asset asset = findAssetForCustomer(order.getCustomer().getId(),  order.getAssetName());

        asset.setSize(asset.getSize().subtract(order.getSize()));

        BigDecimal earnedTRY = order.getSize().multiply(order.getPrice());
        tryAsset.setSize(tryAsset.getSize().add(earnedTRY));
        tryAsset.setUsableSize(tryAsset.getUsableSize().add(earnedTRY));
    }

    private void matchBuyOrder(Order order) {
        Asset tryAsset = findAssetForCustomer(order.getCustomer().getId(), ASSET_TRY);
        Asset asset = assetRepository.findByCustomerIdAndAssetNameForUpdate(order.getCustomer().getId(), order.getAssetName())
                .orElse(new Asset(order.getCustomer(), order.getAssetName(), new BigDecimal(0), new BigDecimal(0)));

        BigDecimal requiredTRY = order.getSize().multiply(order.getPrice()).setScale(2, RoundingMode.HALF_UP);
        tryAsset.setSize(tryAsset.getSize().subtract(requiredTRY)); // finalize TRY deduction

        asset.setSize(asset.getSize().add(order.getSize()));
        asset.setUsableSize(asset.getUsableSize().add(order.getSize()));

        assetRepository.save(tryAsset);
        assetRepository.save(asset);
    }

}
