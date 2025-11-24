package org.brokage.stockorders.application.service.handler;

import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.adapter.in.web.dto.CreateOrderDTO;
import org.brokage.stockorders.domain.exception.NotEnoughBalanceException;
import org.brokage.stockorders.adapter.out.persistence.entity.Asset;
import org.brokage.stockorders.domain.model.order.OrderSide;
import org.brokage.stockorders.application.port.out.AssetRepository;
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
