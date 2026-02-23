package com.rk.WMS.history.repository;

import com.rk.WMS.common.constants.OrderStatus;
import com.rk.WMS.history.model.OrderHistory;
import com.rk.WMS.report.projection.DeliveryPerformanceProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

    @Query(value = """
    SELECT o.warehouse_id,
           DATE(oh.created_at),
           COUNT(*)
    FROM order_history oh
    JOIN orders o ON o.order_id = oh.order_id
    WHERE oh.to_status = :status
      AND o.warehouse_id IN (:warehouseIds)
      AND oh.created_at BETWEEN :start AND :end
    GROUP BY o.warehouse_id, DATE(oh.created_at)
""", nativeQuery = true)
    List<Object[]> fetchStatisticWarehouseData(
            @Param("status") Integer status,
            @Param("warehouseIds") List<Long> warehouseIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query(value = """
    SELECT 
        o.warehouse_id as warehouseId,
        h.created_at as createdAt,
        h.to_status as status,
        h.failure_reason_id as failureReasonId,
        COUNT(h.order_history_id) as total
    FROM order_history h
    JOIN orders o ON o.order_id = h.order_id
    WHERE o.warehouse_id IN (:warehouseIds)
    AND h.to_status IN (:successStatus, :failStatus)
    AND h.created_at BETWEEN :start AND :end
    GROUP BY o.warehouse_id, h.created_at, h.to_status, h.failure_reason_id
""", nativeQuery = true)
    List<DeliveryPerformanceProjection> fetchDeliveryPerformanceData(
            @Param("warehouseIds") List<Long> warehouseIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("successStatus") Integer successStatus,
            @Param("failStatus") Integer failStatus
    );

}
