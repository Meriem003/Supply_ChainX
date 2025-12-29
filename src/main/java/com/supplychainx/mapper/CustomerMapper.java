package com.supplychainx.mapper;

import com.supplychainx.livraison.dto.CustomerRequestDTO;
import com.supplychainx.livraison.dto.CustomerResponseDTO;
import com.supplychainx.livraison.entity.Customer;
import org.mapstruct.*;


@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CustomerMapper {

    Customer toEntity(CustomerRequestDTO dto);
    CustomerResponseDTO toResponseDTO(Customer customer);
    void updateEntityFromDTO(CustomerRequestDTO dto, @MappingTarget Customer customer);
}
