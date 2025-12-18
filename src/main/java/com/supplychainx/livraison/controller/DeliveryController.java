package com.supplychainx.livraison.controller;

import com.supplychainx.livraison.dto.DeliveryRequestDTO;
import com.supplychainx.livraison.dto.DeliveryResponseDTO;
import com.supplychainx.livraison.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@Tag(name = "Deliveries", description = "Gestion des livraisons")
public class DeliveryController {
    
    private final DeliveryService deliveryService;
    

    @PostMapping
    @Operation(summary = "Créer une livraison",
            description = "Permet de créer une livraison pour une commande avec calcul automatique du coût total")
    public ResponseEntity<DeliveryResponseDTO> createDelivery(@Valid @RequestBody DeliveryRequestDTO dto) {
        DeliveryResponseDTO delivery = deliveryService.createDelivery(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(delivery);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une livraison par ID",
            description = "Récupère les détails d'une livraison spécifique")
    public ResponseEntity<DeliveryResponseDTO> getDeliveryById(@PathVariable Long id) {
        DeliveryResponseDTO delivery = deliveryService.getDeliveryById(id);
        return ResponseEntity.ok(delivery);
    }
    
    @GetMapping
    @Operation(summary = "Obtenir les livraisons par statut",
            description = "Récupère toutes les livraisons filtrées par statut")
    public ResponseEntity<List<DeliveryResponseDTO>> getDeliveriesByStatus(@RequestParam String status) {
        List<DeliveryResponseDTO> deliveries = deliveryService.getDeliveriesByStatus(status);
        return ResponseEntity.ok(deliveries);
    }
    
    @PutMapping("/{id}/status")
    @Operation(summary = "Mettre à jour le statut d'une livraison",
            description = "Change le statut d'une livraison et met à jour la commande si nécessaire")
    public ResponseEntity<DeliveryResponseDTO> updateDeliveryStatus(
            @PathVariable Long id, 
            @RequestBody Map<String, String> statusUpdate) {
        String newStatus = statusUpdate.get("status");
        DeliveryResponseDTO delivery = deliveryService.updateDeliveryStatus(id, newStatus);
        return ResponseEntity.ok(delivery);
    }
    
    @PostMapping("/{id}/calculate-cost")
    @Operation(summary = "Calculer et mettre à jour le coût de livraison",
            description = "Calcule le coût de livraison basé sur la distance et le tarif")
    public ResponseEntity<DeliveryResponseDTO> calculateDeliveryCost(
            @PathVariable Long id,
            @RequestBody Map<String, Double> costParams) {
        Double baseCost = costParams.get("baseCost");
        Double distance = costParams.get("distance");
        Double ratePerKm = costParams.get("ratePerKm");
        DeliveryResponseDTO delivery = deliveryService.calculateAndUpdateCost(id, baseCost, distance, ratePerKm);
        return ResponseEntity.ok(delivery);
    }
}
