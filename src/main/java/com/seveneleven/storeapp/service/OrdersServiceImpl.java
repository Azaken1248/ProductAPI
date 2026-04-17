package com.seveneleven.storeapp.service;

import com.seveneleven.storeapp.exceptions.ResourceNotFoundException;
import com.seveneleven.storeapp.model.dto.OrderItemRequestDTO;
import com.seveneleven.storeapp.model.dto.OrderItemResponseDTO;
import com.seveneleven.storeapp.model.dto.OrdersRequestDTO;
import com.seveneleven.storeapp.model.dto.OrdersResponseDTO;
import com.seveneleven.storeapp.model.entity.OrderItem;
import com.seveneleven.storeapp.model.entity.Orders;
import com.seveneleven.storeapp.model.entity.OrderStatus;
import com.seveneleven.storeapp.model.entity.User;
import com.seveneleven.storeapp.repository.OrderItemRepository;
import com.seveneleven.storeapp.repository.OrdersRepository;
import com.seveneleven.storeapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OrdersResponseDTO createOrder(OrdersRequestDTO requestDTO) {
        User user = getActiveUser(requestDTO.getUserId());

        Orders order = Orders.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .subtotalAmount(0.0)
                .totalAmount(0.0)
                .build();

        attachItems(order, requestDTO.getOrderItems());
        double subtotal = calculateSubtotal(order.getOrderItems());
        order.setSubtotalAmount(subtotal);
        order.setTotalAmount(subtotal);

        Orders saved = ordersRepository.save(order);
        log.info("Order created with ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    public List<OrdersResponseDTO> getAllOrders() {
        return ordersRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrdersResponseDTO> getOrdersByUserId(Long userId) {
        return ordersRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrdersResponseDTO getOrderById(Long orderId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        return mapToResponse(order);
    }

    @Override
    @Transactional
    public OrdersResponseDTO updateOrder(Long orderId, OrdersRequestDTO requestDTO) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        User user = getActiveUser(requestDTO.getUserId());
        order.setUser(user);

        if (!order.getOrderItems().isEmpty()) {
            orderItemRepository.deleteAll(order.getOrderItems());
        }
        order.getOrderItems().clear();
        attachItems(order, requestDTO.getOrderItems());
        double subtotal = calculateSubtotal(order.getOrderItems());
        order.setSubtotalAmount(subtotal);
        order.setTotalAmount(subtotal);

        Orders updated = ordersRepository.save(order);
        log.info("Order updated with ID: {}", updated.getId());
        return mapToResponse(updated);
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
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Inactive user cannot place an order");
        }
        return user;
    }

    private void attachItems(Orders order, List<OrderItemRequestDTO> requestedItems) {
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemRequestDTO item : requestedItems) {
            OrderItem newItem = OrderItem.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .lineTotal(item.getQuantity() * item.getUnitPrice())
                    .order(order)
                    .build();
            items.add(newItem);
        }
        order.setOrderItems(items);
    }

    private double calculateSubtotal(List<OrderItem> items) {
        return items.stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
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
                .productId(item.getProductId())
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
}