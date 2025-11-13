package com.supplychainx.livraison.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {

    @NotNull(message = "L'identifiant du client est obligatoire")
    private Long customerId;

    @NotNull(message = "L'identifiant du produit est obligatoire")
    private Long productId;

    @NotNull(message = "La quantité est obligatoire")
    @Positive(message = "La quantité doit être positive")
    private Integer quantity;

    @NotNull(message = "Le statut est obligatoire")
    private String status;
}
