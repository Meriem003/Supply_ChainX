package com.supplychainx.livraison.dto;

import com.supplychainx.production.dto.ProductResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private Long idOrder;
    private CustomerResponseDTO customer;
    private ProductResponseDTO product;
    private Integer quantity;
    private String status;
}
