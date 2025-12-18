package com.supplychainx.livraison.controller;

import com.supplychainx.livraison.dto.CustomerRequestDTO;
import com.supplychainx.livraison.dto.CustomerResponseDTO;
import com.supplychainx.livraison.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Gestion des clients")
public class CustomerController {
    
    private final CustomerService customerService;
    
    @PostMapping
    @Operation(summary = "Créer un client",
            description = "Permet de créer un nouveau client avec nom, adresse et ville")
    public ResponseEntity<CustomerResponseDTO> createCustomer(@Valid @RequestBody CustomerRequestDTO dto) {
        CustomerResponseDTO customer = customerService.createCustomer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un client",
            description = "Permet de modifier toutes les informations d'un client existant")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequestDTO dto) {
        CustomerResponseDTO customer = customerService.updateCustomer(id, dto);
        return ResponseEntity.ok(customer);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un client",
            description = "Permet de supprimer un client uniquement s'il n'a aucune commande associée")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Consulter tous les clients",
            description = "Retourne la liste de tous les clients")
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {
        List<CustomerResponseDTO> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher un client par nom",
            description = "Permet de filtrer les clients dont le nom contient la chaîne recherchée (insensible à la casse)")
    public ResponseEntity<List<CustomerResponseDTO>> searchCustomersByName(@RequestParam String name) {
        List<CustomerResponseDTO> customers = customerService.searchCustomersByName(name);
        return ResponseEntity.ok(customers);
    }
}
