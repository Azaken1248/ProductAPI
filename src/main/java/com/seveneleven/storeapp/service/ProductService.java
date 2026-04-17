package com.seveneleven.storeapp.service;

import com.seveneleven.storeapp.model.dto.ProductRequestDTO;
import com.seveneleven.storeapp.model.dto.ProductResponseDTO;

import java.util.List;

public interface ProductService {
    ProductResponseDTO createProduct(ProductRequestDTO requestDTO);
    List<ProductResponseDTO> getAllProducts(boolean includeInactive);
    ProductResponseDTO getProductById(Long id);
    ProductResponseDTO updateProduct(Long id, ProductRequestDTO requestDTO);
    void deleteProduct(Long id);
}