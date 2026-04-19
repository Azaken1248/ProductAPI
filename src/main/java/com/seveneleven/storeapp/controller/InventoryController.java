package com.seveneleven.storeapp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.seveneleven.storeapp.model.dto.InventoryRequestDTO;
import com.seveneleven.storeapp.model.dto.InventoryResponseDTO;
import com.seveneleven.storeapp.service.InventoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InventoryResponseDTO> createInventory(
            @Valid @RequestBody InventoryRequestDTO requestDTO) {

        InventoryResponseDTO response =
                inventoryService.createInventory(requestDTO);

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InventoryResponseDTO>> getAllInventory() {

        return ResponseEntity.ok(
                inventoryService.getAllInventory());
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN','SYSTEM')")
    public ResponseEntity<InventoryResponseDTO> getInventoryByProductId(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                inventoryService.getInventoryByProductId(productId));
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InventoryResponseDTO> updateQuantity(
            @PathVariable Long productId,
            @RequestParam int quantity) {

        return ResponseEntity.ok(
                inventoryService.updateQuantity(
                        productId,
                        quantity));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InventoryResponseDTO>> getLowStockItems() {

        return ResponseEntity.ok(
                inventoryService.getLowStockItems());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }
}