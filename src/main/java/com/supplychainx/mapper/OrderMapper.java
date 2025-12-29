package com.supplychainx.mapper;

import com.supplychainx.livraison.dto.OrderRequestDTO;
import com.supplychainx.livraison.dto.OrderResponseDTO;
import com.supplychainx.livraison.entity.Order;
import org.mapstruct.*;


@Mapper(
    componentModel = "spring",
    uses = {CustomerMapper.class, ProductMapper.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrderMapper {

    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "product", ignore = true)
    Order toEntity(OrderRequestDTO dto);

    OrderResponseDTO toResponseDTO(Order order);

    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "product", ignore = true)
    void updateEntityFromDTO(OrderRequestDTO dto, @MappingTarget Order order);
}
