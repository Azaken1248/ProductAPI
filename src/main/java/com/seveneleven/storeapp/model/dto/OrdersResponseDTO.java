package com.seveneleven.storeapp.model.dto;

import com.seveneleven.storeapp.model.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdersResponseDTO {
	private Long id;
	private String orderNumber;
	private Long userId;
	private LocalDateTime orderDate;
	private LocalDateTime createdAt;
	private OrderStatus status;
	private Double subtotalAmount;
	private Double totalAmount;
	private List<OrderItemResponseDTO> orderItems;
}
