package org.brokage.stockorders.application.service.handler;

import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.application.port.out.OrderRepository;
import org.brokage.stockorders.domain.model.asset.Asset;
import org.brokage.stockorders.domain.model.order.Order;
import org.brokage.stockorders.domain.model.order.OrderSide;
import org.brokage.stockorders.application.port.out.AssetRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CancelSellOrderHandler implements OrderActionHandler<Order> {

    private final AssetRepository assetRepository;
    private final OrderRepository orderRepository;

    @Override
    public Order handle(Order order) {
        order.cancel();

        // Restore usable shares
        Asset asset = assetRepository.lockAssetForCustomer(order.getAssetName(), order.getCustomer().getId());
        asset.releaseFunds(order.getSize());

        assetRepository.save(asset);

        return orderRepository.save(order);
    }

    @Override
    public OrderAction getAction() {
        return OrderAction.CANCEL;
    }

    @Override
    public OrderSide getOrderSide() {
        return OrderSide.SELL;
    }
}
