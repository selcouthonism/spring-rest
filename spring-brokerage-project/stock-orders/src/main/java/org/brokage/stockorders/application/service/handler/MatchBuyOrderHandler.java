package org.brokage.stockorders.application.service.handler;

import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.exceptions.OperationNotPermittedException;
import org.brokage.stockorders.adapter.out.persistence.entity.Asset;
import org.brokage.stockorders.adapter.out.persistence.entity.Order;
import org.brokage.stockorders.domain.model.order.OrderSide;
import org.brokage.stockorders.domain.model.order.OrderStatus;
import org.brokage.stockorders.application.port.out.AssetRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class MatchBuyOrderHandler implements OrderActionHandler<Order>{

    private final AssetRepository assetRepository;

    @Override
    public void handle(Order order) {

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OperationNotPermittedException("Cannot match order. Status is: " + order.getStatus());
        }

        Asset tryAsset = assetRepository.lockAssetForCustomer("TRY", order.getCustomer().getId());
        Asset asset = assetRepository.findOrCreateAssetForUpdate(order.getAssetName(), order.getCustomer());
        //Asset asset = assetJpaRepository.findByCustomerIdAndAssetNameForUpdate(order.getCustomer().getId(), order.getAssetName())
        //        .orElseGet(() -> assetJpaRepository.save(new Asset(customer, assetName, BigDecimal.ZERO, BigDecimal.ZERO)));

        BigDecimal requiredTRY = order.getSize().multiply(order.getPrice()).setScale(2, RoundingMode.HALF_UP);
        tryAsset.setSize(tryAsset.getSize().subtract(requiredTRY)); // finalize TRY deduction

        asset.setSize(asset.getSize().add(order.getSize()));
        asset.setUsableSize(asset.getUsableSize().add(order.getSize()));
    }

    @Override
    public OrderAction getAction() {
        return OrderAction.MATCH;
    }

    @Override
    public OrderSide getOrderSide() {
        return OrderSide.BUY;
    }
}
