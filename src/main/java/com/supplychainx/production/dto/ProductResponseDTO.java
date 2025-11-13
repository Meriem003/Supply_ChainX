package com.supplychainx.production.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {

    private Long idProduct;
    private String name;
    private Integer productionTime;
    private Double cost;
    private Integer stock;
}
