package com.supplychainx.production.entity;

import com.supplychainx.approvisionnement.entity.RawMaterial;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bill_of_materials")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillOfMaterial {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bom")
    private Long idBOM;
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @ManyToOne
    @JoinColumn(name = "material_id", nullable = false)
    private RawMaterial material;
    
    @Column(nullable = false)
    private Integer quantity;
}
