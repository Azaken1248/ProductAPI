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

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CheckoutController {

    private final OrdersService ordersService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrdersResponseDTO> checkout(@Valid @RequestBody OrdersRequestDTO requestDTO) {
        OrdersResponseDTO response = ordersService.createOrder(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}