package com.supplychainx.approvisionnement.controller;

import com.supplychainx.approvisionnement.dto.RawMaterialCreateDTO;
import com.supplychainx.approvisionnement.dto.RawMaterialResponseDTO;
import com.supplychainx.approvisionnement.dto.RawMaterialUpdateDTO;
import com.supplychainx.approvisionnement.service.RawMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/raw-materials")
@RequiredArgsConstructor
@Tag(name = "Matières Premières", description = "Gestion des matières premières (Module Approvisionnement)")
public class RawMaterialController {

    private final RawMaterialService rawMaterialService;

    @PostMapping
    @Operation(summary = "Créer une matière première", description = "Ajoute une nouvelle matière première (US8)")
    public ResponseEntity<RawMaterialResponseDTO> createRawMaterial(@Valid @RequestBody RawMaterialCreateDTO dto) {
        RawMaterialResponseDTO created = rawMaterialService.createRawMaterial(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une matière première", description = "Met à jour une matière première existante (US9)")
    public ResponseEntity<RawMaterialResponseDTO> updateRawMaterial(
            @PathVariable Long id,
            @Valid @RequestBody RawMaterialUpdateDTO dto) {
        RawMaterialResponseDTO updated = rawMaterialService.updateRawMaterial(id, dto);
        return ResponseEntity.ok(updated);
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une matière première", 
               description = "Supprime une matière première si elle n'est pas utilisée (US10)")
    public ResponseEntity<Void> deleteRawMaterial(@PathVariable Long id) {
        rawMaterialService.deleteRawMaterial(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Liste des matières premières", 
               description = "Consulte la liste de toutes les matières premières (US11)")
    public ResponseEntity<List<RawMaterialResponseDTO>> getAllRawMaterials() {
        List<RawMaterialResponseDTO> materials = rawMaterialService.getAllRawMaterials();
        return ResponseEntity.ok(materials);
    }

    @GetMapping("/critical")
    @Operation(summary = "Matières en stock critique", 
               description = "Filtre les matières dont le stock < seuil minimum (US12)")
    public ResponseEntity<List<RawMaterialResponseDTO>> getCriticalStockMaterials() {
        List<RawMaterialResponseDTO> criticalMaterials = rawMaterialService.getCriticalStockMaterials();
        return ResponseEntity.ok(criticalMaterials);
    }
}
