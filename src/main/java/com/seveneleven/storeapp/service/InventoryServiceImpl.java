package com.seveneleven.storeapp.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.seveneleven.storeapp.exceptions.ResourceNotFoundException;
import com.seveneleven.storeapp.exceptions.InsufficientStockException;
import com.seveneleven.storeapp.model.dto.InventoryRequestDTO;
import com.seveneleven.storeapp.model.dto.InventoryResponseDTO;
import com.seveneleven.storeapp.model.dto.NotificationRequestDTO;
import com.seveneleven.storeapp.model.entity.Inventory;
import com.seveneleven.storeapp.model.entity.Product;
import com.seveneleven.storeapp.model.entity.User;
import com.seveneleven.storeapp.model.entity.NotificationType;
import com.seveneleven.storeapp.repository.InventoryRepository;
import com.seveneleven.storeapp.repository.ProductRepository;
import com.seveneleven.storeapp.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    public InventoryResponseDTO createInventory(InventoryRequestDTO requestDTO) {
        log.debug("Creating inventory for product ID: {}", requestDTO.getProductId());
        if (requestDTO.getAvailableQuantity() < 0) { throw new IllegalArgumentException("Available quantity cannot be negative"); }
        if (requestDTO.getThreshold() < 0) { throw new IllegalArgumentException("Threshold cannot be negative"); }

        Product product = productRepository.findById(requestDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Inventory inventory = Inventory.builder()
                .product(product)
                .availableQuantity(requestDTO.getAvailableQuantity())
                .threshold(requestDTO.getThreshold())
                .updatedAt(LocalDateTime.now())
                .build();

        Inventory saved = inventoryRepository.save(inventory);
        log.info("Inventory created for product ID: {}", product.getId());
        return mapToResponse(saved);
    }

    @Override
    public List<InventoryResponseDTO> getAllInventory() {
        return inventoryRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public InventoryResponseDTO getInventoryById(Long id) {
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
        return mapToResponse(inventory);
    }

    @Override
    public InventoryResponseDTO getInventoryByProductId(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId).orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product"));
        return mapToResponse(inventory);
    }


    @Override
    @Transactional
    public InventoryResponseDTO updateQuantity(Long productId, int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

        inventory.setAvailableQuantity(newQuantity);
        inventory.setUpdatedAt(LocalDateTime.now());
        Inventory saved = inventoryRepository.save(inventory);

        checkLowStockAndNotify(saved);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void reduceStock(Long productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

        int currentStock = inventory.getAvailableQuantity();

        if (currentStock < quantity) {
            throw new InsufficientStockException("Insufficient stock. Only " + currentStock + " left for Product ID: " + productId);
        }

        inventory.setAvailableQuantity(currentStock - quantity);
        inventory.setUpdatedAt(LocalDateTime.now());
        Inventory saved = inventoryRepository.save(inventory);

        checkLowStockAndNotify(saved);
    }

    @Override
    public List<InventoryResponseDTO> getLowStockItems() {
        return inventoryRepository.findLowStockItems().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public void deleteInventory(Long id) {
        inventoryRepository.deleteById(id);
    }

    private void checkLowStockAndNotify(Inventory inventory) {
        if (inventory.getAvailableQuantity() <= inventory.getThreshold()) {
            
            List<User> admins = userRepository.findAll().stream()
                    .filter(u -> u.getRole() != null && u.getRole().toUpperCase().contains("ADMIN"))
                    .collect(Collectors.toList());

            if (admins.isEmpty()) {
                log.warn("No ADMIN users found to receive low stock alert!");
                return;
            }

            for (User admin : admins) {
                NotificationRequestDTO request = new NotificationRequestDTO();
                request.setUserId(admin.getId()); 
                request.setType(NotificationType.LOW_STOCK);
                request.setProductId(inventory.getProduct().getId());
                request.setMessage("Low stock alert for product: " + inventory.getProduct().getName() + ". Remaining quantity: " + inventory.getAvailableQuantity());

                notificationService.create(request);
            }
            
            log.warn("LOW STOCK notifications dispatched for product ID: {}", inventory.getProduct().getId());
        }
    }

    private InventoryResponseDTO mapToResponse(Inventory inventory) {
        return InventoryResponseDTO.builder()
                .id(inventory.getId())
                .productId(inventory.getProduct().getId())
                .availableQuantity(inventory.getAvailableQuantity())
                .threshold(inventory.getThreshold())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}