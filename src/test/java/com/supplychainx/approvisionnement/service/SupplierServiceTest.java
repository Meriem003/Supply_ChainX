package com.supplychainx.approvisionnement.service;

import com.supplychainx.approvisionnement.dto.SupplierCreateDTO;
import com.supplychainx.approvisionnement.dto.SupplierResponseDTO;
import com.supplychainx.approvisionnement.dto.SupplierUpdateDTO;
import com.supplychainx.approvisionnement.entity.Supplier;
import com.supplychainx.approvisionnement.entity.SupplyOrder;
import com.supplychainx.approvisionnement.enums.SupplyOrderStatus;
import com.supplychainx.approvisionnement.repository.SupplierRepository;
import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.ResourceNotFoundException;
import com.supplychainx.mapper.SupplierMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.supplychainx.approvisionnement.repository.SupplyOrderRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplyOrderRepository supplyOrderRepository;

    @Mock
    private SupplierMapper supplierMapper;

    @InjectMocks
    private SupplierService supplierService;

    private Supplier supplier;
    private SupplierCreateDTO createDTO;
    private SupplierUpdateDTO updateDTO;
    private SupplierResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        supplier = new Supplier();
        supplier.setIdSupplier(1L);
        supplier.setName("Fournisseur Test");
        supplier.setContact("contact@test.com");
        supplier.setRating(4.5);
        supplier.setLeadTime(5);
        supplier.setOrders(new ArrayList<>());

        createDTO = new SupplierCreateDTO();
        createDTO.setName("Nouveau Fournisseur");
        createDTO.setContact("nouveau@test.com");
        createDTO.setRating(4.0);
        createDTO.setLeadTime(7);

        updateDTO = new SupplierUpdateDTO();
        updateDTO.setName("Fournisseur Modifié");
        updateDTO.setContact("modifie@test.com");
        updateDTO.setRating(4.8);
        updateDTO.setLeadTime(3);

        responseDTO = new SupplierResponseDTO();
        responseDTO.setIdSupplier(1L);
        responseDTO.setName("Fournisseur Test");
        responseDTO.setContact("contact@test.com");
        responseDTO.setRating(4.5);
        responseDTO.setLeadTime(5);
    }

    
    @Test
    @DisplayName("Créer un fournisseur avec succès")
    void testCreateSupplier_Success() {
        when(supplierRepository.save(any(Supplier.class))).thenReturn(supplier);
        when(supplierMapper.toResponseDTO(supplier)).thenReturn(responseDTO);

        SupplierResponseDTO result = supplierService.createSupplier(createDTO);

        assertNotNull(result);
        assertEquals(responseDTO.getName(), result.getName());
        verify(supplierRepository, times(1)).save(any(Supplier.class));
        verify(supplierMapper, times(1)).toResponseDTO(supplier);
    }

    @Test
    @DisplayName("Créer un fournisseur avec toutes les informations obligatoires")
    void testCreateSupplier_WithAllRequiredFields() {
        when(supplierRepository.save(any(Supplier.class))).thenReturn(supplier);
        when(supplierMapper.toResponseDTO(supplier)).thenReturn(responseDTO);

        SupplierResponseDTO result = supplierService.createSupplier(createDTO);

        assertNotNull(result);
        assertNotNull(result.getName());
        assertNotNull(result.getContact());
        assertNotNull(result.getRating());
        assertNotNull(result.getLeadTime());
        verify(supplierRepository, times(1)).save(any(Supplier.class));
    }

    
    @Test
    @DisplayName("Modifier un fournisseur existant avec succès")
    void testUpdateSupplier_Success() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(supplier);
        when(supplierMapper.toResponseDTO(supplier)).thenReturn(responseDTO);

        SupplierResponseDTO result = supplierService.updateSupplier(1L, updateDTO);

        assertNotNull(result);
        verify(supplierRepository, times(1)).findById(1L);
        verify(supplierRepository, times(1)).save(supplier);
        verify(supplierMapper, times(1)).toResponseDTO(supplier);
    }

    @Test
    @DisplayName("Modifier un fournisseur inexistant doit lever une exception")
    void testUpdateSupplier_NotFound() {
        when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            supplierService.updateSupplier(999L, updateDTO);
        });
        verify(supplierRepository, times(1)).findById(999L);
        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    
    @Test
    @DisplayName("Supprimer un fournisseur sans commandes actives")
    void testDeleteSupplier_WithoutActiveOrders_Success() {
        SupplyOrder completedOrder = new SupplyOrder();
        completedOrder.setStatus(SupplyOrderStatus.RECUE);
        supplier.getOrders().add(completedOrder);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(supplyOrderRepository.countBySupplier_IdSupplierAndStatusIn(eq(1L), anyList())).thenReturn(0L);
        doNothing().when(supplierRepository).delete(supplier);

        supplierService.deleteSupplier(1L);

        verify(supplierRepository, times(1)).findById(1L);
        verify(supplierRepository, times(1)).delete(supplier);
    }

    @Test
    @DisplayName("Supprimer un fournisseur avec commandes EN_ATTENTE doit échouer")
    void testDeleteSupplier_WithPendingOrders_ShouldFail() {
        SupplyOrder pendingOrder = new SupplyOrder();
        pendingOrder.setStatus(SupplyOrderStatus.EN_ATTENTE);
        supplier.getOrders().add(pendingOrder);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(supplyOrderRepository.countBySupplier_IdSupplierAndStatusIn(eq(1L), anyList())).thenReturn(1L);

        assertThrows(BusinessRuleException.class, () -> {
            supplierService.deleteSupplier(1L);
        });
        verify(supplierRepository, times(1)).findById(1L);
        verify(supplierRepository, never()).delete(any(Supplier.class));
    }

    @Test
    @DisplayName("Supprimer un fournisseur avec commandes EN_COURS doit échouer")
    void testDeleteSupplier_WithInProgressOrders_ShouldFail() {
        SupplyOrder inProgressOrder = new SupplyOrder();
        inProgressOrder.setStatus(SupplyOrderStatus.EN_COURS);
        supplier.getOrders().add(inProgressOrder);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(supplyOrderRepository.countBySupplier_IdSupplierAndStatusIn(eq(1L), anyList())).thenReturn(1L);

        assertThrows(BusinessRuleException.class, () -> {
            supplierService.deleteSupplier(1L);
        });
        verify(supplierRepository, never()).delete(any(Supplier.class));
    }

    @Test
    @DisplayName("Supprimer un fournisseur inexistant doit lever une exception")
    void testDeleteSupplier_NotFound() {
        when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            supplierService.deleteSupplier(999L);
        });
        verify(supplierRepository, never()).delete(any(Supplier.class));
    }

    
    @Test
    @DisplayName("Récupérer la liste de tous les fournisseurs")
    void testGetAllSuppliers_Success() {
        Supplier supplier2 = new Supplier();
        supplier2.setIdSupplier(2L);
        supplier2.setName("Fournisseur 2");
        
        List<Supplier> suppliers = Arrays.asList(supplier, supplier2);
        
        SupplierResponseDTO responseDTO2 = new SupplierResponseDTO();
        responseDTO2.setIdSupplier(2L);
        responseDTO2.setName("Fournisseur 2");
        
        when(supplierRepository.findAll()).thenReturn(suppliers);
        when(supplierMapper.toResponseDTO(supplier)).thenReturn(responseDTO);
        when(supplierMapper.toResponseDTO(supplier2)).thenReturn(responseDTO2);

        List<SupplierResponseDTO> result = supplierService.getAllSuppliers();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(supplierRepository, times(1)).findAll();
        verify(supplierMapper, times(2)).toResponseDTO(any(Supplier.class));
    }

    @Test
    @DisplayName("Récupérer une liste vide si aucun fournisseur")
    void testGetAllSuppliers_EmptyList() {
        when(supplierRepository.findAll()).thenReturn(new ArrayList<>());

        List<SupplierResponseDTO> result = supplierService.getAllSuppliers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(supplierRepository, times(1)).findAll();
    }

    
    @Test
    @DisplayName("Rechercher un fournisseur par nom avec succès")
    void testSearchSuppliersByName_Success() {
        List<Supplier> suppliers = Arrays.asList(supplier);
        when(supplierRepository.findByNameContainingIgnoreCase("Test")).thenReturn(suppliers);
        when(supplierMapper.toResponseDTO(supplier)).thenReturn(responseDTO);
        List<SupplierResponseDTO> result = supplierService.searchSuppliersByName("Test");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Fournisseur Test", result.get(0).getName());
        verify(supplierRepository, times(1)).findByNameContainingIgnoreCase("Test");
    }

    @Test
    @DisplayName("Rechercher un fournisseur - aucun résultat")
    void testSearchSuppliersByName_NoResults() {
        when(supplierRepository.findByNameContainingIgnoreCase("Inexistant")).thenReturn(new ArrayList<>());
        List<SupplierResponseDTO> result = supplierService.searchSuppliersByName("Inexistant");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(supplierRepository, times(1)).findByNameContainingIgnoreCase("Inexistant");
    }

    @Test
    @DisplayName("Rechercher un fournisseur - insensible à la casse")
    void testSearchSuppliersByName_CaseInsensitive() {
        List<Supplier> suppliers = Arrays.asList(supplier);
        when(supplierRepository.findByNameContainingIgnoreCase("test")).thenReturn(suppliers);
        when(supplierMapper.toResponseDTO(supplier)).thenReturn(responseDTO);

        List<SupplierResponseDTO> result = supplierService.searchSuppliersByName("test");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(supplierRepository, times(1)).findByNameContainingIgnoreCase("test");
    }
}
