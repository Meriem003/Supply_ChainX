package com.supplychainx.production.dto;

import com.supplychainx.approvisionnement.dto.RawMaterialResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillOfMaterialResponseDTO {

    private Long idBOM;
    private ProductResponseDTO product;
    private RawMaterialResponseDTO material;
    private Integer quantity;
}
