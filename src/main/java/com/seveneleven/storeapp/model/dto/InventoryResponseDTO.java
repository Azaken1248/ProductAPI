package com.seveneleven.storeapp.model.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponseDTO {

    private Long id;

    private Long productId;

    private Integer availableQuantity;

    private Integer threshold;

    private LocalDateTime updatedAt;
}