package com.supplychainx.approvisionnement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplychainx.approvisionnement.dto.SupplierCreateDTO;
import com.supplychainx.approvisionnement.dto.SupplierUpdateDTO;
import com.supplychainx.approvisionnement.entity.Supplier;
import com.supplychainx.approvisionnement.entity.SupplyOrder;
import com.supplychainx.approvisionnement.enums.SupplyOrderStatus;
import com.supplychainx.approvisionnement.repository.SupplierRepository;
import com.supplychainx.approvisionnement.repository.SupplyOrderRepository;
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
import java.util.ArrayList;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "admin", roles = {"ADMIN"})
class SupplierControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private SupplyOrderRepository supplyOrderRepository;

    @Autowired
    private UserRepository userRepository;

    private Supplier testSupplier;
    private String testUserEmail = "admin@test.com";
    private String testUserPassword = "password123";

    @BeforeEach
    void setUp() {
        supplyOrderRepository.deleteAll();
        supplierRepository.deleteAll();
        userRepository.deleteAll();

        User testUser = new User();
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail(testUserEmail);
        testUser.setPassword(testUserPassword);
        testUser.setRole(UserRole.ADMIN);
        userRepository.save(testUser);

        testSupplier = new Supplier();
        testSupplier.setName("Fournisseur Test");
        testSupplier.setContact("contact@test.com");
        testSupplier.setRating(4.5);
        testSupplier.setLeadTime(7);
        testSupplier.setOrders(new ArrayList<>());
        testSupplier = supplierRepository.save(testSupplier);
    }


    @Test
    @DisplayName("POST /api/suppliers - Créer un fournisseur avec succès")
    void testCreateSupplier_Success() throws Exception {
        SupplierCreateDTO dto = new SupplierCreateDTO();
        dto.setName("Nouveau Fournisseur");
        dto.setContact("nouveau@test.com");
        dto.setRating(4.0);
        dto.setLeadTime(5);
        mockMvc.perform(post("/api/suppliers")
                .header("email", testUserEmail)
                .header("password", testUserPassword)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Nouveau Fournisseur")))
                .andExpect(jsonPath("$.contact", is("nouveau@test.com")))
                .andExpect(jsonPath("$.rating", is(4.0)))
                .andExpect(jsonPath("$.leadTime", is(5)))
                .andExpect(jsonPath("$.idSupplier", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/suppliers - Échec avec données invalides")
    void testCreateSupplier_InvalidData() throws Exception {
        SupplierCreateDTO dto = new SupplierCreateDTO();
        dto.setContact("test@test.com");

        mockMvc.perform(post("/api/suppliers")
                .header("email", testUserEmail)
                .header("password", testUserPassword)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/suppliers/{id} - Fournisseur trouvé")
    void testGetSupplierById_Found() throws Exception {
        mockMvc.perform(get("/api/suppliers/{id}", testSupplier.getIdSupplier())
                .header("email", testUserEmail)
                .header("password", testUserPassword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idSupplier", is(testSupplier.getIdSupplier().intValue())))
                .andExpect(jsonPath("$.name", is("Fournisseur Test")))
                .andExpect(jsonPath("$.contact", is("contact@test.com")))
                .andExpect(jsonPath("$.rating", is(4.5)))
                .andExpect(jsonPath("$.leadTime", is(7)));
    }

    @Test
    @DisplayName("GET /api/suppliers/{id} - Fournisseur non trouvé")
    void testGetSupplierById_NotFound() throws Exception {
        mockMvc.perform(get("/api/suppliers/{id}", 999L)
                .header("email", testUserEmail).header("password", testUserPassword))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/suppliers - Retourner liste de fournisseurs")
    void testGetAllSuppliers_Success() throws Exception {
        Supplier supplier2 = new Supplier();
        supplier2.setName("Fournisseur 2");
        supplier2.setContact("contact2@test.com");
        supplier2.setRating(3.8);
        supplier2.setLeadTime(10);
        supplier2.setOrders(new ArrayList<>());
        supplierRepository.save(supplier2);

        mockMvc.perform(get("/api/suppliers")
                .header("email", testUserEmail).header("password", testUserPassword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", notNullValue()))
                .andExpect(jsonPath("$[1].name", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/suppliers - Liste vide")
    void testGetAllSuppliers_EmptyList() throws Exception {
        supplierRepository.deleteAll();

        mockMvc.perform(get("/api/suppliers")
                .header("email", testUserEmail).header("password", testUserPassword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }


    @Test
    @DisplayName("PUT /api/suppliers/{id} - Modifier avec succès")
    void testUpdateSupplier_Success() throws Exception {
        SupplierUpdateDTO dto = new SupplierUpdateDTO();
        dto.setName("Fournisseur Modifié");
        dto.setContact("modifie@test.com");
        dto.setRating(4.8);
        dto.setLeadTime(3);

        mockMvc.perform(put("/api/suppliers/{id}", testSupplier.getIdSupplier())
                .header("email", testUserEmail).header("password", testUserPassword)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Fournisseur Modifié")))
                .andExpect(jsonPath("$.contact", is("modifie@test.com")))
                .andExpect(jsonPath("$.rating", is(4.8)))
                .andExpect(jsonPath("$.leadTime", is(3)));
    }

    @Test
    @DisplayName("PUT /api/suppliers/{id} - Fournisseur inexistant")
    void testUpdateSupplier_NotFound() throws Exception {
        SupplierUpdateDTO dto = new SupplierUpdateDTO();
        dto.setName("Test");
        dto.setContact("test@test.com");
        dto.setRating(4.0);
        dto.setLeadTime(5);

        mockMvc.perform(put("/api/suppliers/{id}", 999L)
                .header("email", testUserEmail).header("password", testUserPassword)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("DELETE /api/suppliers/{id} - Supprimer sans commandes actives")
    void testDeleteSupplier_WithoutActiveOrders_Success() throws Exception {
        SupplyOrder completedOrder = new SupplyOrder();
        completedOrder.setSupplier(testSupplier);      
        completedOrder.setOrderDate(LocalDate.now());
        completedOrder.setStatus(SupplyOrderStatus.RECUE);
        supplyOrderRepository.save(completedOrder);

        mockMvc.perform(delete("/api/suppliers/{id}", testSupplier.getIdSupplier())
                .header("email", testUserEmail).header("password", testUserPassword))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/suppliers/{id} - Refuser si commandes actives")
    void testDeleteSupplier_WithActiveOrders_ShouldFail() throws Exception {
        SupplyOrder activeOrder = new SupplyOrder();
        activeOrder.setSupplier(testSupplier);      
        activeOrder.setOrderDate(LocalDate.now());
        activeOrder.setStatus(SupplyOrderStatus.EN_ATTENTE);
        supplyOrderRepository.save(activeOrder);

        mockMvc.perform(delete("/api/suppliers/{id}", testSupplier.getIdSupplier())
                .header("email", testUserEmail).header("password", testUserPassword))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/suppliers/{id} - Fournisseur inexistant")
    void testDeleteSupplier_NotFound() throws Exception {
        mockMvc.perform(delete("/api/suppliers/{id}", 999L)
                .header("email", testUserEmail).header("password", testUserPassword))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("GET /api/suppliers/search - Recherche avec résultats")
    void testSearchSuppliers_Found() throws Exception {
        mockMvc.perform(get("/api/suppliers/search")
                .header("email", testUserEmail).header("password", testUserPassword)
                .param("name", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name", containsString("Test")));
    }

    @Test
    @DisplayName("GET /api/suppliers/search - Recherche insensible à la casse")
    void testSearchSuppliers_CaseInsensitive() throws Exception {
        mockMvc.perform(get("/api/suppliers/search")
                .header("email", testUserEmail).header("password", testUserPassword)
                .param("name", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/suppliers/search - Aucun résultat")
    void testSearchSuppliers_NoResults() throws Exception {
        mockMvc.perform(get("/api/suppliers/search")
                .header("email", testUserEmail).header("password", testUserPassword)
                .param("name", "Inexistant"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}