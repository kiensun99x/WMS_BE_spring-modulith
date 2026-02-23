package com.rk.WMS.report.projection;

import java.time.LocalDateTime;

public interface DeliveryPerformanceProjection {

    Long getWarehouseId();

    LocalDateTime getCreatedAt();

    Integer getStatus();

    Long getFailureReasonId();

    Long getTotal();
}
