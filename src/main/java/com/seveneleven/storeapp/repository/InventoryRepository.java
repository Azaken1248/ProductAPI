package com.seveneleven.storeapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.seveneleven.storeapp.model.entity.Inventory;

public interface InventoryRepository
        extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    @Query("""
           SELECT i
           FROM Inventory i
           WHERE i.availableQuantity <= i.threshold
           """)
    List<Inventory> findLowStockItems();
}