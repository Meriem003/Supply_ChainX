package com.supplychainx.mapper;

import com.supplychainx.livraison.dto.DeliveryRequestDTO;
import com.supplychainx.livraison.dto.DeliveryResponseDTO;
import com.supplychainx.livraison.entity.Delivery;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring", uses = {OrderMapper.class})
public interface DeliveryMapper {

    @Mapping(target = "order", ignore = true)
    Delivery toEntity(DeliveryRequestDTO dto);

    DeliveryResponseDTO toResponseDTO(Delivery delivery);

    @Mapping(target = "order", ignore = true)
    void updateEntityFromDTO(DeliveryRequestDTO dto, @MappingTarget Delivery delivery);
}
