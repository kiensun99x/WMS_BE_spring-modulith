package com.rk.WMS.warehouse.repository;

import com.rk.WMS.warehouse.model.Warehouse;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {

  Optional<Warehouse> findByWarehouseCode(String warehouseCode);
}
