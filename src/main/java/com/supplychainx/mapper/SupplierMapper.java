package com.supplychainx.mapper;

import com.supplychainx.approvisionnement.dto.SupplierRequestDTO;
import com.supplychainx.approvisionnement.dto.SupplierResponseDTO;
import com.supplychainx.approvisionnement.entity.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SupplierMapper {


    Supplier toEntity(SupplierRequestDTO dto);

    SupplierResponseDTO toResponseDTO(Supplier supplier);

    void updateEntityFromDTO(SupplierRequestDTO dto, @MappingTarget Supplier supplier);
}
