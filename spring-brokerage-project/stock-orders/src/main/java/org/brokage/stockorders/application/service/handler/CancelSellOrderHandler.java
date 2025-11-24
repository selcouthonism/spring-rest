package org.brokage.stockorders.application.service.handler;

import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.application.exception.OperationNotPermittedException;
import org.brokage.stockorders.adapter.out.persistence.entity.Asset;
import org.brokage.stockorders.adapter.out.persistence.entity.Order;
import org.brokage.stockorders.domain.model.order.OrderSide;
import org.brokage.stockorders.domain.model.order.OrderStatus;
import org.brokage.stockorders.application.port.out.AssetRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CancelSellOrderHandler implements OrderActionHandler<Order> {

    private final AssetRepository assetRepository;

    @Override
    public void handle(Order order) {

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OperationNotPermittedException("Cannot cancel order. Status is: " + order.getStatus());
        }

        // Restore usable shares
        Asset asset = assetRepository.lockAssetForCustomer(order.getAssetName(), order.getCustomer().getId());

        asset.setUsableSize(asset.getUsableSize().add(order.getSize()));
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
