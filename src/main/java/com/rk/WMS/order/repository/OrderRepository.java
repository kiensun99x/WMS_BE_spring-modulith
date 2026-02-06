package com.rk.WMS.order.repository;

import com.rk.WMS.common.constants.OrderStatus;
import com.rk.WMS.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Query("""
        SELECT o FROM Order o
        WHERE o.status = :status
        ORDER BY o.createdAt ASC
    """)
    List<Order> findTop100ByStatusOrderByCreatedAtAsc(
            @Param("status") OrderStatus status,
            Pageable pageable
    );
}
