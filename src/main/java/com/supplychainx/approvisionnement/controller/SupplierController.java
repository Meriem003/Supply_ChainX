package com.supplychainx.approvisionnement.controller;

import com.supplychainx.approvisionnement.dto.SupplierCreateDTO;
import com.supplychainx.approvisionnement.dto.SupplierResponseDTO;
import com.supplychainx.approvisionnement.dto.SupplierUpdateDTO;
import com.supplychainx.approvisionnement.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST pour gérer les fournisseurs
 * Implémente les US3, US4, US5, US6, US7
 */
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@Tag(name = "Fournisseurs", description = "Gestion des fournisseurs (Module Approvisionnement)")
public class SupplierController {

    private final SupplierService supplierService;

    /**
     * US3: Ajouter un fournisseur avec toutes ses informations
     * Accessible uniquement au GESTIONNAIRE_APPROVISIONNEMENT
     */
    @PostMapping
    @Operation(summary = "Créer un fournisseur", description = "Ajoute un nouveau fournisseur (US3)")
    public ResponseEntity<SupplierResponseDTO> createSupplier(@Valid @RequestBody SupplierCreateDTO dto) {
        SupplierResponseDTO created = supplierService.createSupplier(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * US4: Modifier un fournisseur existant
     * Accessible uniquement au GESTIONNAIRE_APPROVISIONNEMENT
     */
    @PutMapping("/{id}")
    @Operation(summary = "Modifier un fournisseur", description = "Met à jour un fournisseur existant (US4)")
    public ResponseEntity<SupplierResponseDTO> updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody SupplierUpdateDTO dto) {
        SupplierResponseDTO updated = supplierService.updateSupplier(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * US5: Supprimer un fournisseur (s'il n'a aucune commande active)
     * Accessible uniquement au GESTIONNAIRE_APPROVISIONNEMENT
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un fournisseur", 
               description = "Supprime un fournisseur s'il n'a aucune commande active (US5)")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un fournisseur", description = "Récupère un fournisseur par son ID")
    public ResponseEntity<SupplierResponseDTO> getSupplierById(@PathVariable Long id) {
        SupplierResponseDTO supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(supplier);
    }

    /**
     * US6: Consulter la liste de tous les fournisseurs
     * Accessible au SUPERVISEUR_LOGISTIQUE
     */
    @GetMapping
    @Operation(summary = "Liste des fournisseurs", description = "Consulte la liste de tous les fournisseurs (US6)")
    public ResponseEntity<List<SupplierResponseDTO>> getAllSuppliers() {
        List<SupplierResponseDTO> suppliers = supplierService.getAllSuppliers();
        return ResponseEntity.ok(suppliers);
    }

    /**
     * US7: Rechercher un fournisseur par nom
     * Accessible au RESPONSABLE_ACHATS
     */
    @GetMapping("/search")
    @Operation(summary = "Rechercher un fournisseur", 
               description = "Recherche un fournisseur par nom (recherche partielle) (US7)")
    public ResponseEntity<List<SupplierResponseDTO>> searchSuppliers(@RequestParam String name) {
        List<SupplierResponseDTO> suppliers = supplierService.searchSuppliersByName(name);
        return ResponseEntity.ok(suppliers);
    }
}
