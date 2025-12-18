package com.supplychainx.production.controller;

import com.supplychainx.production.dto.ProductCreateDTO;
import com.supplychainx.production.dto.ProductResponseDTO;
import com.supplychainx.production.dto.ProductUpdateDTO;
import com.supplychainx.production.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Produits Finis", description = "Gestion des produits finis (Module Production)")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Créer un produit fini", 
               description = "Permet au chef de production d'ajouter un nouveau produit")
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductCreateDTO dto) {
        ProductResponseDTO created = productService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un produit fini", 
               description = "Permet au chef de production de modifier un produit existant")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateDTO dto) {
        ProductResponseDTO updated = productService.updateProduct(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un produit fini", 
               description = "Permet au chef de production de supprimer un produit sans ordre associé")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Consulter tous les produits", 
               description = "Permet au superviseur production de consulter tous les produits")
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        List<ProductResponseDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher un produit par nom", 
               description = "Permet au superviseur production de rechercher un produit par nom")
    public ResponseEntity<List<ProductResponseDTO>> searchProductsByName(@RequestParam String name) {
        List<ProductResponseDTO> products = productService.searchProductsByName(name);
        return ResponseEntity.ok(products);
    }
}
