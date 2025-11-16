package com.supplychainx.livraison.service;

import com.supplychainx.exception.ResourceNotFoundException;
import com.supplychainx.livraison.dto.DeliveryRequestDTO;
import com.supplychainx.livraison.dto.DeliveryResponseDTO;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryService {
    
    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final DeliveryMapper deliveryMapper;
    private final OrderMapper orderMapper;
    private final CustomerMapper customerMapper;
    private final ProductMapper productMapper;

    @Transactional
    public DeliveryResponseDTO createDelivery(DeliveryRequestDTO dto) {
        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Commande non trouvée avec l'ID: " + dto.getOrderId()));
        
        Delivery delivery = new Delivery();
        delivery.setOrder(order);
        delivery.setVehicle(dto.getVehicle());
        delivery.setDriver(dto.getDriver());
        delivery.setStatus(DeliveryStatus.valueOf(dto.getStatus()));
        delivery.setDeliveryDate(dto.getDeliveryDate());
        
        Double calculatedCost = calculateDeliveryCost(order, dto.getCost());
        delivery.setCost(calculatedCost);
        
        Delivery savedDelivery = deliveryRepository.save(delivery);
        return deliveryMapper.toResponseDTO(savedDelivery);
    }
    

    private Double calculateDeliveryCost(Order order, Double baseCost) {
        if (baseCost != null && baseCost > 0) {
            return baseCost;
        }

        Double productCost = order.getProduct().getCost();
        Integer quantity = order.getQuantity();
        Double deliveryFactor = 1.1;
        
        return productCost * quantity * deliveryFactor;
    }
    
    public DeliveryResponseDTO getDeliveryById(Long id) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Livraison non trouvée avec l'ID: " + id));
        return deliveryMapper.toResponseDTO(delivery);
    }
    
    @Transactional
    public DeliveryResponseDTO updateDeliveryStatus(Long id, String newStatus) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Livraison non trouvée avec l'ID: " + id));
        
        DeliveryStatus status = DeliveryStatus.valueOf(newStatus);
        delivery.setStatus(status);
        
        // If delivery is marked as LIVREE, update the order status to LIVREE
        if (status == DeliveryStatus.LIVREE) {
            Order order = delivery.getOrder();
            order.setStatus(OrderStatus.LIVREE);
            orderRepository.save(order);
        }
        
        Delivery savedDelivery = deliveryRepository.save(delivery);
        return deliveryMapper.toResponseDTO(savedDelivery);
    }
    
    @Transactional
    public DeliveryResponseDTO calculateAndUpdateCost(Long id, Double baseCost, Double distance, Double ratePerKm) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Livraison non trouvée avec l'ID: " + id));
        
        Double calculatedCost = baseCost + (distance * ratePerKm);
        delivery.setCost(calculatedCost);
        
        Delivery savedDelivery = deliveryRepository.save(delivery);
        return deliveryMapper.toResponseDTO(savedDelivery);
    }
    
    public List<DeliveryResponseDTO> getDeliveriesByStatus(String status) {
        DeliveryStatus deliveryStatus = DeliveryStatus.valueOf(status);
        return deliveryRepository.findByStatus(deliveryStatus).stream()
                .map(deliveryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
