package com.supplychainx.livraison.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponseDTO {

    private Long idDelivery;
    private OrderResponseDTO order;
    private String vehicle;
    private String driver;
    private String status;
    private LocalDate deliveryDate;
    private Double cost;
}
