package com.rk.WMS.batch.controller;

import com.rk.WMS.batch.dto.ManualDispatchRequest;
import com.rk.WMS.batch.service.DispatchService;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.common.response.ApiResponse;
import com.rk.WMS.order.repository.OrderRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@Slf4j(topic = "MANUAL-DISPATCH-CONTROLLER")
@RestController
@RequestMapping("/batch/dispatch")
@RequiredArgsConstructor
public class ManualDispatchController {

    private final OrderRepository orderRepository;
    private final DispatchService dispatchService;

    @PostMapping("/manual")
    public ApiResponse<Void> manualDispatch(
            @Valid @RequestBody ManualDispatchRequest request
    ) {

        log.info("[DISPATCH][API][REQUEST] orderIds={}, warehouseId={}", request.getOrderIds(), request.getWarehouseId());

        dispatchService.manualDispatch(request.getOrderIds(), request.getWarehouseId());

        return ApiResponse.<Void>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .message("Dispatch thành công")
                .build();
    }
}

