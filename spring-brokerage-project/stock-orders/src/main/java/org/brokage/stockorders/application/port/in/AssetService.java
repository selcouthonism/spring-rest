package org.brokage.stockorders.application.port.in;

import org.brokage.stockorders.adapter.in.web.dto.AssetDTO;

import java.util.List;

public interface AssetService {
    AssetDTO getAsset(Long assetId);
    AssetDTO getAsset(Long assetId, Long customerId);
    List<AssetDTO> listAssets(Long customerId, String assetName);
}
