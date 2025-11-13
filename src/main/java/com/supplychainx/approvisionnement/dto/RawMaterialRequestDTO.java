package com.supplychainx.approvisionnement.dto;

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
public class RawMaterialRequestDTO {

    @NotBlank(message = "Le nom de la matière première est obligatoire")
    private String name;

    @NotNull(message = "Le stock est obligatoire")
    @PositiveOrZero(message = "Le stock doit être positif ou zéro")
    private Integer stock;

    @NotNull(message = "Le stock minimum est obligatoire")
    @Positive(message = "Le stock minimum doit être positif")
    private Integer stockMin;

    @NotBlank(message = "L'unité de mesure est obligatoire")
    private String unit;
}
