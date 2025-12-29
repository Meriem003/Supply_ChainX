package com.supplychainx.approvisionnement.service;

import com.supplychainx.approvisionnement.dto.*;
import com.supplychainx.approvisionnement.entity.RawMaterial;
import com.supplychainx.approvisionnement.entity.Supplier;
import com.supplychainx.approvisionnement.entity.SupplyOrder;
import com.supplychainx.approvisionnement.entity.SupplyOrderMaterial;
import com.supplychainx.approvisionnement.enums.SupplyOrderStatus;
import com.supplychainx.approvisionnement.repository.RawMaterialRepository;
import com.supplychainx.approvisionnement.repository.SupplierRepository;
import com.supplychainx.approvisionnement.repository.SupplyOrderRepository;
import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.ResourceNotFoundException;
import com.supplychainx.mapper.RawMaterialMapper;
import com.supplychainx.mapper.SupplierMapper;
import com.supplychainx.mapper.SupplyOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SupplyOrderService {

    private final SupplyOrderRepository supplyOrderRepository;
    private final SupplierRepository supplierRepository;
    private final RawMaterialRepository rawMaterialRepository;
    private final SupplyOrderMapper supplyOrderMapper;
    private final SupplierMapper supplierMapper;
    private final RawMaterialMapper rawMaterialMapper;

    @Transactional
    public SupplyOrderResponseDTO createSupplyOrder(SupplyOrderCreateDTO dto) {
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fournisseur non trouvé avec l'ID: " + dto.getSupplierId()));

        SupplyOrder order = new SupplyOrder();
        order.setSupplier(supplier);
        order.setOrderDate(dto.getOrderDate());
        order.setStatus(SupplyOrderStatus.valueOf(dto.getStatus()));

        for (MaterialQuantityDTO materialDto : dto.getMaterials()) {
            RawMaterial material = rawMaterialRepository.findById(materialDto.getMaterialId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Matière première non trouvée avec l'ID: " + materialDto.getMaterialId()));
            
            SupplyOrderMaterial orderMaterial = new SupplyOrderMaterial();
            orderMaterial.setSupplyOrder(order);
            orderMaterial.setRawMaterial(material);
            orderMaterial.setQuantity(materialDto.getQuantity());
            
            order.getOrderMaterials().add(orderMaterial);
        }

        SupplyOrder savedOrder = supplyOrderRepository.save(order);
        return supplyOrderMapper.toResponseDTO(savedOrder);
    }

    @Transactional
    public SupplyOrderResponseDTO updateSupplyOrder(Long id, SupplyOrderUpdateDTO dto) {
        SupplyOrder order = supplyOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Commande non trouvée avec l'ID: " + id));

        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fournisseur non trouvé avec l'ID: " + dto.getSupplierId()));

        order.setSupplier(supplier);
        order.setOrderDate(dto.getOrderDate());
        order.setStatus(SupplyOrderStatus.valueOf(dto.getStatus()));
        
        order.getOrderMaterials().clear();
        
        for (MaterialQuantityDTO materialDto : dto.getMaterials()) {
            RawMaterial material = rawMaterialRepository.findById(materialDto.getMaterialId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Matière première non trouvée avec l'ID: " + materialDto.getMaterialId()));
            
            SupplyOrderMaterial orderMaterial = new SupplyOrderMaterial();
            orderMaterial.setSupplyOrder(order);
            orderMaterial.setRawMaterial(material);
            orderMaterial.setQuantity(materialDto.getQuantity());
            
            order.getOrderMaterials().add(orderMaterial);
        }

        SupplyOrder updatedOrder = supplyOrderRepository.save(order);
        return supplyOrderMapper.toResponseDTO(updatedOrder);
    }

    @Transactional
    public void deleteSupplyOrder(Long id) {
        SupplyOrder order = supplyOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Commande non trouvée avec l'ID: " + id));

        if (order.getStatus() == SupplyOrderStatus.RECUE) {
            throw new BusinessRuleException(
                    "Impossible de supprimer une commande déjà livrée (statut RECUE)");
        }

        supplyOrderRepository.delete(order);
    }

    @Transactional(readOnly = true)
    public List<SupplyOrderResponseDTO> getAllSupplyOrders() {
        return supplyOrderRepository.findAll().stream()
                .map(supplyOrderMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SupplyOrderResponseDTO> getSupplyOrdersByStatus(String status) {
        SupplyOrderStatus orderStatus = SupplyOrderStatus.valueOf(status);
        
        return supplyOrderRepository.findByStatus(orderStatus).stream()
                .map(supplyOrderMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
