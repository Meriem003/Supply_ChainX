package com.supplychainx.common.controller;

import com.supplychainx.common.dto.UpdateRoleDTO;
import com.supplychainx.common.dto.UserCreateDTO;
import com.supplychainx.common.dto.UserResponseDTO;
import com.supplychainx.common.enums.UserRole;
import com.supplychainx.common.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs", description = "Gestion des utilisateurs et des rôles")
public class UserController {

    private final UserService userService;


    @PostMapping
    @Operation(summary = "Créer un utilisateur", description = "Permet à un admin de créer un nouveau compte utilisateur avec un rôle spécifique")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateDTO dto) {
        UserResponseDTO user = userService.createUser(dto);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PutMapping("/{userId}/role")
    @Operation(summary = "Modifier le rôle d'un utilisateur", description = "Permet à un admin de modifier le rôle d'un utilisateur existant")
    public ResponseEntity<UserResponseDTO> updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateRoleDTO dto) {
        UserResponseDTO user = userService.updateUserRole(userId, dto);
        return ResponseEntity.ok(user);
    }
}

