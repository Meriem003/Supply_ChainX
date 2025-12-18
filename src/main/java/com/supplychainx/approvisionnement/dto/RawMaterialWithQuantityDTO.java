package com.supplychainx.approvisionnement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RawMaterialWithQuantityDTO {
    private Long idMaterial;
    private String name;
    private Integer quantity;
    private String unit;
}
