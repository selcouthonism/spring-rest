package org.brokage.stockorders.service.impl;

import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.dto.AssetDTO;
import org.brokage.stockorders.exceptions.ResourceNotFoundException;
import org.brokage.stockorders.exceptions.UnallowedAccessException;
import org.brokage.stockorders.mapper.AssetMapper;
import org.brokage.stockorders.model.entity.Asset;
import org.brokage.stockorders.repository.AssetRepository;
import org.brokage.stockorders.repository.AssetSpecifications;
import org.brokage.stockorders.service.AssetService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final AssetMapper assetMapper;

    /**
     * Get asset by id
     */
    @Override
    public AssetDTO getAsset(Long assetId) {
        Asset asset = findAssetById(assetId);
        return assetMapper.toDto(asset);
    }

    @Override
    @Transactional(readOnly = true)
    public AssetDTO getAsset(Long assetId, Long customerId) {
        Asset asset = findAssetById(assetId);

        if (!asset.getCustomer().getId().equals(customerId)) {
            throw new UnallowedAccessException("Asset customer not permitted");
        }

        return assetMapper.toDto(asset);
    }

    /**
     * List assets by customer and assetName
     */
    @Override
    @Transactional(readOnly = true)
    public List<AssetDTO> listAssets(Long customerId, String assetName) {
        Specification<Asset> spec = Specification.allOf(
                AssetSpecifications.hasCustomerId(customerId),
                AssetSpecifications.hasAssetName(assetName)
        );

        return assetRepository.findAll(spec).stream()
                .map(assetMapper::toDto)
                .toList();
    }

    private Asset findAssetById(Long assetId) {
        return assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found for ID:" + assetId));
    }
}
