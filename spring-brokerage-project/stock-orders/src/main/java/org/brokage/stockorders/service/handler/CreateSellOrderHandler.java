package org.brokage.stockorders.service.handler;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.dto.CreateOrderDTO;
import org.brokage.stockorders.model.entity.Asset;
import org.brokage.stockorders.model.enums.OrderSide;
import org.brokage.stockorders.repository.AssetRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateSellOrderHandler implements OrderActionHandler<CreateOrderDTO>{

    private final AssetRepository assetRepository;

    @Override
    public void handle(CreateOrderDTO request) {

        Asset asset = assetRepository.lockAssetForCustomer(request.assetName(), request.customerId());

        if (asset.getUsableSize().compareTo(request.size()) < 0) {
            throw new ValidationException("Insufficient usable shares. Have: " + asset.getUsableSize() + ", Need: " + request.size());
        }

        // Reserve usable size
        asset.setUsableSize(asset.getUsableSize().subtract(request.size()));
    }

    @Override
    public OrderAction getAction() {
        return OrderAction.CREATE;
    }

    @Override
    public OrderSide getOrderSide() {
        return OrderSide.SELL;
    }
}
