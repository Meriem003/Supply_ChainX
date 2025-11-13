package com.supplychainx.production.service;

import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.ResourceNotFoundException;
import com.supplychainx.production.dto.ProductCreateDTO;
import com.supplychainx.production.dto.ProductResponseDTO;
import com.supplychainx.production.dto.ProductUpdateDTO;
import com.supplychainx.production.entity.Product;
import com.supplychainx.production.entity.ProductionOrder;
import com.supplychainx.production.repository.ProductRepository;
import com.supplychainx.production.repository.ProductionOrderRepository;
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
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductionOrderRepository productionOrderRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductCreateDTO createDTO;
    private ProductUpdateDTO updateDTO;
    private ProductResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setIdProduct(1L);
        product.setName("Produit A");
        product.setProductionTime(120);
        product.setCost(500.0);
        product.setStock(50);

        createDTO = new ProductCreateDTO();
        createDTO.setName("Nouveau Produit");
        createDTO.setProductionTime(180);
        createDTO.setCost(750.0);
        createDTO.setStock(100);

        updateDTO = new ProductUpdateDTO();
        updateDTO.setName("Produit Modifié");
        updateDTO.setProductionTime(150);
        updateDTO.setCost(600.0);
        updateDTO.setStock(75);

        responseDTO = new ProductResponseDTO();
        responseDTO.setIdProduct(1L);
        responseDTO.setName("Produit A");
        responseDTO.setProductionTime(120);
        responseDTO.setCost(500.0);
        responseDTO.setStock(50);
    }

    
    @Test
    @DisplayName("Créer un produit avec succès")
    void testCreateProduct_Success() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponseDTO result = productService.createProduct(createDTO);

        assertNotNull(result);
        assertNotNull(result.getIdProduct());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Créer un produit avec toutes les informations obligatoires")
    void testCreateProduct_WithAllRequiredFields() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponseDTO result = productService.createProduct(createDTO);

        assertNotNull(result);
        assertNotNull(result.getName());
        assertNotNull(result.getProductionTime());
        assertNotNull(result.getCost());
        assertNotNull(result.getStock());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    
    @Test
    @DisplayName("Modifier un produit existant avec succès")
    void testUpdateProduct_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductResponseDTO result = productService.updateProduct(1L, updateDTO);
        assertNotNull(result);
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("Modifier un produit inexistant doit lever une exception")
    void testUpdateProduct_NotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(999L, updateDTO);
        });
        verify(productRepository, times(1)).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
    }

    
    @Test
    @DisplayName("Supprimer un produit sans ordres de production")
    void testDeleteProduct_WithoutProductionOrders_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productionOrderRepository.findByProduct(product)).thenReturn(new ArrayList<>());
        doNothing().when(productRepository).delete(product);

        productService.deleteProduct(1L);
        verify(productRepository, times(1)).findById(1L);
        verify(productionOrderRepository, times(1)).findByProduct(product);
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    @DisplayName("Supprimer un produit avec ordres actifs doit échouer")
    void testDeleteProduct_WithActiveProductionOrders_ShouldThrowException() {
        ProductionOrder activeOrder = new ProductionOrder();
        activeOrder.setIdOrder(1L);
        activeOrder.setProduct(product);
        
        List<ProductionOrder> orders = Arrays.asList(activeOrder);
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productionOrderRepository.findByProduct(product)).thenReturn(orders);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            productService.deleteProduct(1L);
        });
        
        assertTrue(exception.getMessage().contains("ordre(s) de production associé(s)"));
        verify(productRepository, times(1)).findById(1L);
        verify(productionOrderRepository, times(1)).findByProduct(product);
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    @DisplayName("Supprimer un produit inexistant doit lever une exception")
    void testDeleteProduct_NotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.deleteProduct(999L);
        });
        verify(productRepository, times(1)).findById(999L);
        verify(productRepository, never()).delete(any(Product.class));
    }

    
    @Test
    @DisplayName("Récupérer la liste de tous les produits")
    void testGetAllProducts_Success() {
        Product product2 = new Product();
        product2.setIdProduct(2L);
        product2.setName("Produit B");
        product2.setProductionTime(90);
        product2.setCost(300.0);
        product2.setStock(30);
        
        List<Product> products = Arrays.asList(product, product2);
        when(productRepository.findAll()).thenReturn(products);

        List<ProductResponseDTO> result = productService.getAllProducts();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Récupérer une liste vide si aucun produit")
    void testGetAllProducts_EmptyList() {
        when(productRepository.findAll()).thenReturn(new ArrayList<>());

        List<ProductResponseDTO> result = productService.getAllProducts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findAll();
    }

    
    @Test
    @DisplayName("Rechercher un produit par nom avec succès")
    void testSearchProductByName_Found() {
        List<Product> products = Arrays.asList(product);
        when(productRepository.findByNameContainingIgnoreCase("Produit")).thenReturn(products);

        List<ProductResponseDTO> result = productService.searchProductsByName("Produit");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Produit A", result.get(0).getName());
        verify(productRepository, times(1)).findByNameContainingIgnoreCase("Produit");
    }

    @Test
    @DisplayName("Rechercher un produit - aucun résultat")
    void testSearchProductByName_NotFound() {
        when(productRepository.findByNameContainingIgnoreCase("Inexistant")).thenReturn(new ArrayList<>());

        List<ProductResponseDTO> result = productService.searchProductsByName("Inexistant");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findByNameContainingIgnoreCase("Inexistant");
    }

    @Test
    @DisplayName("Rechercher un produit - insensible à la casse")
    void testSearchProductByName_CaseInsensitive() {
        List<Product> products = Arrays.asList(product);
        when(productRepository.findByNameContainingIgnoreCase("produit")).thenReturn(products);

        List<ProductResponseDTO> result = productService.searchProductsByName("produit");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository, times(1)).findByNameContainingIgnoreCase("produit");
    }

    
    @Test
    @DisplayName("Vérifier stock produit - Stock suffisant")
    void testCheckProductStock_Sufficient() {
        product.setStock(50);

        boolean result = product.getStock() >= 30;
        assertTrue(result, "Le stock devrait être suffisant");
        assertEquals(50, product.getStock());
    }

    @Test
    @DisplayName("Vérifier stock produit - Stock insuffisant")
    void testCheckProductStock_Insufficient() {
        product.setStock(10);

        boolean result = product.getStock() >= 30;

        assertFalse(result, "Le stock devrait être insuffisant");
        assertEquals(10, product.getStock());
    }
}
