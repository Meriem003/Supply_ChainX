package com.supplychainx.approvisionnement.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplyOrderCreateDTO {

    @NotNull(message = "L'identifiant du fournisseur est obligatoire")
    private Long supplierId;

    @NotEmpty(message = "Au moins une matière première est obligatoire")
    private List<MaterialQuantityDTO> materials;

    @NotNull(message = "La date de commande est obligatoire")
    private LocalDate orderDate;

    @NotNull(message = "Le statut est obligatoire")
    private String status;
}
