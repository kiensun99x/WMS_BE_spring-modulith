package com.rk.WMS.order.service.impl;

import com.rk.WMS.common.constants.OrderStatus;
import com.rk.WMS.order.model.Order;
import com.rk.WMS.order.repository.OrderRepository;
import com.rk.WMS.order.service.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;

    @Override
    public List<Order> findFailedOrdersForReturn(
            OrderStatus status,
            Long failedCount,
            Pageable pageable
    ) {
        return orderRepository.findFailedOrdersForReturn(
                status,
                failedCount,
                pageable
        );
    }

    @Override
    public List<Order> findTop100ByStatusOrderByCreatedAtAsc(
            OrderStatus status,
            Pageable pageable
    ) {
        return orderRepository
                .findTop100ByStatusOrderByCreatedAtAsc(status, pageable);
    }

    @Override
    public List<Order> findAllByIds(Collection<Long> ids) {
        return orderRepository.findAllById(ids);
    }
}