package com.supplychainx.livraison.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplychainx.common.entity.User;
import com.supplychainx.common.enums.UserRole;
import com.supplychainx.common.repository.UserRepository;
import com.supplychainx.livraison.dto.DeliveryRequestDTO;
import com.supplychainx.livraison.entity.Customer;
import com.supplychainx.livraison.entity.Delivery;
import com.supplychainx.livraison.entity.Order;
import com.supplychainx.livraison.enums.DeliveryStatus;
import com.supplychainx.livraison.enums.OrderStatus;
import com.supplychainx.livraison.repository.CustomerRepository;
import com.supplychainx.livraison.repository.DeliveryRepository;
import com.supplychainx.livraison.repository.OrderRepository;
import com.supplychainx.production.entity.Product;
import com.supplychainx.production.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "admin", roles = {"ADMIN"})
class DeliveryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private String testUserEmail = "admin@test.com";
    private String testUserPassword = "password123";
    
    private Customer testCustomer;
    private Product testProduct;
    private Order testOrder;
    private Delivery testDelivery;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User testUser = new User();
        testUser.setEmail(testUserEmail);
        testUser.setPassword(testUserPassword);
        testUser.setRole(UserRole.ADMIN);
        testUser.setFirstName("Admin");
        testUser.setLastName("Test");
        userRepository.save(testUser);

        deliveryRepository.deleteAll();
        orderRepository.deleteAll();
        customerRepository.deleteAll();
        productRepository.deleteAll();

        testCustomer = new Customer();
        testCustomer.setName("Client Test");
        testCustomer.setAddress("123 Rue Test");
        testCustomer.setCity("Paris");
        testCustomer = customerRepository.save(testCustomer);

        testProduct = new Product();
        testProduct.setName("Produit Test");
        testProduct.setProductionTime(120);
        testProduct.setCost(500.0);
        testProduct.setStock(100);
        testProduct = productRepository.save(testProduct);

        testOrder = new Order();
        testOrder.setCustomer(testCustomer);
        testOrder.setProduct(testProduct);
        testOrder.setQuantity(10);
        testOrder.setStatus(OrderStatus.EN_PREPARATION);
        testOrder = orderRepository.save(testOrder);

        testDelivery = new Delivery();
        testDelivery.setOrder(testOrder);
        testDelivery.setVehicle("Camion");
        testDelivery.setDriver("Jean Dupont");
        testDelivery.setStatus(DeliveryStatus.PLANIFIEE);
        testDelivery.setDeliveryDate(LocalDate.now().plusDays(3));
        testDelivery.setCost(550.0);
        testDelivery = deliveryRepository.save(testDelivery);
    }


    @Test
    @DisplayName("POST /api/deliveries - Créer avec succès (stock suffisant)")
    void testCreateDelivery_WithSufficientStock_Success() throws Exception {
        Order newOrder = new Order();
        newOrder.setCustomer(testCustomer);
        newOrder.setProduct(testProduct);
        newOrder.setQuantity(5);  
        newOrder.setStatus(OrderStatus.EN_PREPARATION);
        newOrder = orderRepository.save(newOrder);

        DeliveryRequestDTO dto = new DeliveryRequestDTO();
        dto.setOrderId(newOrder.getIdOrder());
        dto.setVehicle("Fourgon");
        dto.setDriver("Marie Martin");
        dto.setStatus("PLANIFIEE");
        dto.setDeliveryDate(LocalDate.now().plusDays(2));
        dto.setCost(300.0);

        mockMvc.perform(post("/api/deliveries")
                .header("email", testUserEmail).header("password", testUserPassword)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vehicle", is("Fourgon")))
                .andExpect(jsonPath("$.driver", is("Marie Martin")))
                .andExpect(jsonPath("$.status", is("PLANIFIEE")))
                .andExpect(jsonPath("$.cost", notNullValue()))
                .andExpect(jsonPath("$.idDelivery", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/deliveries - Commande inexistante")
    void testCreateDelivery_OrderNotFound() throws Exception {
        DeliveryRequestDTO dto = new DeliveryRequestDTO();
        dto.setOrderId(999L); 
        dto.setVehicle("Camion");
        dto.setDriver("Test");
        dto.setStatus("PLANIFIEE");
        dto.setDeliveryDate(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/deliveries")
                .header("email", testUserEmail).header("password", testUserPassword)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/deliveries - Calcul automatique du coût")
    void testCreateDelivery_AutomaticCostCalculation() throws Exception {
        Order newOrder = new Order();
        newOrder.setCustomer(testCustomer);
        newOrder.setProduct(testProduct);
        newOrder.setQuantity(10);
        newOrder.setStatus(OrderStatus.EN_PREPARATION);
        newOrder = orderRepository.save(newOrder);

        DeliveryRequestDTO dto = new DeliveryRequestDTO();
        dto.setOrderId(newOrder.getIdOrder());
        dto.setVehicle("Camion");
        dto.setDriver("Pierre Dubois");
        dto.setStatus("PLANIFIEE");
        dto.setDeliveryDate(LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/deliveries")
                .header("email", testUserEmail).header("password", testUserPassword)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cost", notNullValue()))
                .andExpect(jsonPath("$.cost", greaterThan(0.0)));
    }


    @Test
    @DisplayName("GET /api/deliveries/{id} - Livraison trouvée")
    void testGetDeliveryById_Found() throws Exception {
        mockMvc.perform(get("/api/deliveries/{id}", testDelivery.getIdDelivery())
                .header("email", testUserEmail).header("password", testUserPassword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDelivery", is(testDelivery.getIdDelivery().intValue())))
                .andExpect(jsonPath("$.vehicle", is("Camion")))
                .andExpect(jsonPath("$.driver", is("Jean Dupont")))
                .andExpect(jsonPath("$.status", is("PLANIFIEE")));
    }

    @Test
    @DisplayName("GET /api/deliveries/{id} - Livraison non trouvée")
    void testGetDeliveryById_NotFound() throws Exception {
        mockMvc.perform(get("/api/deliveries/{id}", 999L)
                .header("email", testUserEmail).header("password", testUserPassword))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("PUT /api/deliveries/{id}/status - PLANIFIEE → EN_COURS")
    void testUpdateDeliveryStatus_PlanifieeToEnCours() throws Exception {
        testDelivery.setStatus(DeliveryStatus.PLANIFIEE);
        deliveryRepository.save(testDelivery);

        mockMvc.perform(put("/api/deliveries/{id}/status", testDelivery.getIdDelivery())
                .header("email", testUserEmail).header("password", testUserPassword)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"EN_COURS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("EN_COURS")));
    }

    @Test
    @DisplayName("PUT /api/deliveries/{id}/status - EN_COURS → LIVREE (met à jour order.status)")
    void testUpdateDeliveryStatus_ToLivree_ShouldUpdateOrder() throws Exception {
        testDelivery.setStatus(DeliveryStatus.EN_COURS);
        deliveryRepository.save(testDelivery);

        testOrder.setStatus(OrderStatus.EN_ROUTE);
        orderRepository.save(testOrder);

        mockMvc.perform(put("/api/deliveries/{id}/status", testDelivery.getIdDelivery())
                .header("email", testUserEmail).header("password", testUserPassword)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"LIVREE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("LIVREE")));

        Order updatedOrder = orderRepository.findById(testOrder.getIdOrder()).orElseThrow();
        assert updatedOrder.getStatus() == OrderStatus.LIVREE : "Le statut de la commande devrait être LIVREE";
    }


    @Test
    @DisplayName("POST /api/deliveries/{id}/calculate-cost - Calcul correct")
    void testCalculateDeliveryCost_CorrectCalculation() throws Exception {
        Double baseCost = 100.0;
        Double distance = 50.0; 
        Double ratePerKm = 2.0; 
        Double expectedCost = baseCost + (distance * ratePerKm); 
        mockMvc.perform(post("/api/deliveries/{id}/calculate-cost", testDelivery.getIdDelivery())
                .header("email", testUserEmail).header("password", testUserPassword)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format(java.util.Locale.US, "{\"baseCost\": %.2f, \"distance\": %.2f, \"ratePerKm\": %.2f}", 
                        baseCost, distance, ratePerKm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cost", is(expectedCost)));
    }

    @Test
    @DisplayName("POST /api/deliveries/{id}/calculate-cost - Mise à jour du coût")
    void testCalculateDeliveryCost_UpdatesCost() throws Exception {
        Double initialCost = testDelivery.getCost();
        
        mockMvc.perform(post("/api/deliveries/{id}/calculate-cost", testDelivery.getIdDelivery())
                .header("email", testUserEmail).header("password", testUserPassword)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"baseCost\": 150.0, \"distance\": 30.0, \"ratePerKm\": 2.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cost", is(225.0))); 

        Delivery updatedDelivery = deliveryRepository.findById(testDelivery.getIdDelivery()).orElseThrow();
        assert updatedDelivery.getCost() != initialCost : "Le coût devrait avoir été mis à jour";
    }


    @Test
    @DisplayName("GET /api/deliveries?status=PLANIFIEE - Filtrer par statut")
    void testGetDeliveriesByStatus_Planifiee() throws Exception {
        // Create a new order for the second delivery to avoid unique constraint violation
        Order newOrder = new Order();
        newOrder.setCustomer(testCustomer);
        newOrder.setProduct(testProduct);
        newOrder.setQuantity(5);
        newOrder.setStatus(OrderStatus.EN_PREPARATION);
        newOrder = orderRepository.save(newOrder);
        
        Delivery delivery2 = new Delivery();
        delivery2.setOrder(newOrder);
        delivery2.setVehicle("Moto");
        delivery2.setDriver("Livreur Express");
        delivery2.setStatus(DeliveryStatus.EN_COURS);
        delivery2.setDeliveryDate(LocalDate.now().plusDays(1));
        delivery2.setCost(200.0);
        deliveryRepository.save(delivery2);

        mockMvc.perform(get("/api/deliveries")
                .header("email", testUserEmail).header("password", testUserPassword)
                .param("status", "PLANIFIEE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("PLANIFIEE")))
                .andExpect(jsonPath("$[0].vehicle", is("Camion")));
    }

    @Test
    @DisplayName("GET /api/deliveries?status=EN_COURS - Filtrer EN_COURS")
    void testGetDeliveriesByStatus_EnCours() throws Exception {
        testDelivery.setStatus(DeliveryStatus.EN_COURS);
        deliveryRepository.save(testDelivery);

        mockMvc.perform(get("/api/deliveries")
                .header("email", testUserEmail).header("password", testUserPassword)
                .param("status", "EN_COURS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("EN_COURS")));
    }


    @Test
    @DisplayName("Scénario complet - Workflow de livraison de A à Z")
    void testCompleteDeliveryWorkflow() throws Exception {
        Customer newCustomer = new Customer();
        newCustomer.setName("Nouveau Client");
        newCustomer.setAddress("456 Avenue Test");
        newCustomer.setCity("Lyon");
        newCustomer = customerRepository.save(newCustomer);

        Product newProduct = new Product();
        newProduct.setName("Produit Premium");
        newProduct.setProductionTime(180);
        newProduct.setCost(1000.0);
        newProduct.setStock(50);
        newProduct = productRepository.save(newProduct);

        Order newOrder = new Order();
        newOrder.setCustomer(newCustomer);
        newOrder.setProduct(newProduct);
        newOrder.setQuantity(5);
        newOrder.setStatus(OrderStatus.EN_PREPARATION);
        newOrder = orderRepository.save(newOrder);

        DeliveryRequestDTO createDTO = new DeliveryRequestDTO();
        createDTO.setOrderId(newOrder.getIdOrder());
        createDTO.setVehicle("Camion Premium");
        createDTO.setDriver("Expert Livreur");
        createDTO.setStatus("PLANIFIEE");
        createDTO.setDeliveryDate(LocalDate.now().plusDays(7));
        createDTO.setCost(800.0);

        String createdDeliveryJson = mockMvc.perform(post("/api/deliveries")
                .header("email", testUserEmail).header("password", testUserPassword)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("PLANIFIEE")))
                .andReturn().getResponse().getContentAsString();

        Long deliveryId = objectMapper.readTree(createdDeliveryJson).get("idDelivery").asLong();

        mockMvc.perform(post("/api/deliveries/{id}/calculate-cost", deliveryId)
                .header("email", testUserEmail).header("password", testUserPassword)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"baseCost\": 500.0, \"distance\": 100.0, \"ratePerKm\": 3.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cost", is(800.0)));  

        mockMvc.perform(put("/api/deliveries/{id}/status", deliveryId)
                .header("email", testUserEmail).header("password", testUserPassword)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"LIVREE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("LIVREE")));

        Order finalOrder = orderRepository.findById(newOrder.getIdOrder()).orElseThrow();
        assert finalOrder.getStatus() == OrderStatus.LIVREE : 
            "Le statut de la commande devrait être LIVREE après la livraison";
    }
}

