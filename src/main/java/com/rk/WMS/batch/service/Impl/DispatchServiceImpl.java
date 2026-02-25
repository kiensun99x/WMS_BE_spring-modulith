package com.rk.WMS.batch.service.Impl;

import com.rk.WMS.common.event.DomainEventPublisher;
import com.rk.WMS.batch.event.OrdersAutoDispatchedEvent;
import com.rk.WMS.batch.event.OrdersManuallyDispatchedEvent;
import com.rk.WMS.common.constants.OrderStatus;
import com.rk.WMS.common.exception.AppException;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.order.model.Order;
import com.rk.WMS.order.repository.OrderRepository;
import com.rk.WMS.warehouse.model.Warehouse;
import com.rk.WMS.warehouse.repository.WarehouseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rk.WMS.batch.service.DispatchService;


@Slf4j(topic = "DISPATCH-SERVICE")
@Service
@RequiredArgsConstructor
public class DispatchServiceImpl implements DispatchService {

    private final OrderRepository orderRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseSelector warehouseSelector;
    private final DomainEventPublisher domainEventPublisher;

    /**
     * Thực hiện dispatch thủ công danh sách đơn hàng vào một warehouse cụ thể.
     *
     * @param orderIds     danh sách ID các đơn hàng cần dispatch
     * @param warehouseId ID kho được chọn để dispatch
     *
     * @throws AppException
     *         - FAILED: khi không tìm thấy warehouse
     *         - VALUE_EXCEED_LIMIT: khi số đơn vượt quá số slot còn trống của warehouse
     */
    @Transactional
    @Override
    public void manualDispatch(List<Long> orderIds, Long warehouseId) {

        log.info("[MANUAL_DISPATCH] orderIds={}, warehouseId={}", orderIds, warehouseId);

        // 1. Kiểm tra warehouse có tồn tại hay không
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new AppException(ErrorCode.FAILED));

        // 2. Validate số lượng slot trống của warehouse
        //    Không cho phép dispatch nhiều đơn hơn khả năng xử lý hiện tại của kho
        if (warehouse.getAvailableSlots() < orderIds.size()) {
            throw new AppException(ErrorCode.VALUE_EXCEED_LIMIT);
        }

        // 3. Load orders
        List<Order> orders = orderRepository.findAllById(orderIds);

        if (orders.size() != orderIds.size()) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 4. Validate order status = NEW
        List<Order> invalidOrders = orders.stream()
                .filter(order -> order.getStatus() != OrderStatus.NEW)
                .toList();

        if (!invalidOrders.isEmpty()) {

            log.warn(
                    "[MANUAL_DISPATCH][INVALID_STATUS] orders={}",
                    invalidOrders.stream()
                            .map(o -> o.getCode() + ":" + o.getStatus())
                            .toList()
            );

            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }

        // 5. Publish Event
        domainEventPublisher.publishEvent(
                new OrdersManuallyDispatchedEvent(
                        orderIds,
                        warehouseId,
                        LocalDateTime.now()
                )
        );

        log.info("[MANUAL_DISPATCH][EVENT_PUBLISHED] orderCount={}", orderIds.size());
    }



    /**
     * Tự động dispatch đơn hàng mới (NEW) vào các warehouse phù hợp.
     *
     * Quy trình:
     *   +, Lấy danh sách đơn hàng NEW theo thứ tự FIFO (createdAt tăng dần)
     *   +, Lấy danh sách warehouse còn khả năng xử lý
     *   +, Với mỗi đơn hàng, chọn warehouse gần nhất còn slot trống
     *   +, Dừng khi không còn warehouse nào đủ slot
     *   +, Phát domain event để xử lý dispatch ở các listener
     *
     */
    @Transactional
    @Override
    public void autoDispatch() {

        log.info("[AUTO_DISPATCH] Start auto dispatch");

        // 1. Lấy danh sách đơn hàng mới (NEW)
        List<Order> orders = orderRepository
                .findTop100ByStatusOrderByCreatedAtAsc(
                        OrderStatus.NEW,
                        PageRequest.of(0, 100)
                );

        if (orders.isEmpty()) {
            log.info("[AUTO_DISPATCH] No orders to dispatch");
            return;
        }

        // 2. Lấy danh sách warehouse còn khả năng tiếp nhận đơn
        List<Warehouse> warehouses = warehouseRepository.findAvailableWarehouses();

        if (warehouses.isEmpty()) {
            log.warn("[AUTO_DISPATCH][FAILED] No available warehouses");
            throw new AppException(ErrorCode.FAILED);
        }


        // 3. Map theo dõi số slot còn lại của mỗi warehouse TRONG BỘ NHỚ
        //    - remainingSlots KHÔNG lưu vào database
        //    - (Tránh việc assign vượt quá khả năng xử lý của kho)
        //    - Dùng để mô phỏng việc giảm slot của warehouse trong 1 batch
        //    - DB sẽ được cập nhật thật ở listener sau khi event được publish
        Map<Long, Integer> remainingSlots = new HashMap<>();
        for (Warehouse w : warehouses) {
            remainingSlots.put(w.getWarehouseId(), w.getAvailableSlots());
        }

        // 4. Map kết quả dispatch: orderId -> warehouseId
        Map<Long, Long> orderWarehouseMap = new HashMap<>();



        // 5. Lần lượt xử lý từng đơn hàng
        for (Order order : orders) {

            // Chỉ chọn warehouse còn slot trống theo remainingSlots (RAM),
            Warehouse selectedWarehouse =
                    warehouseSelector.selectNearestWarehouseWithSlot(
                            order,
                            warehouses,
                            remainingSlots
                    );

            // Nếu không còn warehouse nào đủ slot → dừng batch
            if (selectedWarehouse == null) {
                log.warn(
                        "[AUTO_DISPATCH][STOP] No warehouse slot for orderId={}",
                        order.getId()
                );
                break;
            }

            // Ghi nhận kết quả dispatch
            orderWarehouseMap.put(
                    order.getId(),
                    selectedWarehouse.getWarehouseId()
            );


            // Giảm slot còn lại TRONG BỘ NHỚ cho warehouse vừa được assign
            // Cập nhật slot thật trong DB sẽ được xử lý ở listener của Warehouse Module
            remainingSlots.computeIfPresent(
                    selectedWarehouse.getWarehouseId(),
                    (k, v) -> v - 1
            );
        }

        if (orderWarehouseMap.isEmpty()) {
            log.info("[AUTO_DISPATCH] No orders dispatched due to slot limits");
            return;
        }

        // 6. Publish event
        domainEventPublisher.publishEvent(
                new OrdersAutoDispatchedEvent(
                        orderWarehouseMap,
                        LocalDateTime.now()
                )
        );

        log.info(
                "[AUTO_DISPATCH][EVENT_PUBLISHED] orderCount={}",
                orderWarehouseMap.size()
        );
    }
}