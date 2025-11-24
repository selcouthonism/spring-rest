package org.brokage.stockorders.service.handler;

import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.exceptions.OperationNotPermittedException;
import org.brokage.stockorders.model.entity.Asset;
import org.brokage.stockorders.model.entity.Order;
import org.brokage.stockorders.model.enums.OrderSide;
import org.brokage.stockorders.model.enums.OrderStatus;
import org.brokage.stockorders.repository.AssetRepository;
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
