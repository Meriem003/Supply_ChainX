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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final CustomerMapper customerMapper;
    
    @Transactional
    public CustomerResponseDTO createCustomer(CustomerRequestDTO dto) {
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setAddress(dto.getAddress());
        customer.setCity(dto.getCity());
        
        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.toResponseDTO(savedCustomer);
    }
    
    @Transactional
    public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO dto) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client non trouvé avec l'ID: " + id));
        
        customer.setName(dto.getName());
        customer.setAddress(dto.getAddress());
        customer.setCity(dto.getCity());
        
        Customer updatedCustomer = customerRepository.save(customer);
        return customerMapper.toResponseDTO(updatedCustomer);
    }
    
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client non trouvé avec l'ID: " + id));
        
        List<Order> orders = orderRepository.findByCustomer(customer);
        if (!orders.isEmpty()) {
            throw new BusinessRuleException(
                    "Impossible de supprimer le client car il a " + orders.size() + 
                    " commande(s) associée(s)");
        }
        
        customerRepository.delete(customer);
    }
    
    @Transactional(readOnly = true)
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CustomerResponseDTO> searchCustomersByName(String name) {
        return customerRepository.findByNameContainingIgnoreCase(name).stream()
                .map(customerMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
