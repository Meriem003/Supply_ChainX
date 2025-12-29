package com.supplychainx.mapper;

import com.supplychainx.production.dto.ProductionOrderRequestDTO;
import com.supplychainx.production.dto.ProductionOrderResponseDTO;
import com.supplychainx.production.entity.ProductionOrder;
import org.mapstruct.*;


@Mapper(
    componentModel = "spring",
    uses = {ProductMapper.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProductionOrderMapper {

    @Mapping(target = "product", ignore = true)
    ProductionOrder toEntity(ProductionOrderRequestDTO dto);

    ProductionOrderResponseDTO toResponseDTO(ProductionOrder productionOrder);
    @Mapping(target = "product", ignore = true)
    void updateEntityFromDTO(ProductionOrderRequestDTO dto, @MappingTarget ProductionOrder productionOrder);
}
