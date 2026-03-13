package com.rk.WMS.warehouse.service;

import com.rk.WMS.warehouse.model.Warehouse;

import java.util.List;
import java.util.Optional;

public interface WarehouseQueryService {

    Optional<Warehouse> findById(Long id);

    List<Warehouse> findAvailableWarehouses();

}