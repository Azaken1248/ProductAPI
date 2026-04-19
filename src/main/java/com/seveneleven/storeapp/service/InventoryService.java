package com.seveneleven.storeapp.service;

import java.util.List;

import com.seveneleven.storeapp.model.dto.InventoryRequestDTO;
import com.seveneleven.storeapp.model.dto.InventoryResponseDTO;

public interface InventoryService {

    InventoryResponseDTO createInventory(
            InventoryRequestDTO requestDTO);

    List<InventoryResponseDTO> getAllInventory();

    InventoryResponseDTO getInventoryById(Long id);

    InventoryResponseDTO getInventoryByProductId(
            Long productId);

    InventoryResponseDTO updateQuantity(
            Long productId,
            int newQuantity);

    void reduceStock(
            Long productId,
            int quantity);

    List<InventoryResponseDTO> getLowStockItems();

    void deleteInventory(Long id);
}