package com.rk.WMS.batch.controller;

import com.rk.WMS.batch.event.OrderDispatchPublisher;
import com.rk.WMS.batch.service.DispatchService;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.common.response.ApiResponse;
import com.rk.WMS.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/batch/dispatch")
@RequiredArgsConstructor
public class ManualDispatchController {

    private final OrderRepository orderRepository;
    private final OrderDispatchPublisher eventPublisher;
    private final DispatchService dispatchService;

    @PostMapping("/manual")
    public ApiResponse<Void> manualDispatch(
            @RequestParam List<Integer> orderIds,
            @RequestParam Integer warehouseId
    ) {

        dispatchService.manualDispatch(orderIds, warehouseId);

        return ApiResponse.<Void>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .message("Dispatch thành công")
                .build();
    }
}

