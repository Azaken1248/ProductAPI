package com.seveneleven.storeapp.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "inventory",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_inventory_product",
                          columnNames = "product_id")
    }
)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(
        name = "product_id",
        nullable = false,
        unique = true
    )
    private Product product;

    @Column(
        name = "available_quantity",
        nullable = false
    )
    private Integer availableQuantity;


    @Column(
        name = "threshold",
        nullable = false
    )
    private Integer threshold;

    @Column(
        name = "updated_at",
        nullable = false
    )
    private LocalDateTime updatedAt;

}