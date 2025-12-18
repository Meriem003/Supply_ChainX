package com.supplychainx.production.controller;

import com.supplychainx.production.dto.ProductionAvailabilityResponseDTO;
import com.supplychainx.production.dto.ProductionTimeResponseDTO;
import com.supplychainx.production.service.PlanningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/planning")
@RequiredArgsConstructor
@Tag(name = "Planning", description = "Gestion de la planification et ordonnancement de la production")
public class PlanningController {
    
    private final PlanningService planningService;

    @GetMapping("/check-availability")
    @Operation(summary = "Vérifier la disponibilité des matières",
            description = "Permet de vérifier si toutes les matières premières sont disponibles pour produire une quantité donnée d'un produit")
    public ResponseEntity<ProductionAvailabilityResponseDTO> checkMaterialAvailability(
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        ProductionAvailabilityResponseDTO response = planningService.checkMaterialAvailability(productId, quantity);
        return ResponseEntity.ok(response);
    }
    

    @GetMapping("/calculate-time")
    @Operation(summary = "Calculer le temps de production",
            description = "Permet de calculer le temps estimé de production pour une quantité donnée d'un produit")
    public ResponseEntity<ProductionTimeResponseDTO> calculateProductionTime(
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        ProductionTimeResponseDTO response = planningService.calculateProductionTime(productId, quantity);
        return ResponseEntity.ok(response);
    }
}
