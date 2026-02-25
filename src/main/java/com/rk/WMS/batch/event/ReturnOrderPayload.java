package com.rk.WMS.batch.event;

import com.rk.WMS.common.constants.ActorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ReturnOrderPayload {

    private Long orderId;

    private String orderCode;
    private Long warehouseId;

    private String supplierName;
    private String supplierEmail;

    private String receiverName;
    private String receiverEmail;

    private Integer failedDeliveryCount;

    private ActorType actor;
    private LocalDateTime eventTime;
}

