package com.supplychainx.approvisionnement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class SupplierCreateDTO {

    @NotBlank(message = "Le nom du fournisseur est obligatoire")
    private String name;
    @NotBlank(message = "Les informations de contact sont obligatoires")
    private String contact;

    @NotNull(message = "Le score de fiabilité est obligatoire")
    @Min(value = 0, message = "Le score doit être positif")
    private Double rating;

    @NotNull(message = "Le délai de livraison est obligatoire")
    @Min(value = 1, message = "Le délai doit être au minimum 1 jour")
    private Integer leadTime;
}
