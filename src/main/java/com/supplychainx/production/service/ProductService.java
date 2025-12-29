package com.supplychainx.production.service;

import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.ResourceNotFoundException;
import com.supplychainx.logging.LoggingContext;
import com.supplychainx.production.dto.ProductCreateDTO;
import com.supplychainx.production.dto.ProductResponseDTO;
import com.supplychainx.production.dto.ProductUpdateDTO;
import com.supplychainx.production.entity.Product;
import com.supplychainx.production.entity.ProductionOrder;
import com.supplychainx.production.repository.ProductRepository;
import com.supplychainx.production.repository.ProductionOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductionOrderRepository productionOrderRepository;

    @Transactional
    public ProductResponseDTO createProduct(ProductCreateDTO dto) {
        LoggingContext.setLogType(LoggingContext.LogType.BUSINESS);
        log.info("Creating new product: {}", dto.getName());

        Product product = new Product();
        product.setName(dto.getName());
        product.setProductionTime(dto.getProductionTime());
        product.setCost(dto.getCost());
        product.setStock(dto.getStock());

        Product savedProduct = productRepository.save(product);

        LoggingContext.setBusinessId("PRODUCT_" + savedProduct.getIdProduct());
        log.info("Product created successfully - ID: {}, Name: {}, Initial Stock: {}",
            savedProduct.getIdProduct(), savedProduct.getName(), savedProduct.getStock());

        return convertToDTO(savedProduct);
    }

    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductUpdateDTO dto) {
        LoggingContext.setLogType(LoggingContext.LogType.BUSINESS);
        LoggingContext.setBusinessId("PRODUCT_" + id);

        log.info("Updating product ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product not found with ID: {}", id);
                    return new ResourceNotFoundException("Produit non trouvé avec l'ID: " + id);
                });

        Integer oldStock = product.getStock();
        product.setName(dto.getName());
        product.setProductionTime(dto.getProductionTime());
        product.setCost(dto.getCost());
        product.setStock(dto.getStock());

        Product updatedProduct = productRepository.save(product);

        log.info("Product updated successfully - ID: {}, Stock change: {} -> {}",
            id, oldStock, updatedProduct.getStock());

        return convertToDTO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        LoggingContext.setLogType(LoggingContext.LogType.BUSINESS);
        LoggingContext.setBusinessId("PRODUCT_" + id);

        log.info("Attempting to delete product ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cannot delete - Product not found with ID: {}", id);
                    return new ResourceNotFoundException("Produit non trouvé avec l'ID: " + id);
                });

        List<ProductionOrder> orders = productionOrderRepository.findByProduct(product);
        if (!orders.isEmpty()) {
            log.warn("Cannot delete product ID: {} - Has {} associated production orders",
                id, orders.size());
            throw new BusinessRuleException(
                    "Impossible de supprimer le produit car il a " + orders.size() + 
                    " ordre(s) de production associé(s)");
        }

        productRepository.delete(product);
        log.info("Product deleted successfully - ID: {}", id);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getAllProducts() {
        log.debug("Fetching all products");
        List<ProductResponseDTO> products = productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        log.info("Retrieved {} products", products.size());
        return products;
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> searchProductsByName(String name) {
        log.debug("Searching products by name: {}", name);
        List<ProductResponseDTO> products = productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        log.info("Found {} products matching name: {}", products.size(), name);
        return products;
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
