package org.brokage.stockorders.repository;

import org.brokage.stockorders.model.entity.Customer;
import org.brokage.stockorders.model.entity.Order;
import org.brokage.stockorders.model.enums.OrderSide;
import org.brokage.stockorders.model.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = customerRepository.save(Customer.of("testUser", "lastname"));
    }

    @Test
    void saveAndFindOrder() {
        Order order = new Order();
        order.setCustomer(customer);
        order.setAssetName("AAPL");
        order.setOrderSide(OrderSide.BUY);
        order.setSize(new BigDecimal(10));
        order.setPrice(new BigDecimal("100"));
        order.setStatus(OrderStatus.PENDING);
        order.setCreateDate(Instant.now());

        Order saved = orderRepository.save(order);

        Order found = orderRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getAssetName()).isEqualTo("AAPL");
        assertThat(found.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void findAllBySpecification_shouldReturnFilteredOrders() {
        // save multiple orders
        Order order1 = new Order();
        order1.setCustomer(customer);
        order1.setAssetName("AAPL");
        order1.setOrderSide(OrderSide.BUY);
        order1.setSize(new BigDecimal(10));
        order1.setPrice(new BigDecimal("100"));
        order1.setStatus(OrderStatus.PENDING);
        order1.setCreateDate(Instant.now());
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setCustomer(customer);
        order2.setAssetName("TSLA");
        order2.setOrderSide(OrderSide.SELL);
        order2.setSize(new BigDecimal(5));
        order2.setPrice(new BigDecimal("200"));
        order2.setStatus(OrderStatus.MATCHED);
        order2.setCreateDate(Instant.now());
        orderRepository.save(order2);

        // Example: use JpaSpecificationExecutor to filter by status
        var spec = (org.springframework.data.jpa.domain.Specification<Order>) (root, query, cb) ->
                cb.equal(root.get("status"), OrderStatus.PENDING);

        List<Order> filtered = orderRepository.findAll(spec);

        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).getAssetName()).isEqualTo("AAPL");
    }
}
