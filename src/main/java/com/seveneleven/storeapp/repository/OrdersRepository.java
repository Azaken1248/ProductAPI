package com.seveneleven.storeapp.repository;

import com.seveneleven.storeapp.model.entity.Orders;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
    boolean existsByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = "orderItems")
    List<Orders> findByUserId(Long userId);

    @Override
    @EntityGraph(attributePaths = "orderItems")
    List<Orders> findAll();

    @Override
    @EntityGraph(attributePaths = "orderItems")
    Optional<Orders> findById(Long id);
}