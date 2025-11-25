package org.brokage.stockorders.application.service.handler;

import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.adapter.out.persistence.entity.AssetEntity;
import org.brokage.stockorders.application.port.out.OrderRepository;
import org.brokage.stockorders.domain.model.asset.Asset;
import org.brokage.stockorders.domain.model.order.Order;
import org.brokage.stockorders.domain.model.order.OrderSide;
import org.brokage.stockorders.application.port.out.AssetRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class CancelBuyOrderHandler implements OrderActionHandler<Order>{

    private final AssetRepository assetRepository;
    private final OrderRepository orderRepository;

    @Override
    public Order handle(Order order) {
        order.cancel();

        BigDecimal requiredTRY = order.getTotalCost();
        Asset tryAsset = assetRepository.lockAssetForCustomer("TRY", order.getCustomer().getId());
        tryAsset.releaseFunds(requiredTRY);
        assetRepository.save(tryAsset);

        return orderRepository.save(order);
    }

    @Override
    public OrderAction getAction() {
        return OrderAction.CANCEL;
    }

    @Override
    public OrderSide getOrderSide() {
        return OrderSide.BUY;
    }
}
