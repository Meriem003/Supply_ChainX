package com.supplychainx.mapper;

import com.supplychainx.production.dto.BillOfMaterialRequestDTO;
import com.supplychainx.production.dto.BillOfMaterialResponseDTO;
import com.supplychainx.production.entity.BillOfMaterial;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring", uses = {ProductMapper.class, RawMaterialMapper.class})
public interface BillOfMaterialMapper {

    @Mapping(target = "product", ignore = true)
    @Mapping(target = "material", ignore = true)
    BillOfMaterial toEntity(BillOfMaterialRequestDTO dto);

    BillOfMaterialResponseDTO toResponseDTO(BillOfMaterial billOfMaterial);

    @Mapping(target = "product", ignore = true)
    @Mapping(target = "material", ignore = true)
    void updateEntityFromDTO(BillOfMaterialRequestDTO dto, @MappingTarget BillOfMaterial billOfMaterial);
}
