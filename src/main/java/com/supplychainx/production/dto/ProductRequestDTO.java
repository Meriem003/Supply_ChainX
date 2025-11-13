package com.supplychainx.production.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

    @NotBlank(message = "Le nom du produit est obligatoire")
    private String name;

    @NotNull(message = "Le temps de production est obligatoire")
    @Positive(message = "Le temps de production doit être positif")
    private Integer productionTime;

    @NotNull(message = "Le coût est obligatoire")
    @Positive(message = "Le coût doit être positif")
    private Double cost;

    @NotNull(message = "Le stock est obligatoire")
    @PositiveOrZero(message = "Le stock doit être positif ou zéro")
    private Integer stock;
}
