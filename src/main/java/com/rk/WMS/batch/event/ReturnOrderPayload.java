package com.rk.WMS.batch.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ReturnOrderPayload {

    private String orderCode;
    private Long warehouseId;

    private String supplierName;
    private String supplierEmail;

    private String receiverName;
    private String receiverEmail;

    private Integer failedDeliveryCount;

    private String actor;
    private LocalDateTime eventTime;
}

