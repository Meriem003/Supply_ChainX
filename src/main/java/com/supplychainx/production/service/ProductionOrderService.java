package com.supplychainx.production.service;

import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.ResourceNotFoundException;
import com.supplychainx.mapper.ProductMapper;
import com.supplychainx.mapper.ProductionOrderMapper;
import com.supplychainx.production.dto.*;
import com.supplychainx.production.entity.Product;
import com.supplychainx.production.entity.ProductionOrder;
import com.supplychainx.production.enums.ProductionOrderStatus;
import com.supplychainx.production.repository.ProductRepository;
import com.supplychainx.production.repository.ProductionOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductionOrderService {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductRepository productRepository;
    private final ProductionOrderMapper productionOrderMapper;
    private final ProductMapper productMapper;

    @Transactional
    public ProductionOrderResponseDTO createProductionOrder(ProductionOrderCreateDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produit non trouvé avec l'ID: " + dto.getProductId()));

        ProductionOrder order = new ProductionOrder();
        order.setProduct(product);
        order.setQuantity(dto.getQuantity());
        order.setStatus(ProductionOrderStatus.valueOf(dto.getStatus()));
        order.setStartDate(dto.getStartDate());
        order.setEndDate(dto.getEndDate());

        ProductionOrder savedOrder = productionOrderRepository.save(order);
        return productionOrderMapper.toResponseDTO(savedOrder);
    }

    @Transactional
    public ProductionOrderResponseDTO updateProductionOrder(Long id, ProductionOrderUpdateDTO dto) {
        ProductionOrder order = productionOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ordre de production non trouvé avec l'ID: " + id));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produit non trouvé avec l'ID: " + dto.getProductId()));

        order.setProduct(product);
        order.setQuantity(dto.getQuantity());
        order.setStatus(ProductionOrderStatus.valueOf(dto.getStatus()));
        order.setStartDate(dto.getStartDate());
        order.setEndDate(dto.getEndDate());

        ProductionOrder updatedOrder = productionOrderRepository.save(order);
        return productionOrderMapper.toResponseDTO(updatedOrder);
    }

    @Transactional
    public void cancelProductionOrder(Long id) {
        ProductionOrder order = productionOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ordre de production non trouvé avec l'ID: " + id));

        if (order.getStatus() != ProductionOrderStatus.EN_ATTENTE) {
            throw new BusinessRuleException(
                    "Impossible d'annuler l'ordre car il a déjà commencé (statut: " + 
                    order.getStatus() + ")");
        }

        productionOrderRepository.delete(order);
    }


    @Transactional(readOnly = true)
    public List<ProductionOrderResponseDTO> getAllProductionOrders() {
        return productionOrderRepository.findAll().stream()
                .map(productionOrderMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductionOrderResponseDTO> getProductionOrdersByStatus(String status) {
        ProductionOrderStatus orderStatus = ProductionOrderStatus.valueOf(status);
        
        return productionOrderRepository.findByStatus(orderStatus).stream()
                .map(productionOrderMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductionOrderResponseDTO getProductionOrderById(Long id) {
        ProductionOrder order = productionOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ordre de production non trouvé avec l'ID: " + id));
        return productionOrderMapper.toResponseDTO(order);
    }
}
