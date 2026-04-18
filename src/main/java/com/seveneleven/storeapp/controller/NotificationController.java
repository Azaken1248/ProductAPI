package com.seveneleven.storeapp.controller;

import com.seveneleven.storeapp.model.dto.NotificationRequestDTO;
import com.seveneleven.storeapp.model.dto.NotificationResponseDTO;
import com.seveneleven.storeapp.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService service;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public NotificationResponseDTO create(@RequestBody NotificationRequestDTO request) {
        return service.create(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<NotificationResponseDTO> getAll() {
        return service.getAll();
    }
    
    @GetMapping("/{id}")
    public NotificationResponseDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PreAuthorize("hasRole('ADMIN') or principal.id == #userId")
    @GetMapping("/user/{userId}")
    public List<NotificationResponseDTO> getByUser(@PathVariable Long userId) {
        return service.getByUser(userId);
    }

    @PutMapping("/{id}/read")
    public NotificationResponseDTO markAsRead(@PathVariable Long id) {
        return service.markAsRead(id);
    }
}