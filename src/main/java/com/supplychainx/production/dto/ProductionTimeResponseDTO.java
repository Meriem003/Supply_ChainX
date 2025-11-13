package com.supplychainx.production.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionTimeResponseDTO {
    
    private Long productId;
    private String productName;
    private Integer quantity;
    private Integer unitProductionTime;
    private Integer totalProductionTime;
}
