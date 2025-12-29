package com.supplychainx.mapper;

import com.supplychainx.production.dto.ProductRequestDTO;
import com.supplychainx.production.dto.ProductResponseDTO;
import com.supplychainx.production.entity.Product;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProductMapper {

    Product toEntity(ProductRequestDTO dto);

    ProductResponseDTO toResponseDTO(Product product);
    
    void updateEntityFromDTO(ProductRequestDTO dto, @MappingTarget Product product);
}
