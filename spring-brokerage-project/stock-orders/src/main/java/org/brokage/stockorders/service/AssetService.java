package org.brokage.stockorders.service;

import org.brokage.stockorders.dto.AssetDTO;

import java.util.List;

public interface AssetService {
    AssetDTO getAsset(Long assetId);
    public AssetDTO getAsset(Long assetId, Long customerId);
    List<AssetDTO> listAssets(Long customerId, String assetName);
}
