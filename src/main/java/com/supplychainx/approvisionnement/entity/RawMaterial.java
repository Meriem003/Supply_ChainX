package com.supplychainx.approvisionnement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "raw_materials")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RawMaterial {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMaterial;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private Integer stock;
    
    @Column(nullable = false)
    private Integer stockMin;
    
    @Column(nullable = false)
    private String unit;
    
    @OneToMany(mappedBy = "rawMaterial")
    private List<SupplyOrderMaterial> supplyOrderMaterials = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(
        name = "raw_material_suppliers",
        joinColumns = @JoinColumn(name = "raw_material_id"),
        inverseJoinColumns = @JoinColumn(name = "supplier_id")
    )
    private List<Supplier> suppliers = new ArrayList<>();
}
