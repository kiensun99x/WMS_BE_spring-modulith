package com.rk.WMS.warehouse.repository;

import com.rk.WMS.warehouse.model.Warehouse;
import java.util.Map;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {

}
