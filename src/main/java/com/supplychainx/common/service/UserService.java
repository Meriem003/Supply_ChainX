package com.supplychainx.common.service;

import com.supplychainx.common.dto.UpdateRoleDTO;
import com.supplychainx.common.dto.UserCreateDTO;
import com.supplychainx.common.dto.UserResponseDTO;
import com.supplychainx.common.entity.User;
import com.supplychainx.common.repository.UserRepository;
import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.ResourceNotFoundException;
import com.supplychainx.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponseDTO createUser(UserCreateDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new BusinessRuleException("Un utilisateur avec cet email existe déjà");
        }

        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setRole(dto.getRole());

        user = userRepository.save(user);

        return userMapper.toResponseDTO(user);
    }

    public UserResponseDTO updateUserRole(Long userId, UpdateRoleDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + userId));

        user.setRole(dto.getRole());

        user = userRepository.save(user);

        return userMapper.toResponseDTO(user);
    }
}