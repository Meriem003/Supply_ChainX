package com.supplychainx.approvisionnement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplychainx.approvisionnement.dto.RawMaterialCreateDTO;
import com.supplychainx.approvisionnement.dto.RawMaterialUpdateDTO;
import com.supplychainx.approvisionnement.entity.RawMaterial;
import com.supplychainx.approvisionnement.repository.RawMaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "admin", roles = {"ADMIN"})
class RawMaterialControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RawMaterialRepository rawMaterialRepository;

    private RawMaterialCreateDTO createDTO;
    private RawMaterialUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        rawMaterialRepository.deleteAll();

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
        mockMvc.perform(post("/api/raw-materials")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Steel"))
                .andExpect(jsonPath("$.stock").value(100))
                .andExpect(jsonPath("$.stockMin").value(10))
                .andExpect(jsonPath("$.unit").value("kg"));
    }

    @Test
    @DisplayName("Should get all raw materials via REST API")
    void testGetAllRawMaterials() throws Exception {
        RawMaterial material = new RawMaterial();
        material.setName("Steel");
        material.setStock(100);
        material.setStockMin(10);
        material.setUnit("kg");
        rawMaterialRepository.save(material);

        mockMvc.perform(get("/api/raw-materials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Steel"))
                .andExpect(jsonPath("$[0].stock").value(100));
    }

    @Test
    @DisplayName("Should update raw material via REST API")
    void testUpdateRawMaterial() throws Exception {
        RawMaterial material = new RawMaterial();
        material.setName("Steel");
        material.setStock(100);
        material.setStockMin(10);
        material.setUnit("kg");
        material = rawMaterialRepository.save(material);

        mockMvc.perform(put("/api/raw-materials/" + material.getIdMaterial())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Steel"))
                .andExpect(jsonPath("$.stock").value(150));
    }

    @Test
    @DisplayName("Should delete raw material via REST API")
    void testDeleteRawMaterial() throws Exception {
        RawMaterial material = new RawMaterial();
        material.setName("Steel");
        material.setStock(100);
        material.setStockMin(10);
        material.setUnit("kg");
        material = rawMaterialRepository.save(material);

        mockMvc.perform(delete("/api/raw-materials/" + material.getIdMaterial()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should get critical stock materials via REST API")
    void testGetCriticalStockMaterials() throws Exception {
        RawMaterial material = new RawMaterial();
        material.setName("Steel");
        material.setStock(5);
        material.setStockMin(10);
        material.setUnit("kg");
        rawMaterialRepository.save(material);

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
