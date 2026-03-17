package com.rk.WMS.history.repository;

import com.rk.WMS.history.model.OrderHistory;
import java.util.List;
import com.rk.WMS.report.projection.DeliveryPerformanceProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {
  List<OrderHistory> findByOrderIdOrderByCreatedAtDesc(Long orderId);

  @Query(value = """
          SELECT oh.warehouse_id,
                 DATE(oh.created_at),
                 COUNT(*)
          FROM history_db.order_history oh
          WHERE oh.to_status = :status
            AND oh.warehouse_id IN (:warehouseIds)
            AND oh.created_at BETWEEN :start AND :end
          GROUP BY oh.warehouse_id, DATE(oh.created_at)
          """, nativeQuery = true)
  List<Object[]> fetchStatisticWarehouseData(
          @Param("status") Integer status,
          @Param("warehouseIds") List<Long> warehouseIds,
          @Param("start") LocalDateTime start,
          @Param("end") LocalDateTime end
  );

  @Query(value = """
        SELECT 
            h.warehouse_id as warehouseId,
            h.created_at as createdAt,
            h.to_status as status,
            h.failure_reason_id as failureReasonId,
            COUNT(h.order_history_id) as total
        FROM history_db.order_history h
        WHERE h.warehouse_id IN (:warehouseIds)
        AND h.to_status IN (:successStatus, :failStatus)
        AND h.created_at BETWEEN :start AND :end
        GROUP BY h.warehouse_id, h.created_at, h.to_status, h.failure_reason_id
        """, nativeQuery = true)
  List<DeliveryPerformanceProjection> fetchDeliveryPerformanceData(
          @Param("warehouseIds") List<Long> warehouseIds,
          @Param("start") LocalDateTime start,
          @Param("end") LocalDateTime end,
          @Param("successStatus") Integer successStatus,
          @Param("failStatus") Integer failStatus
  );

}
