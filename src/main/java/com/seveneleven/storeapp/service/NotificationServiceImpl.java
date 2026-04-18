package com.seveneleven.storeapp.service;

import com.seveneleven.storeapp.model.dto.NotificationRequestDTO;
import com.seveneleven.storeapp.model.dto.NotificationResponseDTO;
import com.seveneleven.storeapp.model.entity.*;
import com.seveneleven.storeapp.repository.*;
import com.seveneleven.storeapp.exceptions.ResourceNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired private NotificationRepository notificationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private OrdersRepository ordersRepository;
    @Autowired private ProductRepository productRepository;

    @Override
    public NotificationResponseDTO create(NotificationRequestDTO request) {

        logger.info("Creating notification: userId={}, type={}", request.getUserId(), request.getType());

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
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Orders order = null;
        if (request.getOrderId() != null) {
            order = ordersRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));
        }

        Product product = null;
        if (request.getProductId() != null) {
            product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
        }

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(request.getType());
        notification.setMessage(request.getMessage());
        notification.setOrder(order);
        notification.setProduct(product);
        notification.setStatus(NotificationStatus.PENDING);

        Notification saved = notificationRepository.save(notification);
        logger.info("Notification created successfully: id={}, userId={}, type={}", saved.getId(), request.getUserId(), request.getType());

        return mapToDTO(saved);
    }

    @Override
    public List<NotificationResponseDTO> getAll() {
        return notificationRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public NotificationResponseDTO getById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        
        verifyOwnershipOrAdmin(notification.getUser().getId());
        return mapToDTO(notification);
    }

    @Override
    public List<NotificationResponseDTO> getByUser(Long userId) {
        return notificationRepository.findByUserId(userId).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public NotificationResponseDTO markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        verifyOwnershipOrAdmin(notification.getUser().getId());

        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());

        Notification updated = notificationRepository.save(notification);
        logger.info("Notification marked as READ: id={}", id);

        return mapToDTO(updated);
    }

    private void verifyOwnershipOrAdmin(Long targetUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof com.seveneleven.storeapp.security.UserDetailsImpl) {
            com.seveneleven.storeapp.security.UserDetailsImpl userDetails = (com.seveneleven.storeapp.security.UserDetailsImpl) auth.getPrincipal();
            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin && !userDetails.getId().equals(targetUserId)) {
                throw new AccessDeniedException("IDOR Prevention: You cannot access or modify resources belonging to another user.");
            }
        }
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
        if (n.getOrder() != null) dto.setOrderId(n.getOrder().getId());
        if (n.getProduct() != null) dto.setProductId(n.getProduct().getId());
        return dto;
    }
}