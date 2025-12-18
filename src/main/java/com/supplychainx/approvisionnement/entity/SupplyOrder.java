package com.supplychainx.approvisionnement.entity;

import com.supplychainx.approvisionnement.enums.SupplyOrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "supply_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplyOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idOrder;
    
    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;
    
    @OneToMany(mappedBy = "supplyOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplyOrderMaterial> orderMaterials = new ArrayList<>();
    
    @Column(nullable = false)
    private LocalDate orderDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupplyOrderStatus status;
}
