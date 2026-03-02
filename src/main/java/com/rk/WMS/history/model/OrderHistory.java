package com.rk.WMS.history.model;

import com.rk.WMS.auth.model.User;
import com.rk.WMS.common.constants.ActorType;
import com.rk.WMS.common.constants.OrderStatus;
import com.rk.WMS.common.converter.OrderStatusConverter;
import com.rk.WMS.order.model.Order;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_history")
public class OrderHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_history_id")
    private Long orderHistoryId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "failure_reason_id")
    private Long failureReasonId;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false)
    private ActorType actorType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Convert(converter = OrderStatusConverter.class)
    @Column(name = "from_status")
    private OrderStatus fromStatus;

    @Convert(converter = OrderStatusConverter.class)
    @Column(name = "to_status", nullable = false)
    private OrderStatus toStatus;
}