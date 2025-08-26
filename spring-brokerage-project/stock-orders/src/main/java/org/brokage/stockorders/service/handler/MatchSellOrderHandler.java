package org.brokage.stockorders.service.handler;

import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.exceptions.OperationNotPermittedException;
import org.brokage.stockorders.model.entity.Asset;
import org.brokage.stockorders.model.entity.Order;
import org.brokage.stockorders.model.enums.OrderSide;
import org.brokage.stockorders.model.enums.OrderStatus;
import org.brokage.stockorders.repository.AssetRepository;
import org.brokage.stockorders.service.AssetFinder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchSellOrderHandler implements OrderActionHandler<Order> {

    private final AssetFinder assetFinder;
    private final AssetRepository assetRepository;

    @Override
    public void handle(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OperationNotPermittedException("Cannot match order. Status is: " + order.getStatus());
        }

        Asset tryAsset = assetFinder.findAssetForCustomerOrThrow(order.getCustomer().getId(), "TRY");
        Asset asset = assetFinder.findAssetForCustomerOrThrow(order.getCustomer().getId(),  order.getAssetName());

        asset.setSize(asset.getSize().subtract(order.getSize()));

        BigDecimal earnedTRY = order.getSize().multiply(order.getPrice());
        tryAsset.setSize(tryAsset.getSize().add(earnedTRY));
        tryAsset.setUsableSize(tryAsset.getUsableSize().add(earnedTRY));

        assetRepository.saveAll(List.of(tryAsset, asset));
    }

    @Override
    public OrderAction getAction() {
        return OrderAction.MATCH;
    }

    @Override
    public OrderSide getOrderSide() {
        return OrderSide.SELL;
    }
}
