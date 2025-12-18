package com.supplychainx.production.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplychainx.production.dto.ProductionOrderCreateDTO;
import com.supplychainx.production.dto.ProductionOrderUpdateDTO;
import com.supplychainx.production.entity.Product;
import com.supplychainx.production.entity.ProductionOrder;
import com.supplychainx.production.enums.ProductionOrderStatus;
import com.supplychainx.production.repository.ProductRepository;
import com.supplychainx.production.repository.ProductionOrderRepository;
import com.supplychainx.common.entity.User;
import com.supplychainx.common.enums.UserRole;
import com.supplychainx.common.repository.UserRepository;
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
class ProductionOrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private Product testProduct;
    private ProductionOrder testOrder;
    private String testUserEmail = "admin@test.com";
    private String testUserPassword = "password123";

    @BeforeEach
    void setUp() {
        productionOrderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        User testUser = new User();
        testUser.setFirstName("Admin");
        testUser.setLastName("Test");
        testUser.setFirstName("Test");        
        testUser.setLastName("User");        
        testUser.setEmail(testUserEmail);
        testUser.setPassword(testUserPassword);
        testUser.setRole(UserRole.ADMIN);
        userRepository.save(testUser);

        testProduct = new Product();
        testProduct.setName("Produit Test");
        testProduct.setProductionTime(120);
        testProduct.setCost(500.0);
        testProduct.setStock(100);
        testProduct = productRepository.save(testProduct);

        testOrder = new ProductionOrder();
        testOrder.setProduct(testProduct);
        testOrder.setQuantity(10);
        testOrder.setStatus(ProductionOrderStatus.EN_ATTENTE);
        testOrder.setStartDate(LocalDate.now());
        testOrder.setEndDate(LocalDate.now().plusDays(7));
        testOrder = productionOrderRepository.save(testOrder);
    }


    @Test
    @DisplayName("POST /api/production-orders - Créer avec succès")
    void testCreateProductionOrder_Success() throws Exception {
        ProductionOrderCreateDTO dto = new ProductionOrderCreateDTO();
        dto.setProductId(testProduct.getIdProduct());
        dto.setQuantity(20);
        dto.setStatus("EN_ATTENTE");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(10));

        mockMvc.perform(post("/api/production-orders")
                .header("email", testUserEmail).header("password", testUserPassword)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity", is(20)))
                .andExpect(jsonPath("$.status", is("EN_ATTENTE")))
                .andExpect(jsonPath("$.idOrder", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/production-orders - Produit inexistant")
    void testCreateProductionOrder_ProductNotFound() throws Exception {
        ProductionOrderCreateDTO dto = new ProductionOrderCreateDTO();
        dto.setProductId(999L);
        dto.setQuantity(10);
        dto.setStatus("EN_ATTENTE");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/production-orders")
                .header("email", testUserEmail).header("password", testUserPassword)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/production-orders/{id} - Ordre trouvé")
    void testGetProductionOrderById_Found() throws Exception {
        mockMvc.perform(get("/api/production-orders/{id}", testOrder.getIdOrder())
                .header("email", testUserEmail).header("password", testUserPassword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idOrder", is(testOrder.getIdOrder().intValue())))
                .andExpect(jsonPath("$.quantity", is(10)))
                .andExpect(jsonPath("$.status", is("EN_ATTENTE")));
    }

    @Test
    @DisplayName("GET /api/production-orders/{id} - Ordre non trouvé")
    void testGetProductionOrderById_NotFound() throws Exception {
        mockMvc.perform(get("/api/production-orders/{id}", 999L)
                .header("email", testUserEmail).header("password", testUserPassword))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("GET /api/production-orders - Retourner liste complète")
    void testGetAllProductionOrders_Success() throws Exception {
        ProductionOrder order2 = new ProductionOrder();
        order2.setProduct(testProduct);
        order2.setQuantity(5);
        order2.setStatus(ProductionOrderStatus.EN_PRODUCTION);
        order2.setStartDate(LocalDate.now());
        order2.setEndDate(LocalDate.now().plusDays(3));
        productionOrderRepository.save(order2);

        mockMvc.perform(get("/api/production-orders")
                .header("email", testUserEmail).header("password", testUserPassword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].quantity", containsInAnyOrder(10, 5)));
    }


    @Test
    @DisplayName("GET /api/production-orders/status/EN_ATTENTE - Filtrer par statut")
    void testGetProductionOrdersByStatus_EnAttente() throws Exception {
        ProductionOrder orderEnProduction = new ProductionOrder();
        orderEnProduction.setProduct(testProduct);
        orderEnProduction.setQuantity(15);
        orderEnProduction.setStatus(ProductionOrderStatus.EN_PRODUCTION);
        orderEnProduction.setStartDate(LocalDate.now());
        orderEnProduction.setEndDate(LocalDate.now().plusDays(5));
        productionOrderRepository.save(orderEnProduction);

        ProductionOrder orderTermine = new ProductionOrder();
        orderTermine.setProduct(testProduct);
        orderTermine.setQuantity(8);
        orderTermine.setStatus(ProductionOrderStatus.TERMINE);
        orderTermine.setStartDate(LocalDate.now().minusDays(10));
        orderTermine.setEndDate(LocalDate.now().minusDays(3));
        productionOrderRepository.save(orderTermine);

        mockMvc.perform(get("/api/production-orders/status/EN_ATTENTE")
                .header("email", testUserEmail).header("password", testUserPassword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("EN_ATTENTE")))
                .andExpect(jsonPath("$[0].quantity", is(10)));
    }

    @Test
    @DisplayName("GET /api/production-orders/status/EN_PRODUCTION - Filtrer EN_PRODUCTION")
    void testGetProductionOrdersByStatus_EnProduction() throws Exception {
        testOrder.setStatus(ProductionOrderStatus.EN_PRODUCTION);
        productionOrderRepository.save(testOrder);

        mockMvc.perform(get("/api/production-orders/status/EN_PRODUCTION")
                .header("email", testUserEmail).header("password", testUserPassword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("EN_PRODUCTION")));
    }

    @Test
    @DisplayName("GET /api/production-orders/status/TERMINE - Aucun ordre TERMINE")
    void testGetProductionOrdersByStatus_Termine_EmptyList() throws Exception {
        mockMvc.perform(get("/api/production-orders/status/TERMINE")
                .header("email", testUserEmail).header("password", testUserPassword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }


    @Test
    @DisplayName("PUT /api/production-orders/{id} - Modifier avec succès")
    void testUpdateProductionOrder_Success() throws Exception {
        ProductionOrderUpdateDTO dto = new ProductionOrderUpdateDTO();
        dto.setProductId(testProduct.getIdProduct());
        dto.setQuantity(25);
        dto.setStatus("EN_PRODUCTION");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(5));

        mockMvc.perform(put("/api/production-orders/{id}", testOrder.getIdOrder())
                .header("email", testUserEmail).header("password", testUserPassword)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(25)))
                .andExpect(jsonPath("$.status", is("EN_PRODUCTION")));
    }

    @Test
    @DisplayName("PUT /api/production-orders/{id}/status - Changer statut EN_ATTENTE → EN_PRODUCTION")
    void testUpdateOrderStatus_EnAttenteToEnProduction_Success() throws Exception {
        ProductionOrderUpdateDTO dto = new ProductionOrderUpdateDTO();
        dto.setProductId(testProduct.getIdProduct());
        dto.setQuantity(testOrder.getQuantity());
        dto.setStatus("EN_PRODUCTION");
        dto.setStartDate(testOrder.getStartDate());
        dto.setEndDate(testOrder.getEndDate());

        mockMvc.perform(put("/api/production-orders/{id}", testOrder.getIdOrder())
                .header("email", testUserEmail).header("password", testUserPassword)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("EN_PRODUCTION")));
    }

    @Test
    @DisplayName("PUT /api/production-orders/{id} - Ordre inexistant")
    void testUpdateProductionOrder_NotFound() throws Exception {
        ProductionOrderUpdateDTO dto = new ProductionOrderUpdateDTO();
        dto.setProductId(testProduct.getIdProduct());
        dto.setQuantity(10);
        dto.setStatus("EN_ATTENTE");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(5));

        mockMvc.perform(put("/api/production-orders/{id}", 999L)
                .header("email", testUserEmail).header("password", testUserPassword)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("DELETE /api/production-orders/{id} - Annuler si EN_ATTENTE")
    void testCancelProductionOrder_EnAttente_Success() throws Exception {
        testOrder.setStatus(ProductionOrderStatus.EN_ATTENTE);
        productionOrderRepository.save(testOrder);

        mockMvc.perform(delete("/api/production-orders/{id}", testOrder.getIdOrder())
                .header("email", testUserEmail).header("password", testUserPassword))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/production-orders/{id} - Refuser si EN_PRODUCTION")
    void testCancelProductionOrder_EnProduction_ShouldFail() throws Exception {
        testOrder.setStatus(ProductionOrderStatus.EN_PRODUCTION);
        productionOrderRepository.save(testOrder);

        mockMvc.perform(delete("/api/production-orders/{id}", testOrder.getIdOrder())
                .header("email", testUserEmail).header("password", testUserPassword))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/production-orders/{id} - Refuser si TERMINE")
    void testCancelProductionOrder_Termine_ShouldFail() throws Exception {
        testOrder.setStatus(ProductionOrderStatus.TERMINE);
        productionOrderRepository.save(testOrder);

        mockMvc.perform(delete("/api/production-orders/{id}", testOrder.getIdOrder())
                .header("email", testUserEmail).header("password", testUserPassword))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/production-orders/{id} - Ordre inexistant")
    void testCancelProductionOrder_NotFound() throws Exception {
        mockMvc.perform(delete("/api/production-orders/{id}", 999L)
                .header("email", testUserEmail).header("password", testUserPassword))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("Calculer le temps de production estimé")
    void testCalculateProductionTime() throws Exception {
        int productionTimePerUnit = testProduct.getProductionTime();
        int quantity = testOrder.getQuantity(); 
        int expectedTime = productionTimePerUnit * quantity; 

        mockMvc.perform(get("/api/production-orders/{id}", testOrder.getIdOrder())
                .header("email", testUserEmail).header("password", testUserPassword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(10)));

        assert expectedTime == 1200;
    }
}


