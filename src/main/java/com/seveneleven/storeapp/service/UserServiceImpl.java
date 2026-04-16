package com.seveneleven.storeapp.service;

import com.seveneleven.storeapp.exceptions.DuplicateEmailException;
import com.seveneleven.storeapp.exceptions.ResourceNotFoundException;
import com.seveneleven.storeapp.model.dto.UserRequestDTO;
import com.seveneleven.storeapp.model.dto.UserResponseDTO;
import com.seveneleven.storeapp.model.entity.User;
import com.seveneleven.storeapp.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    private boolean isCallerAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @Override
    public UserResponseDTO createUser(UserRequestDTO requestDTO) {
        log.debug("Creating user with email: {}", requestDTO.getEmail());

        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateEmailException("Email already registered: " + requestDTO.getEmail());
        }

        String assignedRole = "CUSTOMER"; 
        
        if (isCallerAdmin() && requestDTO.getRole() != null) {
            assignedRole = requestDTO.getRole().toUpperCase();
        } else if (requestDTO.getRole() != null && requestDTO.getRole().equalsIgnoreCase("ADMIN")) {
            log.warn("Unauthorized attempt to register an ADMIN account by: {}", requestDTO.getEmail());
        }

        User user = User.builder()
                .firstName(requestDTO.getFirstName())
                .lastName(requestDTO.getLastName())
                .email(requestDTO.getEmail())
                .password(passwordEncoder.encode(requestDTO.getPassword()))
                .phone(requestDTO.getPhone())
                .role(assignedRole)
                .status(requestDTO.getStatus() != null ? requestDTO.getStatus().toUpperCase() : "ACTIVE")
                .build();

        User saved = userRepository.save(user);
        log.info("User created with ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO getUserById(Long userId) {
        log.debug("Fetching user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return mapToResponse(user);
    }

    @Override
    public UserResponseDTO updateUser(Long userId, UserRequestDTO requestDTO) {
        log.debug("Updating user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (!user.getEmail().equals(requestDTO.getEmail())
                && userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateEmailException("Email already in use: " + requestDTO.getEmail());
        }

        user.setFirstName(requestDTO.getFirstName());
        user.setLastName(requestDTO.getLastName());
        user.setEmail(requestDTO.getEmail());
        
        if (requestDTO.getPassword() != null && !requestDTO.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        }
        
        user.setPhone(requestDTO.getPhone());
        
        if (isCallerAdmin() && requestDTO.getRole() != null) {
            user.setRole(requestDTO.getRole().toUpperCase());
        } else if (requestDTO.getRole() != null && !user.getRole().equalsIgnoreCase(requestDTO.getRole())) {
            log.warn("Unauthorized attempt: User {} attempted to change their own role to {}", user.getEmail(), requestDTO.getRole());
        }

        user.setStatus(requestDTO.getStatus() != null ? requestDTO.getStatus().toUpperCase() : user.getStatus());

        User updated = userRepository.save(user);
        log.info("User updated with ID: {}", updated.getId());
        return mapToResponse(updated);
    }

    @Override
    public void deleteUser(Long userId) {
        log.debug("Deleting user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        userRepository.delete(user);
        log.info("User deleted with ID: {}", userId);
    }

    private UserResponseDTO mapToResponse(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}