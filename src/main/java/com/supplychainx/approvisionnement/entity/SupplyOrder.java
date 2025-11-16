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
    
    @ManyToMany
    @JoinTable(
        name = "supply_order_materials",
        joinColumns = @JoinColumn(name = "supply_order_id"),
        inverseJoinColumns = @JoinColumn(name = "raw_material_id")
    )
    private List<RawMaterial> materials = new ArrayList<>();
    
    @Column(nullable = false)
    private LocalDate orderDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupplyOrderStatus status;
}
