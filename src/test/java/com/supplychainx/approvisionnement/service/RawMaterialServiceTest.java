package com.supplychainx.approvisionnement.service;

import com.supplychainx.approvisionnement.dto.RawMaterialCreateDTO;
import com.supplychainx.approvisionnement.dto.RawMaterialResponseDTO;
import com.supplychainx.approvisionnement.dto.RawMaterialUpdateDTO;
import com.supplychainx.approvisionnement.entity.RawMaterial;
import com.supplychainx.approvisionnement.entity.Supplier;
import com.supplychainx.approvisionnement.repository.RawMaterialRepository;
import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.ResourceNotFoundException;
import com.supplychainx.mapper.RawMaterialMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RawMaterialServiceTest {

    @Mock
    private RawMaterialRepository rawMaterialRepository;

    @Mock
    private RawMaterialMapper rawMaterialMapper;

    @InjectMocks
    private RawMaterialService rawMaterialService;

    private RawMaterial rawMaterial;
    private RawMaterialCreateDTO createDTO;
    private RawMaterialUpdateDTO updateDTO;
    private RawMaterialResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        rawMaterial = new RawMaterial();
        rawMaterial.setIdMaterial(1L);
        rawMaterial.setName("Acier Inoxydable");
        rawMaterial.setStock(100);
        rawMaterial.setStockMin(20);
        rawMaterial.setUnit("kg");
        rawMaterial.setSuppliers(new ArrayList<>());

        createDTO = new RawMaterialCreateDTO();
        createDTO.setName("Aluminium");
        createDTO.setStock(150);
        createDTO.setStockMin(30);
        createDTO.setUnit("kg");

        updateDTO = new RawMaterialUpdateDTO();
        updateDTO.setName("Acier Modifié");
        updateDTO.setStock(120);
        updateDTO.setStockMin(25);
        updateDTO.setUnit("kg");

        responseDTO = new RawMaterialResponseDTO();
        responseDTO.setIdMaterial(1L);
        responseDTO.setName("Acier Inoxydable");
        responseDTO.setStock(100);
        responseDTO.setStockMin(20);
        responseDTO.setUnit("kg");
    }

    @Test
    @DisplayName("Créer une matière première avec succès")
    void testCreateRawMaterial_Success() {
        when(rawMaterialRepository.save(any(RawMaterial.class))).thenReturn(rawMaterial);
        when(rawMaterialMapper.toResponseDTO(rawMaterial)).thenReturn(responseDTO);

        RawMaterialResponseDTO result = rawMaterialService.createRawMaterial(createDTO);

        assertNotNull(result);
        assertEquals(responseDTO.getName(), result.getName());
        verify(rawMaterialRepository, times(1)).save(any(RawMaterial.class));
        verify(rawMaterialMapper, times(1)).toResponseDTO(rawMaterial);
    }

    @Test
    @DisplayName("Créer une matière première avec toutes les informations")
    void testCreateRawMaterial_WithAllRequiredFields() {
        when(rawMaterialRepository.save(any(RawMaterial.class))).thenReturn(rawMaterial);
        when(rawMaterialMapper.toResponseDTO(rawMaterial)).thenReturn(responseDTO);

        RawMaterialResponseDTO result = rawMaterialService.createRawMaterial(createDTO);

        assertNotNull(result);
        assertNotNull(result.getName());
        assertNotNull(result.getStock());
        assertNotNull(result.getStockMin());
        assertNotNull(result.getUnit());
        verify(rawMaterialRepository, times(1)).save(any(RawMaterial.class));
    }

    @Test
    @DisplayName("Modifier une matière première existante avec succès")
    void testUpdateRawMaterial_Success() {
        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(rawMaterial));
        when(rawMaterialRepository.save(any(RawMaterial.class))).thenReturn(rawMaterial);
        when(rawMaterialMapper.toResponseDTO(rawMaterial)).thenReturn(responseDTO);

        RawMaterialResponseDTO result = rawMaterialService.updateRawMaterial(1L, updateDTO);

        assertNotNull(result);
        verify(rawMaterialRepository, times(1)).findById(1L);
        verify(rawMaterialRepository, times(1)).save(rawMaterial);
        verify(rawMaterialMapper, times(1)).toResponseDTO(rawMaterial);
    }

    @Test
    @DisplayName("Modifier une matière première inexistante doit lever une exception")
    void testUpdateRawMaterial_NotFound() {
        when(rawMaterialRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            rawMaterialService.updateRawMaterial(999L, updateDTO);
        });
        verify(rawMaterialRepository, times(1)).findById(999L);
        verify(rawMaterialRepository, never()).save(any(RawMaterial.class));
    }

    @Test
    @DisplayName("Modifier le stock d'une matière première")
    void testUpdateRawMaterial_UpdateStock() {
        RawMaterialUpdateDTO stockUpdateDTO = new RawMaterialUpdateDTO();
        stockUpdateDTO.setName("Acier Inoxydable");
        stockUpdateDTO.setStock(200); 
        stockUpdateDTO.setStockMin(20);
        stockUpdateDTO.setUnit("kg");

        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(rawMaterial));
        when(rawMaterialRepository.save(any(RawMaterial.class))).thenReturn(rawMaterial);
        when(rawMaterialMapper.toResponseDTO(rawMaterial)).thenReturn(responseDTO);

        RawMaterialResponseDTO result = rawMaterialService.updateRawMaterial(1L, stockUpdateDTO);

        assertNotNull(result);
        verify(rawMaterialRepository, times(1)).save(rawMaterial);
    }

    @Test
    @DisplayName("Supprimer une matière première sans fournisseurs")
    void testDeleteRawMaterial_WithoutSuppliers_Success() {
        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(rawMaterial));
        doNothing().when(rawMaterialRepository).delete(rawMaterial);

        rawMaterialService.deleteRawMaterial(1L);

        verify(rawMaterialRepository, times(1)).findById(1L);
        verify(rawMaterialRepository, times(1)).delete(rawMaterial);
    }

    @Test
    @DisplayName("Supprimer une matière première avec fournisseurs doit échouer")
    void testDeleteRawMaterial_WithSuppliers_ShouldFail() {
        Supplier supplier = new Supplier();
        supplier.setIdSupplier(1L);
        supplier.setName("Fournisseur Test");
        rawMaterial.getSuppliers().add(supplier);

        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(rawMaterial));

        assertThrows(BusinessRuleException.class, () -> {
            rawMaterialService.deleteRawMaterial(1L);
        });
        verify(rawMaterialRepository, times(1)).findById(1L);
        verify(rawMaterialRepository, never()).delete(any(RawMaterial.class));
    }

    @Test
    @DisplayName("Supprimer une matière première inexistante doit lever une exception")
    void testDeleteRawMaterial_NotFound() {
        when(rawMaterialRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            rawMaterialService.deleteRawMaterial(999L);
        });
        verify(rawMaterialRepository, never()).delete(any(RawMaterial.class));
    }

    @Test
    @DisplayName("Récupérer la liste de toutes les matières premières")
    void testGetAllRawMaterials_Success() {
        RawMaterial material2 = new RawMaterial();
        material2.setIdMaterial(2L);
        material2.setName("Cuivre");
        material2.setStock(80);
        material2.setStockMin(15);
        material2.setUnit("kg");
        
        List<RawMaterial> materials = Arrays.asList(rawMaterial, material2);
        
        RawMaterialResponseDTO responseDTO2 = new RawMaterialResponseDTO();
        responseDTO2.setIdMaterial(2L);
        responseDTO2.setName("Cuivre");
        responseDTO2.setStock(80);
        
        when(rawMaterialRepository.findAll()).thenReturn(materials);
        when(rawMaterialMapper.toResponseDTO(rawMaterial)).thenReturn(responseDTO);
        when(rawMaterialMapper.toResponseDTO(material2)).thenReturn(responseDTO2);

        List<RawMaterialResponseDTO> result = rawMaterialService.getAllRawMaterials();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(rawMaterialRepository, times(1)).findAll();
        verify(rawMaterialMapper, times(2)).toResponseDTO(any(RawMaterial.class));
    }

    @Test
    @DisplayName("Récupérer une liste vide si aucune matière première")
    void testGetAllRawMaterials_EmptyList() {
        when(rawMaterialRepository.findAll()).thenReturn(new ArrayList<>());
        List<RawMaterialResponseDTO> result = rawMaterialService.getAllRawMaterials();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(rawMaterialRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Récupérer les matières premières en stock critique")
    void testGetCriticalStockMaterials_Success() {
        RawMaterial criticalMaterial = new RawMaterial();
        criticalMaterial.setIdMaterial(2L);
        criticalMaterial.setName("Matériau Critique");
        criticalMaterial.setStock(10);
        criticalMaterial.setStockMin(20);
        criticalMaterial.setUnit("kg");
        
        List<RawMaterial> criticalMaterials = Arrays.asList(criticalMaterial);
        
        RawMaterialResponseDTO criticalResponseDTO = new RawMaterialResponseDTO();
        criticalResponseDTO.setIdMaterial(2L);
        criticalResponseDTO.setName("Matériau Critique");
        criticalResponseDTO.setStock(10);
        criticalResponseDTO.setStockMin(20);
        
        when(rawMaterialRepository.findMaterialsBelowMinStock()).thenReturn(criticalMaterials);
        when(rawMaterialMapper.toResponseDTO(criticalMaterial)).thenReturn(criticalResponseDTO);

        List<RawMaterialResponseDTO> result = rawMaterialService.getCriticalStockMaterials();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Matériau Critique", result.get(0).getName());
        assertTrue(result.get(0).getStock() < result.get(0).getStockMin());
        verify(rawMaterialRepository, times(1)).findMaterialsBelowMinStock();
        verify(rawMaterialMapper, times(1)).toResponseDTO(criticalMaterial);
    }

    @Test
    @DisplayName("Aucune matière première en stock critique")
    void testGetCriticalStockMaterials_NoResults() {
        when(rawMaterialRepository.findMaterialsBelowMinStock()).thenReturn(new ArrayList<>());

        List<RawMaterialResponseDTO> result = rawMaterialService.getCriticalStockMaterials();
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(rawMaterialRepository, times(1)).findMaterialsBelowMinStock();
    }

    @Test
    @DisplayName("Plusieurs matières premières en stock critique")
    void testGetCriticalStockMaterials_MultipleResults() {
        RawMaterial critical1 = new RawMaterial();
        critical1.setIdMaterial(1L);
        critical1.setName("Matériau 1");
        critical1.setStock(5);
        critical1.setStockMin(10);
        
        RawMaterial critical2 = new RawMaterial();
        critical2.setIdMaterial(2L);
        critical2.setName("Matériau 2");
        critical2.setStock(8);
        critical2.setStockMin(15);
        
        List<RawMaterial> criticalMaterials = Arrays.asList(critical1, critical2);
        
        RawMaterialResponseDTO response1 = new RawMaterialResponseDTO();
        response1.setIdMaterial(1L);
        response1.setName("Matériau 1");
        
        RawMaterialResponseDTO response2 = new RawMaterialResponseDTO();
        response2.setIdMaterial(2L);
        response2.setName("Matériau 2");
        
        when(rawMaterialRepository.findMaterialsBelowMinStock()).thenReturn(criticalMaterials);
        when(rawMaterialMapper.toResponseDTO(critical1)).thenReturn(response1);
        when(rawMaterialMapper.toResponseDTO(critical2)).thenReturn(response2);

        List<RawMaterialResponseDTO> result = rawMaterialService.getCriticalStockMaterials();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(rawMaterialRepository, times(1)).findMaterialsBelowMinStock();
        verify(rawMaterialMapper, times(2)).toResponseDTO(any(RawMaterial.class));
    }
}
