package com.seveneleven.storeapp.model.dto;

import com.seveneleven.storeapp.model.entity.NotificationType;

public class NotificationRequestDTO {

    private Long userId;
    private NotificationType type;
    private Long orderId;
    private Long productId;
    private String message;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}