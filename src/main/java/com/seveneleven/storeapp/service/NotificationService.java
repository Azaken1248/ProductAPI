package com.seveneleven.storeapp.service;

import com.seveneleven.storeapp.model.dto.NotificationRequestDTO;
import com.seveneleven.storeapp.model.dto.NotificationResponseDTO;

import java.util.List;

public interface NotificationService {
    
    NotificationResponseDTO create(NotificationRequestDTO request);
    
    List<NotificationResponseDTO> getAll();
    
    NotificationResponseDTO getById(Long id);
    
    List<NotificationResponseDTO> getByUser(Long userId);
    
    NotificationResponseDTO markAsRead(Long id);
}