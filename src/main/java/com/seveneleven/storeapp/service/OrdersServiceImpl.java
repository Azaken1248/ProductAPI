package com.seveneleven.storeapp.service;

import com.seveneleven.storeapp.exceptions.ResourceNotFoundException;
import com.seveneleven.storeapp.model.dto.*;
import com.seveneleven.storeapp.model.entity.*;
import com.seveneleven.storeapp.repository.OrdersRepository;
import com.seveneleven.storeapp.repository.ProductRepository;
import com.seveneleven.storeapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdersServiceImpl implements OrdersService {

    private final OrdersRepository ordersRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final InventoryService inventoryService;

    @Override
    @Transactional
    public OrdersResponseDTO createOrder(OrdersRequestDTO requestDTO) {
        verifyOwnershipOrAdmin(requestDTO.getUserId());
        User user = getActiveUser(requestDTO.getUserId());

        Orders order = Orders.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .subtotalAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .build();

        List<OrderItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItemRequestDTO itemDTO : requestDTO.getOrderItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + itemDTO.getProductId()));
            
            if (!Boolean.TRUE.equals(product.getIsActive())) {
                throw new IllegalArgumentException("Product is inactive and cannot be ordered: " + product.getSku());
            }

            inventoryService.reduceStock(product.getId(), itemDTO.getQuantity());

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            subtotal = subtotal.add(lineTotal);

            OrderItem newItem = OrderItem.builder()
                    .product(product) 
                    .quantity(itemDTO.getQuantity())
                    .unitPrice(product.getPrice())
                    .lineTotal(lineTotal)
                    .order(order)
                    .build();
            items.add(newItem);
        }

        order.setOrderItems(items);
        order.setSubtotalAmount(subtotal);
        order.setTotalAmount(subtotal);

        Orders saved = ordersRepository.save(order);
        
        NotificationRequestDTO notifRequest = new NotificationRequestDTO();
        notifRequest.setUserId(user.getId());
        notifRequest.setType(NotificationType.CHECKOUT_CONFIRMATION);
        notifRequest.setOrderId(saved.getId());
        notifRequest.setMessage("Order " + saved.getOrderNumber() + " has been placed successfully.");
        notificationService.create(notifRequest);

        log.info("Order created with ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    public List<OrdersResponseDTO> getAllOrders() {
        return ordersRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<OrdersResponseDTO> getOrdersByUserId(Long userId) {
        return ordersRepository.findByUserId(userId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public OrdersResponseDTO getOrderById(Long orderId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        verifyOwnershipOrAdmin(order.getUser().getId());
        return mapToResponse(order);
    }

    @Override
    @Transactional
    public OrdersResponseDTO updateOrder(Long orderId, OrdersRequestDTO requestDTO) {
        throw new UnsupportedOperationException("Order modification after placement is restricted to preserve inventory integrity.");
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        ordersRepository.delete(order);
        log.info("Order deleted with ID: {}", orderId);
    }

    private User getActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new com.seveneleven.storeapp.exceptions.InactiveUserException("Inactive user cannot place an order");
        }
        return user;
    }

    private OrdersResponseDTO mapToResponse(Orders order) {
        return OrdersResponseDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .orderDate(order.getOrderDate())
                .createdAt(order.getCreatedAt())
                .status(order.getStatus())
                .subtotalAmount(order.getSubtotalAmount())
                .totalAmount(order.getTotalAmount())
                .orderItems(order.getOrderItems().stream()
                        .map(this::mapToItemResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private OrderItemResponseDTO mapToItemResponse(OrderItem item) {
        return OrderItemResponseDTO.builder()
                .id(item.getId())
                .productId(item.getProduct().getId()) 
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getLineTotal())
                .build();
    }

    private String generateOrderNumber() {
        String value;
        do {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            int random = (int) (Math.random() * 9000) + 1000;
            value = "ORD-" + timestamp + "-" + random;
        } while (ordersRepository.existsByOrderNumber(value));
        return value;
    }
    
    private void verifyOwnershipOrAdmin(Long targetUserId) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof com.seveneleven.storeapp.security.UserDetailsImpl) {
            com.seveneleven.storeapp.security.UserDetailsImpl userDetails = (com.seveneleven.storeapp.security.UserDetailsImpl) auth.getPrincipal();
            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin && !userDetails.getId().equals(targetUserId)) {
                throw new org.springframework.security.access.AccessDeniedException("IDOR Prevention: You cannot access or modify resources belonging to another user.");
            }
        }
    }
}