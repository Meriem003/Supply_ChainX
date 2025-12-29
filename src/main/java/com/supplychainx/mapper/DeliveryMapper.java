package com.supplychainx.mapper;

import com.supplychainx.livraison.dto.DeliveryRequestDTO;
import com.supplychainx.livraison.dto.DeliveryResponseDTO;
import com.supplychainx.livraison.entity.Delivery;
import org.mapstruct.*;


@Mapper(
    componentModel = "spring",
    uses = {OrderMapper.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface DeliveryMapper {

    @Mapping(target = "order", ignore = true)
    Delivery toEntity(DeliveryRequestDTO dto);

    DeliveryResponseDTO toResponseDTO(Delivery delivery);

    @Mapping(target = "order", ignore = true)
    void updateEntityFromDTO(DeliveryRequestDTO dto, @MappingTarget Delivery delivery);
}
