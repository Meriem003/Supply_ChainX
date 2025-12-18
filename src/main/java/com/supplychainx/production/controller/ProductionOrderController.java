package com.supplychainx.production.controller;

import com.supplychainx.production.dto.ProductionOrderCreateDTO;
import com.supplychainx.production.dto.ProductionOrderResponseDTO;
import com.supplychainx.production.dto.ProductionOrderUpdateDTO;
import com.supplychainx.production.service.ProductionOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/production-orders")
@RequiredArgsConstructor
@Tag(name = "Ordres de Production", description = "Gestion des ordres de production (Module Production)")
public class ProductionOrderController {

    private final ProductionOrderService productionOrderService;

    @PostMapping
    @Operation(summary = "Créer un ordre de production", 
               description = "Permet au chef de production de créer un nouvel ordre")
    public ResponseEntity<ProductionOrderResponseDTO> createProductionOrder(@Valid @RequestBody ProductionOrderCreateDTO dto) {
        ProductionOrderResponseDTO created = productionOrderService.createProductionOrder(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un ordre de production", 
               description = "Permet au chef de production de modifier un ordre existant")
    public ResponseEntity<ProductionOrderResponseDTO> updateProductionOrder(
            @PathVariable Long id,
            @Valid @RequestBody ProductionOrderUpdateDTO dto) {
        ProductionOrderResponseDTO updated = productionOrderService.updateProductionOrder(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Annuler un ordre de production", 
               description = "Permet au chef de production d'annuler un ordre non commencé")
    public ResponseEntity<Void> cancelProductionOrder(@PathVariable Long id) {
        productionOrderService.cancelProductionOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Consulter tous les ordres", 
               description = "Permet au superviseur production de consulter tous les ordres")
    public ResponseEntity<List<ProductionOrderResponseDTO>> getAllProductionOrders() {
        List<ProductionOrderResponseDTO> orders = productionOrderService.getAllProductionOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Filtrer les ordres par statut", 
               description = "Permet au superviseur production de suivre les ordres selon leur statut (EN_ATTENTE, EN_PRODUCTION, TERMINE, BLOQUE)")
    public ResponseEntity<List<ProductionOrderResponseDTO>> getProductionOrdersByStatus(@PathVariable String status) {
        List<ProductionOrderResponseDTO> orders = productionOrderService.getProductionOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un ordre de production par ID", 
               description = "Permet de récupérer les détails d'un ordre de production spécifique")
    public ResponseEntity<ProductionOrderResponseDTO> getProductionOrderById(@PathVariable Long id) {
        ProductionOrderResponseDTO order = productionOrderService.getProductionOrderById(id);
        return ResponseEntity.ok(order);
    }
}
