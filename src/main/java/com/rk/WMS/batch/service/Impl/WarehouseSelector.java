package com.rk.WMS.batch.service.Impl;

import com.rk.WMS.common.exception.AppException;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.order.model.Order;
import com.rk.WMS.warehouse.model.Warehouse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WarehouseSelector {

    private final DistanceCalculator distanceCalculator;

    public Warehouse selectNearestWarehouse(Order order, List<Warehouse> warehouses) {

        return warehouses.stream()
                .min(Comparator.comparingDouble(
                        w -> distanceCalculator.distanceKm(
                                order.getReceiverLat(),
                                order.getReceiverLon(),
                                w.getLatitude(),
                                w.getLongitude()
                        )
                ))
                .orElseThrow(() ->
                        new AppException(ErrorCode.FAILED)
                );
    }
}

