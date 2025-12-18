package com.supplychainx.approvisionnement.controller;

import com.supplychainx.approvisionnement.dto.SupplyOrderCreateDTO;
import com.supplychainx.approvisionnement.dto.SupplyOrderResponseDTO;
import com.supplychainx.approvisionnement.dto.SupplyOrderUpdateDTO;
import com.supplychainx.approvisionnement.service.SupplyOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supply-orders")
@RequiredArgsConstructor
@Tag(name = "Commandes d'Approvisionnement", description = "Gestion des commandes d'approvisionnement")
public class SupplyOrderController {

    private final SupplyOrderService supplyOrderService;

    @PostMapping
    @Operation(summary = "Créer une commande d'approvisionnement", 
               description = "US13: Permet au responsable des achats de créer une nouvelle commande")
    public ResponseEntity<SupplyOrderResponseDTO> createSupplyOrder(@Valid @RequestBody SupplyOrderCreateDTO dto) {
        SupplyOrderResponseDTO created = supplyOrderService.createSupplyOrder(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une commande d'approvisionnement", 
               description = "US14: Permet au responsable des achats de modifier une commande existante")
    public ResponseEntity<SupplyOrderResponseDTO> updateSupplyOrder(
            @PathVariable Long id,
            @Valid @RequestBody SupplyOrderUpdateDTO dto) {
        SupplyOrderResponseDTO updated = supplyOrderService.updateSupplyOrder(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une commande d'approvisionnement", 
               description = "US15: Permet au responsable des achats de supprimer une commande non livrée")
    public ResponseEntity<Void> deleteSupplyOrder(@PathVariable Long id) {
        supplyOrderService.deleteSupplyOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Consulter toutes les commandes", 
               description = "US16: Permet au superviseur logistique de consulter toutes les commandes")
    public ResponseEntity<List<SupplyOrderResponseDTO>> getAllSupplyOrders() {
        List<SupplyOrderResponseDTO> orders = supplyOrderService.getAllSupplyOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Filtrer les commandes par statut", 
               description = "US17: Permet au superviseur logistique de suivre les commandes selon leur statut (EN_ATTENTE, EN_COURS, RECUE)")
    public ResponseEntity<List<SupplyOrderResponseDTO>> getSupplyOrdersByStatus(@PathVariable String status) {
        List<SupplyOrderResponseDTO> orders = supplyOrderService.getSupplyOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }
}
