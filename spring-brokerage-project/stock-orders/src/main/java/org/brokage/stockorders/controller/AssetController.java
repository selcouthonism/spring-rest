package org.brokage.stockorders.controller;

import lombok.RequiredArgsConstructor;
import org.brokage.stockorders.dto.AssetDTO;
import org.brokage.stockorders.security.CustomUserDetails;
import org.brokage.stockorders.service.AssetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
public class AssetController {
    private final AssetService assetService;

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    /**
     * Fetch a single asset by ID.
     */

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<AssetDTO>> getAsset(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principle) {

        log.info("Getting asset with id {}", id);

        AssetDTO dto = principle.isAdmin() ? assetService.getAsset(id) :  assetService.getAsset(id, principle.getId());
        return ResponseEntity.ok().body(EntityModel.of(dto));
    }

    @GetMapping
    public ResponseEntity<List<EntityModel<AssetDTO>>> listAssets(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String assetName,
            @AuthenticationPrincipal CustomUserDetails principal) {

        List<AssetDTO> assetDtoList = assetService.listAssets(principal.isAdmin() ? customerId : principal.getId(), assetName);

        List<EntityModel<AssetDTO>> model = assetDtoList.stream()
                .map(asset -> EntityModel.of(asset))
                .toList();

        return ResponseEntity.ok().body(model);
    }
}
