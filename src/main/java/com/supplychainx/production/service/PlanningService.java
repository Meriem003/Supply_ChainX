package com.supplychainx.production.service;

import com.supplychainx.exception.ResourceNotFoundException;
import com.supplychainx.production.dto.MaterialAvailabilityDTO;
import com.supplychainx.production.dto.ProductionAvailabilityResponseDTO;
import com.supplychainx.production.dto.ProductionTimeResponseDTO;
import com.supplychainx.production.entity.BillOfMaterial;
import com.supplychainx.production.entity.Product;
import com.supplychainx.production.repository.BillOfMaterialRepository;
import com.supplychainx.production.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanningService {
    
    private final ProductRepository productRepository;
    private final BillOfMaterialRepository billOfMaterialRepository;
    
    @Transactional(readOnly = true)
    public ProductionAvailabilityResponseDTO checkMaterialAvailability(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produit non trouvé avec l'ID: " + productId));
        
        List<BillOfMaterial> bom = billOfMaterialRepository.findByProduct(product);
        
        List<MaterialAvailabilityDTO> materialsStatus = new ArrayList<>();
        boolean canProduce = true;
        
        for (BillOfMaterial bomItem : bom) {
            Integer requiredQuantity = bomItem.getQuantity() * quantity;
            Integer availableStock = bomItem.getMaterial().getStock();
            boolean isAvailable = availableStock >= requiredQuantity;
            
            materialsStatus.add(new MaterialAvailabilityDTO(
                    bomItem.getMaterial().getIdMaterial(),
                    bomItem.getMaterial().getName(),
                    requiredQuantity,
                    availableStock,
                    isAvailable
            ));
            
            if (!isAvailable) {
                canProduce = false;
            }
        }
        
        return new ProductionAvailabilityResponseDTO(
                product.getIdProduct(),
                product.getName(),
                quantity,
                canProduce,
                materialsStatus
        );
    }
    

    @Transactional(readOnly = true)
    public ProductionTimeResponseDTO calculateProductionTime(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produit non trouvé avec l'ID: " + productId));
        
        Integer unitProductionTime = product.getProductionTime();
        Integer totalProductionTime = unitProductionTime * quantity;
        
        return new ProductionTimeResponseDTO(
                product.getIdProduct(),
                product.getName(),
                quantity,
                unitProductionTime,
                totalProductionTime
        );
    }
}
