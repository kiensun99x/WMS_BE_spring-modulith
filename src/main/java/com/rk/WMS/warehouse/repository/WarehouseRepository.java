package com.rk.WMS.warehouse.repository;

import com.rk.WMS.warehouse.model.Warehouse;
import java.util.Map;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {

    @Query("""
        SELECT w FROM Warehouse w
        WHERE w.status = 1
          AND w.availableSlots > 0
    """)
    List<Warehouse> findAvailableWarehouses();

}
