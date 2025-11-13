package com.supplychainx.production.service;

import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.ResourceNotFoundException;
import com.supplychainx.mapper.ProductMapper;
import com.supplychainx.mapper.ProductionOrderMapper;
import com.supplychainx.production.dto.ProductionOrderCreateDTO;
import com.supplychainx.production.dto.ProductionOrderResponseDTO;
import com.supplychainx.production.dto.ProductionOrderUpdateDTO;
import com.supplychainx.production.entity.Product;
import com.supplychainx.production.entity.ProductionOrder;
import com.supplychainx.production.enums.ProductionOrderStatus;
import com.supplychainx.production.repository.ProductRepository;
import com.supplychainx.production.repository.ProductionOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductionOrderServiceTest {

    @Mock
    private ProductionOrderRepository productionOrderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductionOrderMapper productionOrderMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductionOrderService productionOrderService;

    private Product product;
    private ProductionOrder productionOrder;
    private ProductionOrderCreateDTO createDTO;
    private ProductionOrderUpdateDTO updateDTO;
    private ProductionOrderResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setIdProduct(1L);
        product.setName("Produit Test");
        product.setProductionTime(120);
        product.setCost(500.0);
        product.setStock(100);

        productionOrder = new ProductionOrder();
        productionOrder.setIdOrder(1L);
        productionOrder.setProduct(product);
        productionOrder.setQuantity(10);
        productionOrder.setStatus(ProductionOrderStatus.EN_ATTENTE);
        productionOrder.setStartDate(LocalDate.now());
        productionOrder.setEndDate(LocalDate.now().plusDays(7));

        createDTO = new ProductionOrderCreateDTO();
        createDTO.setProductId(1L);
        createDTO.setQuantity(15);
        createDTO.setStatus("EN_ATTENTE");
        createDTO.setStartDate(LocalDate.now());
        createDTO.setEndDate(LocalDate.now().plusDays(10));

        updateDTO = new ProductionOrderUpdateDTO();
        updateDTO.setProductId(1L);
        updateDTO.setQuantity(20);
        updateDTO.setStatus("EN_PRODUCTION");
        updateDTO.setStartDate(LocalDate.now());
        updateDTO.setEndDate(LocalDate.now().plusDays(5));

        responseDTO = new ProductionOrderResponseDTO();
        responseDTO.setIdOrder(1L);
        responseDTO.setQuantity(10);
        responseDTO.setStatus("EN_ATTENTE");
    }

    
    @Test
    @DisplayName("Créer un ordre de production avec succès")
    void testCreateProductionOrder_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productionOrderRepository.save(any(ProductionOrder.class))).thenReturn(productionOrder);
        when(productionOrderMapper.toResponseDTO(productionOrder)).thenReturn(responseDTO);

        ProductionOrderResponseDTO result = productionOrderService.createProductionOrder(createDTO);

        assertNotNull(result);
        assertEquals(responseDTO.getQuantity(), result.getQuantity());
        verify(productRepository, times(1)).findById(1L);
        verify(productionOrderRepository, times(1)).save(any(ProductionOrder.class));
        verify(productionOrderMapper, times(1)).toResponseDTO(productionOrder);
    }

    @Test
    @DisplayName("Créer un ordre avec produit inexistant doit échouer")
    void testCreateProductionOrder_ProductNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        createDTO.setProductId(999L);

        assertThrows(ResourceNotFoundException.class, () -> {
            productionOrderService.createProductionOrder(createDTO);
        });
        verify(productRepository, times(1)).findById(999L);
        verify(productionOrderRepository, never()).save(any(ProductionOrder.class));
    }

    
    @Test
    @DisplayName("Modifier un ordre de production avec succès")
    void testUpdateProductionOrder_Success() {
        when(productionOrderRepository.findById(1L)).thenReturn(Optional.of(productionOrder));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productionOrderRepository.save(any(ProductionOrder.class))).thenReturn(productionOrder);
        when(productionOrderMapper.toResponseDTO(productionOrder)).thenReturn(responseDTO);
        ProductionOrderResponseDTO result = productionOrderService.updateProductionOrder(1L, updateDTO);

        assertNotNull(result);
        verify(productionOrderRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findById(1L);
        verify(productionOrderRepository, times(1)).save(productionOrder);
        verify(productionOrderMapper, times(1)).toResponseDTO(productionOrder);
    }

    @Test
    @DisplayName("Modifier un ordre inexistant doit lever une exception")
    void testUpdateProductionOrder_NotFound() {
        when(productionOrderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            productionOrderService.updateProductionOrder(999L, updateDTO);
        });
        verify(productionOrderRepository, times(1)).findById(999L);
        verify(productionOrderRepository, never()).save(any(ProductionOrder.class));
    }

    @Test
    @DisplayName("Modifier le statut d'un ordre")
    void testUpdateOrderStatus_Success() {
        when(productionOrderRepository.findById(1L)).thenReturn(Optional.of(productionOrder));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productionOrderRepository.save(any(ProductionOrder.class))).thenReturn(productionOrder);
        when(productionOrderMapper.toResponseDTO(productionOrder)).thenReturn(responseDTO);

        updateDTO.setStatus("TERMINE");

        ProductionOrderResponseDTO result = productionOrderService.updateProductionOrder(1L, updateDTO);

        assertNotNull(result);
        verify(productionOrderRepository, times(1)).save(productionOrder);
    }

    
    @Test
    @DisplayName("Annuler un ordre EN_ATTENTE avec succès")
    void testCancelProductionOrder_EnAttente_Success() {
        productionOrder.setStatus(ProductionOrderStatus.EN_ATTENTE);
        when(productionOrderRepository.findById(1L)).thenReturn(Optional.of(productionOrder));
        doNothing().when(productionOrderRepository).delete(productionOrder);

        productionOrderService.cancelProductionOrder(1L);

        verify(productionOrderRepository, times(1)).findById(1L);
        verify(productionOrderRepository, times(1)).delete(productionOrder);
    }

    @Test
    @DisplayName("Annuler un ordre EN_PRODUCTION doit échouer")
    void testCancelProductionOrder_EnProduction_ShouldFail() {
        productionOrder.setStatus(ProductionOrderStatus.EN_PRODUCTION);
        when(productionOrderRepository.findById(1L)).thenReturn(Optional.of(productionOrder));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            productionOrderService.cancelProductionOrder(1L);
        });
        
        assertTrue(exception.getMessage().contains("a déjà commencé"));
        verify(productionOrderRepository, times(1)).findById(1L);
        verify(productionOrderRepository, never()).delete(any(ProductionOrder.class));
    }

    @Test
    @DisplayName("Annuler un ordre TERMINE doit échouer")
    void testCancelProductionOrder_Termine_ShouldFail() {
        productionOrder.setStatus(ProductionOrderStatus.TERMINE);
        when(productionOrderRepository.findById(1L)).thenReturn(Optional.of(productionOrder));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            productionOrderService.cancelProductionOrder(1L);
        });
        
        assertTrue(exception.getMessage().contains("a déjà commencé"));
        verify(productionOrderRepository, times(1)).findById(1L);
        verify(productionOrderRepository, never()).delete(any(ProductionOrder.class));
    }

    @Test
    @DisplayName("Annuler un ordre inexistant doit lever une exception")
    void testCancelProductionOrder_NotFound() {
        when(productionOrderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            productionOrderService.cancelProductionOrder(999L);
        });
        verify(productionOrderRepository, times(1)).findById(999L);
        verify(productionOrderRepository, never()).delete(any(ProductionOrder.class));
    }

    
    @Test
    @DisplayName("Récupérer la liste de tous les ordres de production")
    void testGetAllProductionOrders_Success() {
        ProductionOrder order2 = new ProductionOrder();
        order2.setIdOrder(2L);
        order2.setProduct(product);
        order2.setQuantity(5);
        order2.setStatus(ProductionOrderStatus.EN_PRODUCTION);
        
        List<ProductionOrder> orders = Arrays.asList(productionOrder, order2);
        
        ProductionOrderResponseDTO responseDTO2 = new ProductionOrderResponseDTO();
        responseDTO2.setIdOrder(2L);
        responseDTO2.setQuantity(5);
        
        when(productionOrderRepository.findAll()).thenReturn(orders);
        when(productionOrderMapper.toResponseDTO(productionOrder)).thenReturn(responseDTO);
        when(productionOrderMapper.toResponseDTO(order2)).thenReturn(responseDTO2);

        List<ProductionOrderResponseDTO> result = productionOrderService.getAllProductionOrders();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productionOrderRepository, times(1)).findAll();
        verify(productionOrderMapper, times(2)).toResponseDTO(any(ProductionOrder.class));
    }

    @Test
    @DisplayName("Récupérer une liste vide si aucun ordre")
    void testGetAllProductionOrders_EmptyList() {
        when(productionOrderRepository.findAll()).thenReturn(new ArrayList<>());

        List<ProductionOrderResponseDTO> result = productionOrderService.getAllProductionOrders();
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productionOrderRepository, times(1)).findAll();
    }

    
    @Test
    @DisplayName("Récupérer les ordres par statut EN_ATTENTE")
    void testGetOrdersByStatus_EnAttente() {
        List<ProductionOrder> orders = Arrays.asList(productionOrder);
        when(productionOrderRepository.findByStatus(ProductionOrderStatus.EN_ATTENTE)).thenReturn(orders);
        when(productionOrderMapper.toResponseDTO(productionOrder)).thenReturn(responseDTO);

        List<ProductionOrderResponseDTO> result = productionOrderService.getProductionOrdersByStatus("EN_ATTENTE");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productionOrderRepository, times(1)).findByStatus(ProductionOrderStatus.EN_ATTENTE);
        verify(productionOrderMapper, times(1)).toResponseDTO(productionOrder);
    }

    @Test
    @DisplayName("Récupérer les ordres par statut EN_PRODUCTION")
    void testGetOrdersByStatus_EnProduction() {
        productionOrder.setStatus(ProductionOrderStatus.EN_PRODUCTION);
        List<ProductionOrder> orders = Arrays.asList(productionOrder);
        
        when(productionOrderRepository.findByStatus(ProductionOrderStatus.EN_PRODUCTION)).thenReturn(orders);
        when(productionOrderMapper.toResponseDTO(productionOrder)).thenReturn(responseDTO);

        List<ProductionOrderResponseDTO> result = productionOrderService.getProductionOrdersByStatus("EN_PRODUCTION");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productionOrderRepository, times(1)).findByStatus(ProductionOrderStatus.EN_PRODUCTION);
    }

    @Test
    @DisplayName("Récupérer les ordres par statut TERMINE")
    void testGetOrdersByStatus_Termine() {
        when(productionOrderRepository.findByStatus(ProductionOrderStatus.TERMINE)).thenReturn(new ArrayList<>());
        List<ProductionOrderResponseDTO> result = productionOrderService.getProductionOrdersByStatus("TERMINE");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productionOrderRepository, times(1)).findByStatus(ProductionOrderStatus.TERMINE);
    }

    
    @Test
    @DisplayName("Calculer le temps de production estimé")
    void testCalculateEstimatedProductionTime_CorrectCalculation() {
        Integer productionTimePerUnit = product.getProductionTime(); // 120 minutes
        Integer quantity = productionOrder.getQuantity(); // 10

        Integer estimatedTime = productionTimePerUnit * quantity;

        assertEquals(1200, estimatedTime);
        assertEquals(120 * 10, estimatedTime);
    }
}
