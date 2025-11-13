package com.supplychainx.mapper;

import com.supplychainx.approvisionnement.dto.SupplyOrderRequestDTO;
import com.supplychainx.approvisionnement.dto.SupplyOrderResponseDTO;
import com.supplychainx.approvisionnement.entity.SupplyOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring", uses = {SupplierMapper.class, RawMaterialMapper.class})
public interface SupplyOrderMapper {

    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "materials", ignore = true)
    SupplyOrder toEntity(SupplyOrderRequestDTO dto);

    SupplyOrderResponseDTO toResponseDTO(SupplyOrder supplyOrder);

    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "materials", ignore = true)
    void updateEntityFromDTO(SupplyOrderRequestDTO dto, @MappingTarget SupplyOrder supplyOrder);
}
