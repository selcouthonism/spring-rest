package org.brokage.stockorders.application.service.handler;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.adapter.in.web.dto.CreateOrderDTO;
import org.brokage.stockorders.adapter.out.persistence.entity.Asset;
import org.brokage.stockorders.domain.model.order.OrderSide;
import org.brokage.stockorders.application.port.out.AssetRepository;
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
