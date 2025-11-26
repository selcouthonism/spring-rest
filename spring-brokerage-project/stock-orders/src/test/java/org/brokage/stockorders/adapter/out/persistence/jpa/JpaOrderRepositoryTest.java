package org.brokage.stockorders.adapter.out.persistence.jpa;

import org.brokage.stockorders.adapter.out.persistence.entity.CustomerEntity;
import org.brokage.stockorders.adapter.out.persistence.entity.OrderEntity;
import org.brokage.stockorders.domain.model.order.OrderSide;
import org.brokage.stockorders.domain.model.order.OrderStatus;
import org.brokage.stockorders.adapter.out.persistence.jpa.JpaCustomerRepository;
import org.brokage.stockorders.adapter.out.persistence.jpa.JpaOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class JpaOrderRepositoryTest {

    @Autowired
    private JpaOrderRepository jpaOrderRepository;

    @Autowired
    private JpaCustomerRepository jpaCustomerRepository;

    private CustomerEntity customer;

    @BeforeEach
    void setUp() {
        customer = jpaCustomerRepository.save(CustomerEntity.of("testUser", "lastname"));
    }

    @Test
    void saveAndFindOrder() {
        OrderEntity order = new OrderEntity();
        order.setCustomer(customer);
        order.setAssetName("AAPL");
        order.setOrderSide(OrderSide.BUY);
        order.setSize(new BigDecimal(10));
        order.setPrice(new BigDecimal("100"));
        order.setStatus(OrderStatus.PENDING);
        order.setCreateDate(Instant.now());

        OrderEntity saved = jpaOrderRepository.save(order);

        OrderEntity found = jpaOrderRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getAssetName()).isEqualTo("AAPL");
        assertThat(found.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void findAllBySpecification_shouldReturnFilteredOrders() {
        // save multiple orders
        OrderEntity order1 = new OrderEntity();
        order1.setCustomer(customer);
        order1.setAssetName("AAPL");
        order1.setOrderSide(OrderSide.BUY);
        order1.setSize(new BigDecimal(10));
        order1.setPrice(new BigDecimal("100"));
        order1.setStatus(OrderStatus.PENDING);
        order1.setCreateDate(Instant.now());
        jpaOrderRepository.save(order1);

        OrderEntity order2 = new OrderEntity();
        order2.setCustomer(customer);
        order2.setAssetName("TSLA");
        order2.setOrderSide(OrderSide.SELL);
        order2.setSize(new BigDecimal(5));
        order2.setPrice(new BigDecimal("200"));
        order2.setStatus(OrderStatus.MATCHED);
        order2.setCreateDate(Instant.now());
        jpaOrderRepository.save(order2);

        // Example: use JpaSpecificationExecutor to filter by status
        var spec = (org.springframework.data.jpa.domain.Specification<OrderEntity>) (root, query, cb) ->
                cb.equal(root.get("status"), OrderStatus.PENDING);

        List<OrderEntity> filtered = jpaOrderRepository.findAll(spec);

        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).getAssetName()).isEqualTo("AAPL");
    }
}
