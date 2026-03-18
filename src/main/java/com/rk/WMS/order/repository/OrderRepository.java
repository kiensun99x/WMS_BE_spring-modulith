package com.rk.WMS.order.repository;

import com.rk.WMS.common.constants.OrderStatus;
import com.rk.WMS.order.model.Order;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>,
    JpaSpecificationExecutor<Order> {
    @Query("""
        SELECT o FROM Order o
        WHERE o.status = :status
        ORDER BY o.createdAt ASC
    """)
    List<Order> findTop100ByStatusOrderByCreatedAtAsc(
            @Param("status") OrderStatus status,
            Pageable pageable
    );

    @Query("""
        SELECT o FROM Order o
        WHERE o.status = :status
          AND o.failedDeliveryCount >= :failedCount
        ORDER BY o.deliveryAt ASC
    """)
    List<Order> findFailedOrdersForReturn(
            @Param("status") OrderStatus status,
            @Param("failedCount") Long failedCount,
            Pageable pageable
    );

    List<Order> findByCodeInOrderByCodeAsc(Collection<String> codes);
    List<Order> findByIdInOrderByIdAsc(List<Integer> ids);
}
