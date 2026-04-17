package com.seveneleven.storeapp.service;

import com.seveneleven.storeapp.model.dto.NotificationRequestDTO;
import com.seveneleven.storeapp.model.dto.NotificationResponseDTO;
import com.seveneleven.storeapp.model.entity.*;
import com.seveneleven.storeapp.repository.*;
import com.seveneleven.storeapp.exceptions.ResourceNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired private NotificationRepository notificationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private OrdersRepository ordersRepository;
    @Autowired private ProductRepository productRepository;

    public NotificationResponseDTO create(NotificationRequestDTO request) {

        logger.info("Creating notification: userId={}, type={}",
                request.getUserId(), request.getType());

        if (request.getType() == NotificationType.CHECKOUT_CONFIRMATION && request.getOrderId() == null) {
            throw new IllegalArgumentException("Order is required for CHECKOUT_CONFIRMATION");
        }

        if (request.getType() == NotificationType.LOW_STOCK && request.getProductId() == null) {
            throw new IllegalArgumentException("Product is required for LOW_STOCK");
        }

        if (request.getType() == NotificationType.CHECKOUT_CONFIRMATION && request.getProductId() != null) {
            throw new IllegalArgumentException("Product should not be provided for CHECKOUT_CONFIRMATION");
        }

        if (request.getType() == NotificationType.LOW_STOCK && request.getOrderId() != null) {
            throw new IllegalArgumentException("Order should not be provided for LOW_STOCK");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> {
                    logger.error("User not found: id={}", request.getUserId());
                    return new ResourceNotFoundException("User not found with id: " + request.getUserId());
                });

        Orders order = null;
        if (request.getOrderId() != null) {
            order = ordersRepository.findById(request.getOrderId())
                    .orElseThrow(() -> {
                        logger.error("Order not found: id={}", request.getOrderId());
                        return new ResourceNotFoundException("Order not found with id: " + request.getOrderId());
                    });
        }

        Product product = null;
        if (request.getProductId() != null) {
            product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> {
                        logger.error("Product not found: id={}", request.getProductId());
                        return new ResourceNotFoundException("Product not found with id: " + request.getProductId());
                    });
        }

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(request.getType());
        notification.setMessage(request.getMessage());
        notification.setOrder(order);
        notification.setProduct(product);
        notification.setStatus(NotificationStatus.PENDING);

        Notification saved = notificationRepository.save(notification);

        logger.info("Notification created successfully: id={}, userId={}, type={}",
                saved.getId(), request.getUserId(), request.getType());

        return mapToDTO(saved);
    }

    public List<NotificationResponseDTO> getAll() {
        logger.info("Fetching all notifications");

        return notificationRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public NotificationResponseDTO getById(Long id) {
        logger.info("Fetching notification: id={}", id);

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Notification not found: id={}", id);
                    return new ResourceNotFoundException("Notification not found with id: " + id);
                });

        return mapToDTO(notification);
    }

    public List<NotificationResponseDTO> getByUser(Long userId) {
        logger.info("Fetching notifications for userId={}", userId);

        return notificationRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public NotificationResponseDTO markAsRead(Long id) {
        logger.info("Marking notification as READ: id={}", id);

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Notification not found: id={}", id);
                    return new ResourceNotFoundException("Notification not found with id: " + id);
                });

        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());

        Notification updated = notificationRepository.save(notification);

        logger.info("Notification marked as READ: id={}", id);

        return mapToDTO(updated);
    }

    private NotificationResponseDTO mapToDTO(Notification n) {

        NotificationResponseDTO dto = new NotificationResponseDTO();

        dto.setId(n.getId());
        dto.setUserId(n.getUser() != null ? n.getUser().getId() : null);
        dto.setType(n.getType());
        dto.setMessage(n.getMessage());
        dto.setStatus(n.getStatus());
        dto.setCreatedAt(n.getCreatedAt());
        dto.setSentAt(n.getSentAt());
        dto.setReadAt(n.getReadAt());

        if (n.getOrder() != null)
            dto.setOrderId(n.getOrder().getId());

        if (n.getProduct() != null)
            dto.setProductId(n.getProduct().getId());

        return dto;
    }
}