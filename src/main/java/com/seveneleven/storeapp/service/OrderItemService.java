package com.seveneleven.storeapp.service;

import com.seveneleven.storeapp.model.dto.OrderItemResponseDTO;

import java.util.List;

public interface OrderItemService {
    List<OrderItemResponseDTO> getItemsByOrderId(Long orderId);
}
