package com.supplychainx.approvisionnement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplychainx.approvisionnement.dto.RawMaterialCreateDTO;
import com.supplychainx.approvisionnement.dto.RawMaterialResponseDTO;
import com.supplychainx.approvisionnement.dto.RawMaterialUpdateDTO;
import com.supplychainx.approvisionnement.service.RawMaterialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RawMaterialController.class)
class RawMaterialControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RawMaterialService rawMaterialService;

    private RawMaterialResponseDTO responseDTO;
    private RawMaterialCreateDTO createDTO;
    private RawMaterialUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new RawMaterialResponseDTO();
        responseDTO.setIdMaterial(1L);
        responseDTO.setName("Steel");
        responseDTO.setStock(100);
        responseDTO.setStockMin(10);
        responseDTO.setUnit("kg");
        responseDTO.setIsCritical(false);

        createDTO = new RawMaterialCreateDTO();
        createDTO.setName("Steel");
        createDTO.setStock(100);
        createDTO.setStockMin(10);
        createDTO.setUnit("kg");

        updateDTO = new RawMaterialUpdateDTO();
        updateDTO.setName("Updated Steel");
        updateDTO.setStock(150);
        updateDTO.setStockMin(15);
        updateDTO.setUnit("kg");
    }

    @Test
    @DisplayName("Should create raw material via REST API")
    void testCreateRawMaterial() throws Exception {
        when(rawMaterialService.createRawMaterial(any(RawMaterialCreateDTO.class)))
            .thenReturn(responseDTO);

        mockMvc.perform(post("/api/raw-materials")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMaterial").value(1))
                .andExpect(jsonPath("$.name").value("Steel"))
                .andExpect(jsonPath("$.stock").value(100));
    }

    @Test
    @DisplayName("Should get all raw materials via REST API")
    void testGetAllRawMaterials() throws Exception {
        List<RawMaterialResponseDTO> materials = Arrays.asList(responseDTO);
        when(rawMaterialService.getAllRawMaterials()).thenReturn(materials);

        mockMvc.perform(get("/api/raw-materials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idMaterial").value(1))
                .andExpect(jsonPath("$[0].name").value("Steel"));
    }

    @Test
    @DisplayName("Should update raw material via REST API")
    void testUpdateRawMaterial() throws Exception {
        responseDTO.setName("Updated Steel");
        responseDTO.setStock(150);
        when(rawMaterialService.updateRawMaterial(eq(1L), any(RawMaterialUpdateDTO.class)))
            .thenReturn(responseDTO);

        mockMvc.perform(put("/api/raw-materials/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Steel"))
                .andExpect(jsonPath("$.stock").value(150));
    }

    @Test
    @DisplayName("Should delete raw material via REST API")
    void testDeleteRawMaterial() throws Exception {
        mockMvc.perform(delete("/api/raw-materials/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should get critical stock materials via REST API")
    void testGetCriticalStockMaterials() throws Exception {
        responseDTO.setIsCritical(true);
        List<RawMaterialResponseDTO> materials = Arrays.asList(responseDTO);
        when(rawMaterialService.getCriticalStockMaterials()).thenReturn(materials);

        mockMvc.perform(get("/api/raw-materials/critical"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isCritical").value(true));
    }

    @Test
    @DisplayName("Should validate raw material creation with invalid data")
    void testCreateRawMaterial_InvalidData() throws Exception {
        RawMaterialCreateDTO invalidDTO = new RawMaterialCreateDTO();
        mockMvc.perform(post("/api/raw-materials")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }
}
