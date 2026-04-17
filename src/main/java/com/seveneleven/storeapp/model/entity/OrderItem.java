package com.seveneleven.storeapp.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;

    @NotNull(message = "Product id is required")
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Positive(message = "Quantity must be greater than zero")
    @Column(name = "quantity", nullable = false)
    private int quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Unit price must be non-negative")
    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Line total must be non-negative")
    @Column(name = "line_total", nullable = false)
    private Double lineTotal;

    @PrePersist
    @PreUpdate
    protected void calculateLineTotal() {
        if (unitPrice != null) {
            lineTotal = quantity * unitPrice;
        }
    }
}