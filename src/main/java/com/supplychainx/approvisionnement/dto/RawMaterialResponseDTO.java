package com.supplychainx.approvisionnement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RawMaterialResponseDTO {

    private Long idMaterial;
    private String name;
    private Integer stock;
    private Integer stockMin;
    private String unit;
    private Boolean isCritical;
}
