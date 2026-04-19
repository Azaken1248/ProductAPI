package com.seveneleven.storeapp.controller;

import com.seveneleven.storeapp.model.dto.OrderItemResponseDTO;
import com.seveneleven.storeapp.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderItemController {

    private final OrderItemService orderItemService;

    @GetMapping("/{orderId}/items")
    public ResponseEntity<List<OrderItemResponseDTO>> getItemsByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderItemService.getItemsByOrderId(orderId));
    }
}
