package com.supplychainx.livraison.controller;

import com.supplychainx.common.enums.UserRole;
import com.supplychainx.livraison.dto.DeliveryRequestDTO;
import com.supplychainx.livraison.dto.DeliveryResponseDTO;
import com.supplychainx.livraison.service.DeliveryService;
import com.supplychainx.security.RequiresRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@Tag(name = "Deliveries", description = "Gestion des livraisons")
public class DeliveryController {
    
    private final DeliveryService deliveryService;
    

    @PostMapping
    @RequiresRole(UserRole.RESPONSABLE_LOGISTIQUE)
    @Operation(summary = "Créer une livraison",
            description = "Permet de créer une livraison pour une commande avec calcul automatique du coût total")
    public ResponseEntity<DeliveryResponseDTO> createDelivery(@Valid @RequestBody DeliveryRequestDTO dto) {
        DeliveryResponseDTO delivery = deliveryService.createDelivery(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(delivery);
    }
}
