package org.brokage.stockorders.application.service.handler;

import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.application.port.out.CustomerRepository;
import org.brokage.stockorders.application.port.out.OrderRepository;
import org.brokage.stockorders.domain.model.asset.Asset;
import org.brokage.stockorders.domain.model.customer.Customer;
import org.brokage.stockorders.domain.model.order.Order;
import org.brokage.stockorders.domain.model.order.OrderSide;
import org.brokage.stockorders.application.port.out.AssetRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateSellOrderHandler implements OrderActionHandler<Order>{

    private final CustomerRepository customerRepository;
    private final AssetRepository assetRepository;
    private final OrderRepository orderRepository;

    @Override
    public Order handle(Order domain) {
        Customer customer = customerRepository.findByIdOrThrow(domain.getCustomer().getId());
        domain.setCustomer(customer);

        Asset asset = assetRepository.lockAssetForCustomer(domain.getAssetName(), domain.getCustomer().getId());
        asset.checkUsableSize(domain.getSize());

        // Reserve usable size
        asset.withdrawFromUsable(domain.getSize());
        assetRepository.save(asset);

        return orderRepository.save(domain);
    }

    @Override
    public OrderAction getAction() {
        return OrderAction.CREATE;
    }

    @Override
    public OrderSide getOrderSide() {
        return OrderSide.SELL;
    }
}
