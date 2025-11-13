package com.supplychainx.mapper;

import com.supplychainx.production.dto.ProductRequestDTO;
import com.supplychainx.production.dto.ProductResponseDTO;
import com.supplychainx.production.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toEntity(ProductRequestDTO dto);

    ProductResponseDTO toResponseDTO(Product product);
    
    void updateEntityFromDTO(ProductRequestDTO dto, @MappingTarget Product product);
}
