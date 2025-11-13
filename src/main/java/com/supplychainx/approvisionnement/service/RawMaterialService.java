package com.supplychainx.approvisionnement.service;

import com.supplychainx.approvisionnement.dto.RawMaterialCreateDTO;
import com.supplychainx.approvisionnement.dto.RawMaterialResponseDTO;
import com.supplychainx.approvisionnement.dto.RawMaterialUpdateDTO;
import com.supplychainx.approvisionnement.entity.RawMaterial;
import com.supplychainx.approvisionnement.repository.RawMaterialRepository;
import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.ResourceNotFoundException;
import com.supplychainx.mapper.RawMaterialMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RawMaterialService {

    private final RawMaterialRepository rawMaterialRepository;
    private final RawMaterialMapper rawMaterialMapper;

    public RawMaterialResponseDTO createRawMaterial(RawMaterialCreateDTO dto) {
        RawMaterial material = new RawMaterial();
        material.setName(dto.getName());
        material.setStock(dto.getStock());
        material.setStockMin(dto.getStockMin());
        material.setUnit(dto.getUnit());

        material = rawMaterialRepository.save(material);

        return rawMaterialMapper.toResponseDTO(material);
    }


    public RawMaterialResponseDTO updateRawMaterial(Long materialId, RawMaterialUpdateDTO dto) {
        RawMaterial material = rawMaterialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Matière première non trouvée avec l'ID: " + materialId));

        material.setName(dto.getName());
        material.setStock(dto.getStock());
        material.setStockMin(dto.getStockMin());
        material.setUnit(dto.getUnit());

        material = rawMaterialRepository.save(material);

        return rawMaterialMapper.toResponseDTO(material);
    }


    public void deleteRawMaterial(Long materialId) {
        RawMaterial material = rawMaterialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Matière première non trouvée avec l'ID: " + materialId));
        boolean isUsedInOrders = !material.getSuppliers().isEmpty();

        if (isUsedInOrders) {
            throw new BusinessRuleException(
                    "Impossible de supprimer la matière première : elle est associée à des fournisseurs ou des commandes");
        }
        rawMaterialRepository.delete(material);
    }

    public List<RawMaterialResponseDTO> getAllRawMaterials() {
        return rawMaterialRepository.findAll().stream()
                .map(rawMaterialMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<RawMaterialResponseDTO> getCriticalStockMaterials() {
        return rawMaterialRepository.findMaterialsBelowMinStock().stream()
                .map(rawMaterialMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
