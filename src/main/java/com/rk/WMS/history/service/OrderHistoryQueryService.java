package com.rk.WMS.history.service;


import com.rk.WMS.report.projection.DeliveryPerformanceProjection;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderHistoryQueryService {

    List<DeliveryPerformanceProjection> fetchDeliveryPerformanceData(
            List<Long> warehouseIds,
            LocalDateTime start,
            LocalDateTime end,
            Integer successStatus,
            Integer failStatus
    );

    List<Object[]> fetchStatisticWarehouseData(
            Integer status,
            List<Long> warehouseIds,
            LocalDateTime start,
            LocalDateTime end
    );

}
