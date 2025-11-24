package org.brokage.stockorders.service.handler;

import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.dto.CreateOrderDTO;
import org.brokage.stockorders.exceptions.NotEnoughBalanceException;
import org.brokage.stockorders.model.entity.Asset;
import org.brokage.stockorders.model.enums.OrderSide;
import org.brokage.stockorders.repository.AssetRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class CreateBuyOrderHandler implements OrderActionHandler<CreateOrderDTO> {

    private final AssetRepository assetRepository;

    @Override
    public void handle(CreateOrderDTO request) {
        Asset tryAsset = assetRepository.lockAssetForCustomer("TRY", request.customerId());

        BigDecimal requiredTRY = request.size().multiply(request.price()).setScale(2, RoundingMode.HALF_UP);;
        if (tryAsset.getUsableSize().compareTo(requiredTRY) < 0) {
            throw new NotEnoughBalanceException("Not enough TRY balance");
        }

        tryAsset.setUsableSize(tryAsset.getUsableSize().subtract(requiredTRY));
    }

    @Override
    public OrderAction getAction() {
        return OrderAction.CREATE;
    }

    @Override
    public OrderSide getOrderSide() {
        return OrderSide.BUY;
    }
}
