package com.supplychainx.approvisionnement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "supply_order_materials")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplyOrderMaterial {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "supply_order_id", nullable = false)
    private SupplyOrder supplyOrder;
    
    @ManyToOne
    @JoinColumn(name = "raw_material_id", nullable = false)
    private RawMaterial rawMaterial;
    
    @Column(nullable = false)
    private Integer quantity;
}
