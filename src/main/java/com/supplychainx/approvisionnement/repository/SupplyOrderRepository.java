package com.supplychainx.approvisionnement.repository;

import com.supplychainx.approvisionnement.entity.SupplyOrder;
import com.supplychainx.approvisionnement.enums.SupplyOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplyOrderRepository extends JpaRepository<SupplyOrder, Long> {
    
    List<SupplyOrder> findByStatus(SupplyOrderStatus status);
    
    long countBySupplier_IdSupplierAndStatusIn(Long supplierId, List<SupplyOrderStatus> statuses);
}
