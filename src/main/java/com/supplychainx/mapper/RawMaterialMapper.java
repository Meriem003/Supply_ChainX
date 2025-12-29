package com.supplychainx.mapper;

import com.supplychainx.approvisionnement.dto.RawMaterialRequestDTO;
import com.supplychainx.approvisionnement.dto.RawMaterialResponseDTO;
import com.supplychainx.approvisionnement.entity.RawMaterial;
import org.mapstruct.*;


@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RawMaterialMapper {

    RawMaterial toEntity(RawMaterialRequestDTO dto);

    RawMaterialResponseDTO toResponseDTO(RawMaterial rawMaterial);

    void updateEntityFromDTO(RawMaterialRequestDTO dto, @MappingTarget RawMaterial rawMaterial);

    @AfterMapping
    default void calculateIsCritical(RawMaterial rawMaterial, @MappingTarget RawMaterialResponseDTO dto) {
        if (rawMaterial.getStock() != null && rawMaterial.getStockMin() != null) {
            dto.setIsCritical(rawMaterial.getStock() < rawMaterial.getStockMin());
        } else {
            dto.setIsCritical(false);
        }
    }
}
