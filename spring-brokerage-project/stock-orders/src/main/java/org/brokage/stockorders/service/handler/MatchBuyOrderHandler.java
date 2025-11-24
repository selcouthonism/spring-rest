package org.brokage.stockorders.service.handler;

import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.exceptions.OperationNotPermittedException;
import org.brokage.stockorders.model.entity.Asset;
import org.brokage.stockorders.model.entity.Order;
import org.brokage.stockorders.model.enums.OrderSide;
import org.brokage.stockorders.model.enums.OrderStatus;
import org.brokage.stockorders.repository.jpa.AssetRepository;
import org.brokage.stockorders.service.AssetFinder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchBuyOrderHandler implements OrderActionHandler<Order>{

    private final AssetFinder assetFinder;
    private final AssetRepository assetRepository;

    @Override
    public void handle(Order order) {

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OperationNotPermittedException("Cannot match order. Status is: " + order.getStatus());
        }

        Asset tryAsset = assetFinder.findAssetForCustomerOrThrow(order.getCustomer().getId(), "TRY");
        Asset asset = assetRepository.findByCustomerIdAndAssetNameForUpdate(order.getCustomer().getId(), order.getAssetName())
                .orElse(new Asset(order.getCustomer(), order.getAssetName(), new BigDecimal(0), new BigDecimal(0)));

        BigDecimal requiredTRY = order.getSize().multiply(order.getPrice()).setScale(2, RoundingMode.HALF_UP);
        tryAsset.setSize(tryAsset.getSize().subtract(requiredTRY)); // finalize TRY deduction

        asset.setSize(asset.getSize().add(order.getSize()));
        asset.setUsableSize(asset.getUsableSize().add(order.getSize()));

        assetRepository.saveAll(List.of(tryAsset, asset));
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
