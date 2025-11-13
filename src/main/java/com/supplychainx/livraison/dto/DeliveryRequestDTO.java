package com.supplychainx.livraison.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRequestDTO {

    @NotNull(message = "L'identifiant de la commande est obligatoire")
    private Long orderId;

    private String vehicle;

    private String driver;

    @NotNull(message = "Le statut est obligatoire")
    private String status;

    @NotNull(message = "La date de livraison est obligatoire")
    private LocalDate deliveryDate;

    @Positive(message = "Le coût doit être positif")
    private Double cost;
}
