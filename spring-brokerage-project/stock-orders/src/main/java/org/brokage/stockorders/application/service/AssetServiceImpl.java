package org.brokage.stockorders.application.service;

import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.adapter.in.web.dto.AssetDTO;
import org.brokage.stockorders.adapter.in.web.mapper.WebAssetMapper;
import org.brokage.stockorders.application.port.out.AssetRepository;
import org.brokage.stockorders.domain.model.asset.Asset;
import org.brokage.stockorders.application.port.in.AssetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final WebAssetMapper assetMapper;

    /**
     * Get asset by id
     */
    @Override
    public AssetDTO getAsset(Long assetId) {
        Asset asset = assetRepository.findById(assetId);
        return assetMapper.toDto(asset);
    }

    @Override
    @Transactional(readOnly = true)
    public AssetDTO getAsset(Long assetId, Long customerId) {
        Asset asset = assetRepository.findByIdAndCustomerId(assetId, customerId);
        return assetMapper.toDto(asset);
    }

    /**
     * List assets by customer and assetName
     */
    @Override
    @Transactional(readOnly = true)
    public List<AssetDTO> listAssets(Long customerId, String assetName) {
        return assetRepository.findAllByFilter(customerId, assetName)
                .stream()
                .map(assetMapper::toDto)
                .toList();
    }
}
