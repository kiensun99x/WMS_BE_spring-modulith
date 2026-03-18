package com.rk.WMS.order.service;

import com.rk.WMS.common.constants.OrderStatus;
import com.rk.WMS.order.model.Order;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

public interface OrderQueryService {

    List<Order> findFailedOrdersForReturn(
            OrderStatus status,
            Long failedCount,
            Pageable pageable
    );

    List<Order> findTop100ByStatusOrderByCreatedAtAsc(
            OrderStatus status,
            Pageable pageable
    );

    List<Order> findAllByIds(Collection<Long> ids);

}
