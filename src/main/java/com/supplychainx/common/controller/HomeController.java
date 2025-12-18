package com.supplychainx.common.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "SupplyChainX Management System");
        response.put("version", "1.0.0");
        response.put("status", "running");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("Approvisionnement", "/api/suppliers, /api/raw-materials, /api/supply-orders");
        endpoints.put("Production", "/api/products, /api/production-orders, /api/bom, /api/planning");
        endpoints.put("Livraison", "/api/customers, /api/orders, /api/deliveries");
        endpoints.put("Utilisateurs", "/api/users");
        
        response.put("endpoints", endpoints);
        response.put("documentation", "Documentation API disponible prochainement");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}
