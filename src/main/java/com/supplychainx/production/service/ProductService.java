package com.supplychainx.production.service;

import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.ResourceNotFoundException;
import com.supplychainx.production.dto.ProductCreateDTO;
import com.supplychainx.production.dto.ProductResponseDTO;
import com.supplychainx.production.dto.ProductUpdateDTO;
import com.supplychainx.production.entity.Product;
import com.supplychainx.production.entity.ProductionOrder;
import com.supplychainx.production.repository.ProductRepository;
import com.supplychainx.production.repository.ProductionOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductionOrderRepository productionOrderRepository;

    @Transactional
    public ProductResponseDTO createProduct(ProductCreateDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setProductionTime(dto.getProductionTime());
        product.setCost(dto.getCost());
        product.setStock(dto.getStock());

        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductUpdateDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produit non trouvé avec l'ID: " + id));

        product.setName(dto.getName());
        product.setProductionTime(dto.getProductionTime());
        product.setCost(dto.getCost());
        product.setStock(dto.getStock());

        Product updatedProduct = productRepository.save(product);
        return convertToDTO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produit non trouvé avec l'ID: " + id));

        List<ProductionOrder> orders = productionOrderRepository.findByProduct(product);
        if (!orders.isEmpty()) {
            throw new BusinessRuleException(
                    "Impossible de supprimer le produit car il a " + orders.size() + 
                    " ordre(s) de production associé(s)");
        }

        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ProductResponseDTO convertToDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setIdProduct(product.getIdProduct());
        dto.setName(product.getName());
        dto.setProductionTime(product.getProductionTime());
        dto.setCost(product.getCost());
        dto.setStock(product.getStock());
        return dto;
    }
}
