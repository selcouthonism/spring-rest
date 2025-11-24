package org.brokage.stockorders.service.handler;

import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.exceptions.OperationNotPermittedException;
import org.brokage.stockorders.model.entity.Asset;
import org.brokage.stockorders.model.entity.Order;
import org.brokage.stockorders.model.enums.OrderSide;
import org.brokage.stockorders.model.enums.OrderStatus;
import org.brokage.stockorders.repository.AssetRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class CancelBuyOrderHandler implements OrderActionHandler<Order>{

    private final AssetRepository assetRepository;

    @Override
    public void handle(Order order) {

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OperationNotPermittedException("Cannot cancel order. Status is: " + order.getStatus());
        }

        BigDecimal requiredTRY = order.getSize().multiply(order.getPrice()).setScale(2, RoundingMode.HALF_UP);
        Asset tryAsset = assetRepository.lockAssetForCustomer("TRY", order.getCustomer().getId());
        tryAsset.releaseFunds(requiredTRY);
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
