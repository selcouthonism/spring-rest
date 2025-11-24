package org.brokage.stockorders.domain.service;

import org.brokage.stockorders.adapter.in.web.dto.AssetDTO;

import java.util.List;

public interface AssetService {
    AssetDTO getAsset(Long assetId);
    public AssetDTO getAsset(Long assetId, Long customerId);
    List<AssetDTO> listAssets(Long customerId, String assetName);
}
