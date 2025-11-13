package com.supplychainx.production.service;

import com.supplychainx.approvisionnement.entity.RawMaterial;
import com.supplychainx.approvisionnement.repository.RawMaterialRepository;
import com.supplychainx.exception.ResourceNotFoundException;
import com.supplychainx.mapper.BillOfMaterialMapper;
import com.supplychainx.production.dto.BillOfMaterialRequestDTO;
import com.supplychainx.production.dto.BillOfMaterialResponseDTO;
import com.supplychainx.production.entity.BillOfMaterial;
import com.supplychainx.production.entity.Product;
import com.supplychainx.production.repository.BillOfMaterialRepository;
import com.supplychainx.production.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BillOfMaterialService {

    private final BillOfMaterialRepository billOfMaterialRepository;
    private final ProductRepository productRepository;
    private final RawMaterialRepository rawMaterialRepository;
    private final BillOfMaterialMapper billOfMaterialMapper;

    @Transactional
    public BillOfMaterialResponseDTO createBillOfMaterial(BillOfMaterialRequestDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produit non trouvé avec l'ID: " + dto.getProductId()));

        RawMaterial material = rawMaterialRepository.findById(dto.getMaterialId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Matière première non trouvée avec l'ID: " + dto.getMaterialId()));

        BillOfMaterial bom = billOfMaterialMapper.toEntity(dto);
        bom.setProduct(product);
        bom.setMaterial(material);

        BillOfMaterial savedBom = billOfMaterialRepository.save(bom);
        return billOfMaterialMapper.toResponseDTO(savedBom);
    }


    @Transactional
    public BillOfMaterialResponseDTO updateBillOfMaterial(Long id, BillOfMaterialRequestDTO dto) {
        BillOfMaterial bom = billOfMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nomenclature non trouvée avec l'ID: " + id));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produit non trouvé avec l'ID: " + dto.getProductId()));

        RawMaterial material = rawMaterialRepository.findById(dto.getMaterialId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Matière première non trouvée avec l'ID: " + dto.getMaterialId()));

        billOfMaterialMapper.updateEntityFromDTO(dto, bom);
        bom.setProduct(product);
        bom.setMaterial(material);

        BillOfMaterial updatedBom = billOfMaterialRepository.save(bom);
        return billOfMaterialMapper.toResponseDTO(updatedBom);
    }

    @Transactional
    public void deleteBillOfMaterial(Long id) {
        BillOfMaterial bom = billOfMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nomenclature non trouvée avec l'ID: " + id));

        billOfMaterialRepository.delete(bom);
    }

    @Transactional(readOnly = true)
    public List<BillOfMaterialResponseDTO> getAllBillOfMaterials() {
        return billOfMaterialRepository.findAll().stream()
                .map(billOfMaterialMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BillOfMaterialResponseDTO> getBillOfMaterialsByProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produit non trouvé avec l'ID: " + productId));

        return billOfMaterialRepository.findByProduct(product).stream()
                .map(billOfMaterialMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
