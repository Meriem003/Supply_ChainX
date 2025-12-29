package com.supplychainx.mapper;

import com.supplychainx.approvisionnement.dto.RawMaterialWithQuantityDTO;
import com.supplychainx.approvisionnement.dto.SupplyOrderRequestDTO;
import com.supplychainx.approvisionnement.dto.SupplyOrderResponseDTO;
import com.supplychainx.approvisionnement.entity.SupplyOrder;
import com.supplychainx.approvisionnement.entity.SupplyOrderMaterial;
import org.mapstruct.*;

import java.util.List;


@Mapper(
    componentModel = "spring",
    uses = {SupplierMapper.class, RawMaterialMapper.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface SupplyOrderMapper {

    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "orderMaterials", ignore = true)
    @Mapping(target = "idOrder", ignore = true)
    SupplyOrder toEntity(SupplyOrderRequestDTO dto);

    @Mapping(target = "materials", expression = "java(mapOrderMaterialsToDTO(supplyOrder))")
    SupplyOrderResponseDTO toResponseDTO(SupplyOrder supplyOrder);

    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "orderMaterials", ignore = true)
    @Mapping(target = "idOrder", ignore = true)
    void updateEntityFromDTO(SupplyOrderRequestDTO dto, @MappingTarget SupplyOrder supplyOrder);
    
    default List<RawMaterialWithQuantityDTO> mapOrderMaterialsToDTO(SupplyOrder supplyOrder) {
        if (supplyOrder.getOrderMaterials() == null) {
            return List.of();
        }
        return supplyOrder.getOrderMaterials().stream()
                .map(this::toRawMaterialWithQuantityDTO)
                .toList();
    }
    
    default RawMaterialWithQuantityDTO toRawMaterialWithQuantityDTO(SupplyOrderMaterial orderMaterial) {
        RawMaterialWithQuantityDTO dto = new RawMaterialWithQuantityDTO();
        dto.setIdMaterial(orderMaterial.getRawMaterial().getIdMaterial());
        dto.setName(orderMaterial.getRawMaterial().getName());
        dto.setQuantity(orderMaterial.getQuantity());
        dto.setUnit(orderMaterial.getRawMaterial().getUnit());
        return dto;
    }
}
