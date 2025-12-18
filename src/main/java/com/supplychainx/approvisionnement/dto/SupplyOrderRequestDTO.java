package com.supplychainx.approvisionnement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplyOrderRequestDTO {

    @NotNull(message = "L'identifiant du fournisseur est obligatoire")
    private Long supplierId;

    @NotNull(message = "Les matières premières sont obligatoires")
    private List<MaterialQuantityDTO> materials;

    @NotNull(message = "La date de commande est obligatoire")
    private LocalDate orderDate;

    @NotNull(message = "Le statut est obligatoire")
    private String status;
}
