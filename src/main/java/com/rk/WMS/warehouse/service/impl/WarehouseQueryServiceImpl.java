package com.rk.WMS.warehouse.service.impl;


import com.rk.WMS.warehouse.model.Warehouse;
import com.rk.WMS.warehouse.repository.WarehouseRepository;
import com.rk.WMS.warehouse.service.WarehouseQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WarehouseQueryServiceImpl implements WarehouseQueryService {

    private final WarehouseRepository warehouseRepository;

    @Override
    public Optional<Warehouse> findById(Long id) {
        return warehouseRepository.findById(id);
    }

    @Override
    public List<Warehouse> findAvailableWarehouses() {
        return warehouseRepository.findAvailableWarehouses();
    }
}
