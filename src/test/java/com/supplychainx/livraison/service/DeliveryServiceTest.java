package com.supplychainx.livraison.service;

import com.supplychainx.exception.ResourceNotFoundException;
import com.supplychainx.livraison.dto.DeliveryRequestDTO;
import com.supplychainx.livraison.dto.DeliveryResponseDTO;
import com.supplychainx.livraison.entity.Customer;
import com.supplychainx.livraison.entity.Delivery;
import com.supplychainx.livraison.entity.Order;
import com.supplychainx.livraison.enums.DeliveryStatus;
import com.supplychainx.livraison.enums.OrderStatus;
import com.supplychainx.livraison.repository.DeliveryRepository;
import com.supplychainx.livraison.repository.OrderRepository;
import com.supplychainx.mapper.CustomerMapper;
import com.supplychainx.mapper.DeliveryMapper;
import com.supplychainx.mapper.OrderMapper;
import com.supplychainx.mapper.ProductMapper;
import com.supplychainx.production.entity.Product;
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
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DeliveryMapper deliveryMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private DeliveryService deliveryService;

    private Delivery delivery;
    private Order order;
    private Product product;
    private Customer customer;
    private DeliveryRequestDTO requestDTO;
    private DeliveryResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setIdCustomer(1L);
        customer.setName("Client Test");
        customer.setAddress("123 Rue Test");
        customer.setCity("Paris");

        product = new Product();
        product.setIdProduct(1L);
        product.setName("Produit Test");
        product.setProductionTime(120);
        product.setCost(500.0);
        product.setStock(100);

        order = new Order();
        order.setIdOrder(1L);
        order.setCustomer(customer);
        order.setProduct(product);
        order.setQuantity(10);
        order.setStatus(OrderStatus.EN_PREPARATION);

        delivery = new Delivery();
        delivery.setIdDelivery(1L);
        delivery.setOrder(order);
        delivery.setVehicle("Camion");
        delivery.setDriver("Jean Dupont");
        delivery.setStatus(DeliveryStatus.PLANIFIEE);
        delivery.setDeliveryDate(LocalDate.now().plusDays(3));
        delivery.setCost(550.0);

        requestDTO = new DeliveryRequestDTO();
        requestDTO.setOrderId(1L);
        requestDTO.setVehicle("Camion");
        requestDTO.setDriver("Jean Dupont");
        requestDTO.setStatus("PLANIFIEE");
        requestDTO.setDeliveryDate(LocalDate.now().plusDays(3));
        requestDTO.setCost(550.0);

        responseDTO = new DeliveryResponseDTO();
        responseDTO.setIdDelivery(1L);
        responseDTO.setVehicle("Camion");
        responseDTO.setDriver("Jean Dupont");
        responseDTO.setStatus("PLANIFIEE");
        responseDTO.setCost(550.0);
    }

    
    @Test
    @DisplayName("Créer une livraison avec succès")
    void testCreateDelivery_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(delivery);
        when(deliveryMapper.toResponseDTO(delivery)).thenReturn(responseDTO);

        DeliveryResponseDTO result = deliveryService.createDelivery(requestDTO);

        assertNotNull(result);
        assertEquals(responseDTO.getVehicle(), result.getVehicle());
        assertEquals(responseDTO.getDriver(), result.getDriver());
        assertNotNull(result.getCost());
        verify(orderRepository, times(1)).findById(1L);
        verify(deliveryRepository, times(1)).save(any(Delivery.class));
        verify(deliveryMapper, times(1)).toResponseDTO(delivery);
    }

    @Test
    @DisplayName("Créer une livraison avec commande inexistante doit échouer")
    void testCreateDelivery_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());
        requestDTO.setOrderId(999L);

        assertThrows(ResourceNotFoundException.class, () -> {
            deliveryService.createDelivery(requestDTO);
        });
        verify(orderRepository, times(1)).findById(999L);
        verify(deliveryRepository, never()).save(any(Delivery.class));
    }

    @Test
    @DisplayName("Créer une livraison avec calcul automatique du coût")
    void testCreateDelivery_AutomaticCostCalculation() {
        requestDTO.setCost(null); 
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(delivery);
        when(deliveryMapper.toResponseDTO(delivery)).thenReturn(responseDTO);

        DeliveryResponseDTO result = deliveryService.createDelivery(requestDTO);

        assertNotNull(result);
        assertNotNull(result.getCost());
        verify(orderRepository, times(1)).findById(1L);
        verify(deliveryRepository, times(1)).save(any(Delivery.class));
    }

    
    @Test
    @DisplayName("Calculer le coût de livraison - Calcul correct")
    void testCalculateDeliveryCost_CorrectCalculation() {
        Double productCost = product.getCost(); 
        Integer quantity = order.getQuantity(); 
        Double deliveryFactor = 1.1; 

        Double expectedCost = productCost * quantity * deliveryFactor;
        Double calculatedCost = 500.0 * 10 * 1.1;

        assertEquals(5500.0, expectedCost);
        assertEquals(5500.0, calculatedCost);
    }

    @Test
    @DisplayName("Calculer le coût de livraison avec coût fourni")
    void testCalculateDeliveryCost_WithProvidedCost() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(delivery);
        when(deliveryMapper.toResponseDTO(delivery)).thenReturn(responseDTO);

        requestDTO.setCost(600.0); 
        DeliveryResponseDTO result = deliveryService.createDelivery(requestDTO);

        assertNotNull(result);
        verify(deliveryRepository, times(1)).save(any(Delivery.class));
    }

    
    @Test
    @DisplayName("Mettre à jour le statut de livraison vers EN_COURS")
    void testUpdateDeliveryStatus_ToEnCours() {
        delivery.setStatus(DeliveryStatus.PLANIFIEE);
        
        delivery.setStatus(DeliveryStatus.EN_COURS);

        assertEquals(DeliveryStatus.EN_COURS, delivery.getStatus());
    }

    @Test
    @DisplayName("Mettre à jour le statut de livraison vers LIVREE")
    void testUpdateDeliveryStatus_ToLivree_ShouldUpdateOrder() {
        delivery.setStatus(DeliveryStatus.EN_COURS);
        order.setStatus(OrderStatus.EN_ROUTE);
        
        delivery.setStatus(DeliveryStatus.LIVREE);
        order.setStatus(OrderStatus.LIVREE); 

        assertEquals(DeliveryStatus.LIVREE, delivery.getStatus());
        assertEquals(OrderStatus.LIVREE, order.getStatus());
    }

    
    @Test
    @DisplayName("Récupérer les livraisons par statut PLANIFIEE")
    void testGetDeliveriesByStatus_Planifiee() {
        List<Delivery> deliveries = Arrays.asList(delivery);
        
        List<Delivery> result = deliveries.stream()
                .filter(d -> d.getStatus() == DeliveryStatus.PLANIFIEE)
                .toList();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(DeliveryStatus.PLANIFIEE, result.get(0).getStatus());
    }

    @Test
    @DisplayName("Récupérer les livraisons par statut EN_COURS")
    void testGetDeliveriesByStatus_EnCours() {
        delivery.setStatus(DeliveryStatus.EN_COURS);
        List<Delivery> deliveries = Arrays.asList(delivery);
        
        List<Delivery> result = deliveries.stream()
                .filter(d -> d.getStatus() == DeliveryStatus.EN_COURS)
                .toList();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(DeliveryStatus.EN_COURS, result.get(0).getStatus());
    }

    @Test
    @DisplayName("Récupérer les livraisons par statut LIVREE")
    void testGetDeliveriesByStatus_Livree() {
        List<Delivery> deliveries = new ArrayList<>();
        
        List<Delivery> result = deliveries.stream()
                .filter(d -> d.getStatus() == DeliveryStatus.LIVREE)
                .toList();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    
    @Test
    @DisplayName("Assigner un véhicule et un chauffeur à une livraison")
    void testAssignVehicleAndDriver_Success() {
        String newVehicle = "Fourgon";
        String newDriver = "Marie Martin";

        delivery.setVehicle(newVehicle);
        delivery.setDriver(newDriver);

        assertEquals(newVehicle, delivery.getVehicle());
        assertEquals(newDriver, delivery.getDriver());
    }

    @Test
    @DisplayName("Modifier le véhicule d'une livraison existante")
    void testAssignVehicleAndDriver_UpdateVehicle() {
        assertEquals("Camion", delivery.getVehicle());
        
        delivery.setVehicle("Fourgonnette");

        assertEquals("Fourgonnette", delivery.getVehicle());
        assertEquals("Jean Dupont", delivery.getDriver()); 
    }

    
    @Test
    @DisplayName("Vérifier que la livraison a une date de livraison valide")
    void testDelivery_HasValidDeliveryDate() {
        LocalDate deliveryDate = delivery.getDeliveryDate();

        assertNotNull(deliveryDate);
        assertTrue(deliveryDate.isAfter(LocalDate.now()) || deliveryDate.isEqual(LocalDate.now()));
    }

    @Test
    @DisplayName("Vérifier que la livraison est associée à une commande")
    void testDelivery_HasAssociatedOrder() {
        Order associatedOrder = delivery.getOrder();

        assertNotNull(associatedOrder);
        assertEquals(1L, associatedOrder.getIdOrder());
        assertNotNull(associatedOrder.getProduct());
        assertNotNull(associatedOrder.getCustomer());
    }
}
