package com.supplychainx.production.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionOrderResponseDTO {

    private Long idOrder;
    private ProductResponseDTO product;
    private Integer quantity;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
}
