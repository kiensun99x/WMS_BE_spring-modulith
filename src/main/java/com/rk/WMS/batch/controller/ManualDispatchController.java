package com.rk.WMS.batch.controller;

import com.rk.WMS.batch.service.DispatchService;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.common.response.ApiResponse;
import com.rk.WMS.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j(topic = "MANUAL-DISPATCH-CONTROLLER")
@RestController
@RequestMapping("/batch/dispatch")
@RequiredArgsConstructor
public class ManualDispatchController {

    private final OrderRepository orderRepository;
    private final DispatchService dispatchService;

    @PostMapping("/manual")
    public ApiResponse<Void> manualDispatch(
            @RequestParam List<Long> orderIds,
            @RequestParam Long warehouseId
    ) {

        log.info("[DISPATCH][API][REQUEST] Request manual dispatch");

        dispatchService.manualDispatch(orderIds, warehouseId);

        return ApiResponse.<Void>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .message("Dispatch thành công")
                .build();
    }
}

