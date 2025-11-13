package com.supplychainx.approvisionnement.service;

import com.supplychainx.approvisionnement.dto.SupplierCreateDTO;
import com.supplychainx.approvisionnement.dto.SupplierResponseDTO;
import com.supplychainx.approvisionnement.dto.SupplierUpdateDTO;
import com.supplychainx.approvisionnement.entity.Supplier;
import com.supplychainx.approvisionnement.enums.SupplyOrderStatus;
import com.supplychainx.approvisionnement.repository.SupplierRepository;
import com.supplychainx.approvisionnement.repository.SupplyOrderRepository;
import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.ResourceNotFoundException;
import com.supplychainx.mapper.SupplierMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplyOrderRepository supplyOrderRepository;
    private final SupplierMapper supplierMapper;


    public SupplierResponseDTO createSupplier(SupplierCreateDTO dto) {
        Supplier supplier = new Supplier();
        supplier.setName(dto.getName());
        supplier.setContact(dto.getContact());
        supplier.setRating(dto.getRating());
        supplier.setLeadTime(dto.getLeadTime());

        supplier = supplierRepository.save(supplier);

        return supplierMapper.toResponseDTO(supplier);
    }

    public SupplierResponseDTO updateSupplier(Long supplierId, SupplierUpdateDTO dto) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + supplierId));

        supplier.setName(dto.getName());
        supplier.setContact(dto.getContact());
        supplier.setRating(dto.getRating());
        supplier.setLeadTime(dto.getLeadTime());

        supplier = supplierRepository.save(supplier);

        return supplierMapper.toResponseDTO(supplier);
    }


    public void deleteSupplier(Long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + supplierId));
        long activeOrdersCount = supplyOrderRepository.countBySupplier_IdSupplierAndStatusIn(
                supplierId, 
                List.of(SupplyOrderStatus.EN_ATTENTE, SupplyOrderStatus.EN_COURS)
        );

        if (activeOrdersCount > 0) {
            throw new BusinessRuleException(
                    "Impossible de supprimer le fournisseur : il a des commandes actives (EN_ATTENTE ou EN_COURS)");
        }
        supplierRepository.delete(supplier);
    }

    public SupplierResponseDTO getSupplierById(Long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + supplierId));
        return supplierMapper.toResponseDTO(supplier);
    }

    public List<SupplierResponseDTO> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(supplierMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<SupplierResponseDTO> searchSuppliersByName(String name) {
        return supplierRepository.findByNameContainingIgnoreCase(name).stream()
                .map(supplierMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
