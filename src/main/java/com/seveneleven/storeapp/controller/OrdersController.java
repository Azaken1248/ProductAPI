package com.seveneleven.storeapp.controller;

import com.seveneleven.storeapp.model.dto.OrdersRequestDTO;
import com.seveneleven.storeapp.model.dto.OrdersResponseDTO;
import com.seveneleven.storeapp.service.OrdersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<OrdersResponseDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(ordersService.getOrderById(id));
    }

}
