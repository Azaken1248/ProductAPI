package com.seveneleven.storeapp.controller;

import com.seveneleven.storeapp.model.dto.OrdersRequestDTO;
import com.seveneleven.storeapp.model.dto.OrdersResponseDTO;
import com.seveneleven.storeapp.service.OrdersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrdersController {

    private final OrdersService ordersService;

    @PostMapping
    public ResponseEntity<OrdersResponseDTO> createOrder(@Valid @RequestBody OrdersRequestDTO requestDTO) {
        OrdersResponseDTO response = ordersService.createOrder(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<OrdersResponseDTO>> getAllOrders() {
        return ResponseEntity.ok(ordersService.getAllOrders());
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrdersResponseDTO>> getOrdersByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ordersService.getOrdersByUserId(userId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OrdersResponseDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(ordersService.getOrderById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<OrdersResponseDTO> updateOrder(@PathVariable Long id, @Valid @RequestBody OrdersRequestDTO requestDTO) {
        return ResponseEntity.ok(ordersService.updateOrder(id, requestDTO));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        ordersService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}