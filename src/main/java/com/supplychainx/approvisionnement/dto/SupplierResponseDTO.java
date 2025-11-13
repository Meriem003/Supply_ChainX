package com.supplychainx.approvisionnement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponseDTO {

    private Long idSupplier;
    private String name;
    private String contact;
    private Double rating;
    private Integer leadTime;
}
