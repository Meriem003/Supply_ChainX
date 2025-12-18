package com.supplychainx.approvisionnement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplyOrderResponseDTO {

    private Long idOrder;
    private SupplierResponseDTO supplier;
    private List<RawMaterialWithQuantityDTO> materials;
    private LocalDate orderDate;
    private String status;
}
