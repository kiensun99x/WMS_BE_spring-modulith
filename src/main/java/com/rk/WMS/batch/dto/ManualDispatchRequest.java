package com.rk.WMS.batch.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ManualDispatchRequest {

    @NotEmpty(message = "Danh sách orderIds không được để trống")
    private List<Long> orderIds;

    @NotNull(message = "WarehouseId không được để trống")
    private Long warehouseId;
}