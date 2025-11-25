package org.brokage.stockorders.application.service.handler;

import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.application.port.out.OrderRepository;
import org.brokage.stockorders.domain.model.asset.Asset;
import org.brokage.stockorders.domain.model.order.Order;
import org.brokage.stockorders.domain.model.order.OrderSide;
import org.brokage.stockorders.application.port.out.AssetRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class MatchSellOrderHandler implements OrderActionHandler<Order> {

    private final AssetRepository assetRepository;
    private final OrderRepository orderRepository;

    @Override
    public Order handle(Order order) {
        order.match();

        Asset tryAsset = assetRepository.lockAssetForCustomer( "TRY", order.getCustomer().getId());
        Asset asset = assetRepository.lockAssetForCustomer(order.getAssetName(), order.getCustomer().getId());

        asset.withdrawFromSize(order.getSize());

        BigDecimal earnedTRY = order.getSize().multiply(order.getPrice());
        tryAsset.credit(earnedTRY);
        assetRepository.save(tryAsset);
        assetRepository.save(asset);

        return orderRepository.save(order);
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
