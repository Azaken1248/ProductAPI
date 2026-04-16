package com.seveneleven.storeapp.service;

import com.seveneleven.storeapp.model.dto.UserRequestDTO;
import com.seveneleven.storeapp.model.dto.UserResponseDTO;

import java.util.List;

public interface UserService {

    UserResponseDTO createUser(UserRequestDTO requestDTO);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserById(Long userId);

    UserResponseDTO updateUser(Long userId, UserRequestDTO requestDTO);

    void deleteUser(Long userId);

}