package com.rk.WMS.batch.event;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReturnOrderEvent {

    private String orderCode;
    private Integer warehouseId;

    private String supplierName;
    private String supplierEmail;

    private String receiverName;
    private String receiverEmail;

    private Integer failedDeliveryCount;

    private String actor;
    private LocalDateTime eventTime;
}


