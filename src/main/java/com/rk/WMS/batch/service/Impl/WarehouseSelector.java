package com.rk.WMS.batch.service.Impl;

import com.rk.WMS.order.model.Order;
import com.rk.WMS.warehouse.model.Warehouse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WarehouseSelector {

    private final DistanceCalculator distanceCalculator;

    /**
     * Chọn warehouse gần nhất còn slot trống cho một đơn hàng.
     *
     * @param order           đơn hàng cần dispatch
     * @param warehouses      danh sách warehouse khả dụng
     * @param remainingSlots  map theo dõi slot còn lại trong batch
     * @return warehouse phù hợp nhất hoặc null nếu không còn warehouse nào đủ slot
     */
    public Warehouse selectNearestWarehouseWithSlot(
            Order order,
            List<Warehouse> warehouses,
            Map<Long, Integer> remainingSlots
    ) {

        return warehouses.stream()
                // Chỉ xét warehouse còn slot trống
                .filter(w -> remainingSlots.getOrDefault(
                        w.getWarehouseId(), 0) > 0
                )
                // Chọn warehouse có khoảng cách gần nhất tới địa chỉ nhận hàng
                .min(Comparator.comparingDouble(
                        w -> distanceCalculator.distanceKm(
                                order.getReceiverLat(),
                                order.getReceiverLon(),
                                w.getLatitude(),
                                w.getLongitude()
                        )
                ))
                // Không tìm được warehouse
                .orElse(null);
    }
}

