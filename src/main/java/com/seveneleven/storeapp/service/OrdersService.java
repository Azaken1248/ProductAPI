package com.seveneleven.storeapp.service;

import com.seveneleven.storeapp.model.dto.OrdersRequestDTO;
import com.seveneleven.storeapp.model.dto.OrdersResponseDTO;

import java.util.List;

public interface OrdersService {
    OrdersResponseDTO createOrder(OrdersRequestDTO requestDTO);

    List<OrdersResponseDTO> getAllOrders();

    List<OrdersResponseDTO> getOrdersByUserId(Long userId);

    OrdersResponseDTO getOrderById(Long orderId);

    OrdersResponseDTO updateOrder(Long orderId, OrdersRequestDTO requestDTO);

    void deleteOrder(Long orderId);
}
