package com.seveneleven.storeapp.service;

import com.seveneleven.storeapp.exceptions.DuplicateSkuException;
import com.seveneleven.storeapp.exceptions.ResourceNotFoundException;
import com.seveneleven.storeapp.model.dto.ProductRequestDTO;
import com.seveneleven.storeapp.model.dto.ProductResponseDTO;
import com.seveneleven.storeapp.model.entity.Notification;
import com.seveneleven.storeapp.model.entity.Product;
import com.seveneleven.storeapp.repository.NotificationRepository;
import com.seveneleven.storeapp.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final NotificationRepository notificationRepository; 

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO) {
        log.debug("Creating new product with SKU: {}", requestDTO.getSku());

        if (productRepository.existsBySku(requestDTO.getSku())) {
            throw new DuplicateSkuException("Product with SKU " + requestDTO.getSku() + " already exists.");
        }

        Product product = Product.builder()
                .sku(requestDTO.getSku())
                .name(requestDTO.getName())
                .description(requestDTO.getDescription())
                .category(requestDTO.getCategory())
                .price(requestDTO.getPrice())
                .isActive(requestDTO.getIsActive() != null ? requestDTO.getIsActive() : true)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Successfully created product ID: {}", savedProduct.getId());
        
        return mapToResponseDTO(savedProduct);
    }

    @Override
    public List<ProductResponseDTO> getAllProducts(boolean includeInactive) {
        log.debug("Fetching all products. Include inactive: {}", includeInactive);
        List<Product> products = includeInactive ? 
                productRepository.findAll() : 
                productRepository.findAllByIsActiveTrue();

        return products.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponseDTO getProductById(Long id) {
        log.debug("Fetching product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return mapToResponseDTO(product);
    }

    @Override
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO requestDTO) {
        log.debug("Updating product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        if (!product.getSku().equalsIgnoreCase(requestDTO.getSku()) && productRepository.existsBySku(requestDTO.getSku())) {
            throw new DuplicateSkuException("Product with SKU " + requestDTO.getSku() + " already exists.");
        }

        product.setSku(requestDTO.getSku());
        product.setName(requestDTO.getName());
        product.setDescription(requestDTO.getDescription());
        product.setCategory(requestDTO.getCategory());
        product.setPrice(requestDTO.getPrice());
        
        if (requestDTO.getIsActive() != null) {
            product.setIsActive(requestDTO.getIsActive());
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Successfully updated product ID: {}", updatedProduct.getId());
        
        return mapToResponseDTO(updatedProduct);
    }

    @Override
    @Transactional 
    public void deleteProduct(Long id) {
        log.debug("Hard deleting product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        List<Notification> linkedNotifications = notificationRepository.findByProductId(id);
        for (Notification notification : linkedNotifications) {
            notification.setProduct(null);
            notificationRepository.save(notification);
        }

        productRepository.delete(product);
        log.info("Successfully physically deleted product ID: {}", id);
    }

    private ProductResponseDTO mapToResponseDTO(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .price(product.getPrice())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}