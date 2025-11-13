package com.supplychainx.approvisionnement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierRequestDTO {

    @NotBlank(message = "Le nom du fournisseur est obligatoire")
    private String name;

    @NotBlank(message = "Les informations de contact sont obligatoires")
    private String contact;

    @NotNull(message = "La note de fiabilité est obligatoire")
    @Positive(message = "La note de fiabilité doit être positive")
    private Double rating;

    @NotNull(message = "Le délai de livraison est obligatoire")
    @Positive(message = "Le délai de livraison doit être positif")
    private Integer leadTime;
}
