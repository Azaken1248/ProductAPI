package com.seveneleven.storeapp.model.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdersRequestDTO {

	@NotNull(message = "User id is required")
	private Long userId;

	@NotEmpty(message = "Order items list cannot be empty")
    @Size(min = 1, message = "At least one order item is required")
    @Valid
	private List<OrderItemRequestDTO> orderItems;
}
