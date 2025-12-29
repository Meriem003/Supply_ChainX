package com.supplychainx.mapper;

import com.supplychainx.approvisionnement.dto.SupplierRequestDTO;
import com.supplychainx.approvisionnement.dto.SupplierResponseDTO;
import com.supplychainx.approvisionnement.entity.Supplier;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface SupplierMapper {


    Supplier toEntity(SupplierRequestDTO dto);

    SupplierResponseDTO toResponseDTO(Supplier supplier);

    void updateEntityFromDTO(SupplierRequestDTO dto, @MappingTarget Supplier supplier);
}
