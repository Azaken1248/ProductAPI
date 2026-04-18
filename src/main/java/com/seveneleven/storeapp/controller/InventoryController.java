package com.seveneleven.storeapp.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.seveneleven.storeapp.model.entity.Inventory;
import com.seveneleven.storeapp.service.InventoryService;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // Create Inventory
    @PostMapping
    public Inventory createInventory(
            @RequestBody Inventory inventory) {

        return inventoryService.saveInventory(inventory);
    }

    // Get All Inventory
    @GetMapping
    public List<Inventory> getAllInventory() {

        return inventoryService.getAllInventory();
    }

    // Get Inventory By ID
    @GetMapping("/{id}")
    public Inventory getInventoryById(
            @PathVariable Long id) {

        return inventoryService.getInventoryById(id);
    }

    // Get Inventory By Product ID
    @GetMapping("/product/{productId}")
    public Inventory getInventoryByProductId(
            @PathVariable Long productId) {

        return inventoryService
                .getInventoryByProductId(productId);
    }

    // Update Quantity
    @PutMapping("/quantity/{productId}")
    public Inventory updateQuantity(
            @PathVariable Long productId,
            @RequestParam int quantity) {

        return inventoryService
                .updateQuantity(productId, quantity);
    }

    // Reduce Stock (used when order placed)
    @PutMapping("/reduce/{productId}")
    public void reduceStock(
            @PathVariable Long productId,
            @RequestParam int quantity) {

        inventoryService
                .reduceStock(productId, quantity);
    }

    // Get Low Stock Items
    @GetMapping("/low-stock")
    public List<Inventory> getLowStockItems() {

        return inventoryService.getLowStockItems();
    }

    // Delete Inventory
    @DeleteMapping("/{id}")
    public void deleteInventory(
            @PathVariable Long id) {

        inventoryService.deleteInventory(id);
    }
}