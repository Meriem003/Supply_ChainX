package com.supplychainx.approvisionnement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RawMaterialCreateDTO {


    @NotBlank(message = "Le nom de la matière première est obligatoire")
    private String name;

    @NotNull(message = "Le stock est obligatoire")
    @Min(value = 0, message = "Le stock doit être positif ou nul")
    private Integer stock;

    @NotNull(message = "Le seuil minimum est obligatoire")
    @Min(value = 0, message = "Le seuil minimum doit être positif ou nul")
    private Integer stockMin;

    @NotBlank(message = "L'unité de mesure est obligatoire")
    private String unit;
}
