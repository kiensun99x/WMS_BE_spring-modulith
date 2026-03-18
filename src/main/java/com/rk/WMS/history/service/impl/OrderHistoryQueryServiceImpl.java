package com.rk.WMS.history.service.impl;


import com.rk.WMS.history.repository.OrderHistoryRepository;
import com.rk.WMS.history.service.OrderHistoryQueryService;
import com.rk.WMS.report.projection.DeliveryPerformanceProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderHistoryQueryServiceImpl implements OrderHistoryQueryService {

    private final OrderHistoryRepository orderHistoryRepository;

    @Override
    public List<DeliveryPerformanceProjection> fetchDeliveryPerformanceData(
            List<Long> warehouseIds,
            LocalDateTime start,
            LocalDateTime end,
            Integer successStatus,
            Integer failStatus
    ) {
        return orderHistoryRepository.fetchDeliveryPerformanceData(
                warehouseIds,
                start,
                end,
                successStatus,
                failStatus
        );
    }


    @Override
    public List<Object[]> fetchStatisticWarehouseData(
            Integer status,
            List<Long> warehouseIds,
            LocalDateTime start,
            LocalDateTime end
    ) {

        return orderHistoryRepository.fetchStatisticWarehouseData(
                status,
                warehouseIds,
                start,
                end
        );
    }
}
