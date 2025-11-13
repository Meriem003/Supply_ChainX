package com.supplychainx.mapper;

import com.supplychainx.production.dto.ProductionOrderRequestDTO;
import com.supplychainx.production.dto.ProductionOrderResponseDTO;
import com.supplychainx.production.entity.ProductionOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface ProductionOrderMapper {

    @Mapping(target = "product", ignore = true)
    ProductionOrder toEntity(ProductionOrderRequestDTO dto);

    ProductionOrderResponseDTO toResponseDTO(ProductionOrder productionOrder);
    @Mapping(target = "product", ignore = true)
    void updateEntityFromDTO(ProductionOrderRequestDTO dto, @MappingTarget ProductionOrder productionOrder);
}
