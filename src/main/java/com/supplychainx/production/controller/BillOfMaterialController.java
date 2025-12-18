package com.supplychainx.production.controller;

import com.supplychainx.production.dto.BillOfMaterialRequestDTO;
import com.supplychainx.production.dto.BillOfMaterialResponseDTO;
import com.supplychainx.production.service.BillOfMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/bom")
@RequiredArgsConstructor
@Tag(name = "Nomenclatures (BOM)", description = "Gestion des nomenclatures - Association produits/matières premières")
public class BillOfMaterialController {

    private final BillOfMaterialService billOfMaterialService;


    @PostMapping
    @Operation(summary = "Créer une nomenclature", 
               description = "Associer une matière première à un produit fini avec sa quantité nécessaire")
    public ResponseEntity<BillOfMaterialResponseDTO> createBillOfMaterial(
            @Valid @RequestBody BillOfMaterialRequestDTO dto) {
        BillOfMaterialResponseDTO created = billOfMaterialService.createBillOfMaterial(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une nomenclature", 
               description = "Modifier la quantité ou les associations d'une nomenclature existante")
    public ResponseEntity<BillOfMaterialResponseDTO> updateBillOfMaterial(
            @PathVariable Long id,
            @Valid @RequestBody BillOfMaterialRequestDTO dto) {
        BillOfMaterialResponseDTO updated = billOfMaterialService.updateBillOfMaterial(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une nomenclature", 
               description = "Retirer une matière première de la nomenclature d'un produit")
    public ResponseEntity<Void> deleteBillOfMaterial(@PathVariable Long id) {
        billOfMaterialService.deleteBillOfMaterial(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Consulter toutes les nomenclatures", 
               description = "Liste complète de toutes les associations produits/matières")
    public ResponseEntity<List<BillOfMaterialResponseDTO>> getAllBillOfMaterials() {
        List<BillOfMaterialResponseDTO> boms = billOfMaterialService.getAllBillOfMaterials();
        return ResponseEntity.ok(boms);
    }
    @GetMapping("/product/{productId}")
    @Operation(summary = "Consulter la BOM d'un produit", 
               description = "US28 Support: Liste des matières premières nécessaires pour fabriquer un produit")
    public ResponseEntity<List<BillOfMaterialResponseDTO>> getBillOfMaterialsByProduct(
            @PathVariable Long productId) {
        List<BillOfMaterialResponseDTO> boms = billOfMaterialService.getBillOfMaterialsByProduct(productId);
        return ResponseEntity.ok(boms);
    }
}
