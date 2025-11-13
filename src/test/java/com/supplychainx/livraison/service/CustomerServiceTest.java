package com.supplychainx.livraison.service;

import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.ResourceNotFoundException;
import com.supplychainx.livraison.dto.CustomerRequestDTO;
import com.supplychainx.livraison.dto.CustomerResponseDTO;
import com.supplychainx.livraison.entity.Customer;
import com.supplychainx.livraison.entity.Order;
import com.supplychainx.livraison.repository.CustomerRepository;
import com.supplychainx.livraison.repository.OrderRepository;
import com.supplychainx.mapper.CustomerMapper;
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
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private CustomerRequestDTO requestDTO;
    private CustomerResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setIdCustomer(1L);
        customer.setName("Client Test");
        customer.setAddress("123 Rue Test");
        customer.setCity("Paris");

        requestDTO = new CustomerRequestDTO();
        requestDTO.setName("Nouveau Client");
        requestDTO.setAddress("456 Avenue Test");
        requestDTO.setCity("Lyon");

        responseDTO = new CustomerResponseDTO();
        responseDTO.setIdCustomer(1L);
        responseDTO.setName("Client Test");
        responseDTO.setAddress("123 Rue Test");
        responseDTO.setCity("Paris");
    }

    
    @Test
    @DisplayName("Créer un client avec succès")
    void testCreateCustomer_Success() {
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(customerMapper.toResponseDTO(customer)).thenReturn(responseDTO);

        CustomerResponseDTO result = customerService.createCustomer(requestDTO);

        assertNotNull(result);
        assertEquals(responseDTO.getName(), result.getName());
        assertEquals(responseDTO.getAddress(), result.getAddress());
        assertEquals(responseDTO.getCity(), result.getCity());
        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(customerMapper, times(1)).toResponseDTO(customer);
    }

    @Test
    @DisplayName("Créer un client avec toutes les informations obligatoires")
    void testCreateCustomer_WithAllRequiredFields() {
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(customerMapper.toResponseDTO(customer)).thenReturn(responseDTO);

        CustomerResponseDTO result = customerService.createCustomer(requestDTO);

        assertNotNull(result);
        assertNotNull(result.getName());
        assertNotNull(result.getAddress());
        assertNotNull(result.getCity());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    
    @Test
    @DisplayName("Modifier un client existant avec succès")
    void testUpdateCustomer_Success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(customerMapper.toResponseDTO(customer)).thenReturn(responseDTO);

        CustomerResponseDTO result = customerService.updateCustomer(1L, requestDTO);

        assertNotNull(result);
        verify(customerRepository, times(1)).findById(1L);
        verify(customerRepository, times(1)).save(customer);
        verify(customerMapper, times(1)).toResponseDTO(customer);
    }

    @Test
    @DisplayName("Modifier un client inexistant doit lever une exception")
    void testUpdateCustomer_NotFound() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            customerService.updateCustomer(999L, requestDTO);
        });
        verify(customerRepository, times(1)).findById(999L);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    
    @Test
    @DisplayName("Supprimer un client sans commandes avec succès")
    void testDeleteCustomer_WithoutActiveOrders_ShouldSucceed() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.findByCustomer(customer)).thenReturn(new ArrayList<>());
        doNothing().when(customerRepository).delete(customer);

        customerService.deleteCustomer(1L);

        verify(customerRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).findByCustomer(customer);
        verify(customerRepository, times(1)).delete(customer);
    }

    @Test
    @DisplayName("Supprimer un client avec commandes actives doit échouer")
    void testDeleteCustomer_WithActiveOrders_ShouldThrowException() {
        Order activeOrder = new Order();
        activeOrder.setIdOrder(1L);
        activeOrder.setCustomer(customer);
        
        List<Order> orders = Arrays.asList(activeOrder);
        
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.findByCustomer(customer)).thenReturn(orders);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            customerService.deleteCustomer(1L);
        });
        
        assertTrue(exception.getMessage().contains("commande(s) associée(s)"));
        verify(customerRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).findByCustomer(customer);
        verify(customerRepository, never()).delete(any(Customer.class));
    }

    @Test
    @DisplayName("Supprimer un client inexistant doit lever une exception")
    void testDeleteCustomer_NotFound() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            customerService.deleteCustomer(999L);
        });
        verify(customerRepository, times(1)).findById(999L);
        verify(customerRepository, never()).delete(any(Customer.class));
    }

    
    @Test
    @DisplayName("Récupérer la liste de tous les clients")
    void testGetAllCustomers_Success() {
        Customer customer2 = new Customer();
        customer2.setIdCustomer(2L);
        customer2.setName("Client 2");
        customer2.setAddress("789 Boulevard Test");
        customer2.setCity("Marseille");
        
        List<Customer> customers = Arrays.asList(customer, customer2);
        
        CustomerResponseDTO responseDTO2 = new CustomerResponseDTO();
        responseDTO2.setIdCustomer(2L);
        responseDTO2.setName("Client 2");
        
        when(customerRepository.findAll()).thenReturn(customers);
        when(customerMapper.toResponseDTO(customer)).thenReturn(responseDTO);
        when(customerMapper.toResponseDTO(customer2)).thenReturn(responseDTO2);

        List<CustomerResponseDTO> result = customerService.getAllCustomers();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(customerRepository, times(1)).findAll();
        verify(customerMapper, times(2)).toResponseDTO(any(Customer.class));
    }

    @Test
    @DisplayName("Récupérer une liste vide si aucun client")
    void testGetAllCustomers_EmptyList() {
        when(customerRepository.findAll()).thenReturn(new ArrayList<>());

        List<CustomerResponseDTO> result = customerService.getAllCustomers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(customerRepository, times(1)).findAll();
    }

    
    @Test
    @DisplayName("Rechercher un client par nom avec succès")
    void testSearchCustomerByName_Found() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByNameContainingIgnoreCase("Test")).thenReturn(customers);
        when(customerMapper.toResponseDTO(customer)).thenReturn(responseDTO);

        List<CustomerResponseDTO> result = customerService.searchCustomersByName("Test");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Client Test", result.get(0).getName());
        verify(customerRepository, times(1)).findByNameContainingIgnoreCase("Test");
        verify(customerMapper, times(1)).toResponseDTO(customer);
    }

    @Test
    @DisplayName("Rechercher un client - aucun résultat")
    void testSearchCustomerByName_NotFound() {
        when(customerRepository.findByNameContainingIgnoreCase("Inexistant")).thenReturn(new ArrayList<>());
        List<CustomerResponseDTO> result = customerService.searchCustomersByName("Inexistant");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(customerRepository, times(1)).findByNameContainingIgnoreCase("Inexistant");
    }

    @Test
    @DisplayName("Rechercher un client - insensible à la casse")
    void testSearchCustomerByName_CaseInsensitive() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByNameContainingIgnoreCase("test")).thenReturn(customers);
        when(customerMapper.toResponseDTO(customer)).thenReturn(responseDTO);

        List<CustomerResponseDTO> result = customerService.searchCustomersByName("test");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(customerRepository, times(1)).findByNameContainingIgnoreCase("test");
    }
}