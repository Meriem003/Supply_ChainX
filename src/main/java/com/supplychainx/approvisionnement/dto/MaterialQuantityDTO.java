package com.supplychainx.approvisionnement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialQuantityDTO {
    private Long materialId;
    private Integer quantity;
}
