package com.rk.WMS.order.service.impl;

import com.rk.WMS.common.constants.ActorType;
import com.rk.WMS.common.constants.OrderStatus;
import com.rk.WMS.common.event.DomainEventPublisher;
import com.rk.WMS.common.exception.AppException;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.order.criteria.SearchOrderCriteria;
import com.rk.WMS.order.dto.request.CreateOrderRequest;
import com.rk.WMS.order.dto.request.SearchOrderRequest;
import com.rk.WMS.order.dto.response.OrderResponse;
import com.rk.WMS.order.event.ListOrderStatusChangedEvent;
import com.rk.WMS.order.event.OrderStatusChangedEvent;
import com.rk.WMS.order.mapper.OrderMapper;
import com.rk.WMS.order.model.Order;
import com.rk.WMS.order.repository.OrderRepository;
import com.rk.WMS.order.repository.specification.OrderSpecification;
import com.rk.WMS.order.service.OrderCodeService;
import com.rk.WMS.order.service.OrderService;
import com.rk.WMS.warehouse.dto.WarehouseBrief;
import com.rk.WMS.warehouse.model.Warehouse;
import com.rk.WMS.warehouse.repository.WarehouseRepository;
import com.rk.WMS.warehouse.service.WarehouseService;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {
  private final OrderRepository orderRepository;
  private final OrderMapper orderMapper;
  private final WarehouseService warehouseService;
  private final WarehouseRepository warehouseRepository;
  private final OrderCodeService orderCodeService;
  private final DomainEventPublisher domainEventPublisher;

  /**
   * Lấy tất cả đơn hàng theo page
   * +) Build criteria(nếu có)
   * +) Lấy ra tất cả đơn hàng
   * +) Lặp qua tất cả để lấy những id warehouse
   * +) Lấy ra thông tin warehouse theo id
   * +) Map ra dto rồi trả về
   *
   * @param pageable: số thứ tự của trang và số lượng bản ghi mỗi trang
   * @param request: request search
   * @return danh sách OrderDTO theo page
   */
  @Override
  public Page<OrderResponse> getOrders(SearchOrderRequest request, Pageable pageable) {
    SearchOrderCriteria criteria = mapToCriteria(request);
    //criteria builder for dynamic query
    Specification<Order> specification = OrderSpecification.search(criteria);
    //get order entity
    Page<Order> orders = orderRepository.findAll(specification, pageable);
    if (orders.isEmpty()) {
      return Page.empty();
    }
    //get warehouse map
    Map<Long, WarehouseBrief> warehouseMap = getWarehouseMap(orders);

    // map order entity + warehouse name -> dto
    Page<OrderResponse> dtoPage = orders.map(
        (order) -> orderMapper.toResponseDto(order, warehouseMap)
    );
    return dtoPage;
  }

  /**
   * tạo 1 đơn hàng thủ công
   *
   * 1) lấy sequence mã đơn, sinh mã đơn hàng
   * 2) map request -> entity
   * 3) lưu đơn hàng vào db, tăng và lưu sequence
   * 4) publish event
   *
   * @param order
   * @return
   */
  @Override
  @Transactional
  public OrderResponse createOrder(CreateOrderRequest order) {
    //sinh mã đơn hàng
    LocalDate today = LocalDate.now();
    Long todaySequence = orderCodeService.generateTodaySequence(today);
    String code = orderCodeService.toOrderCode(today, todaySequence);

    //map request -> entity
    Order createdOrder = orderMapper.toEntity(order);
    createdOrder.setCode(code);
    createdOrder.setStatus(OrderStatus.NEW);

    createdOrder = orderRepository.saveAndFlush(createdOrder);

    //publish event
    domainEventPublisher.publishEvent(
        OrderStatusChangedEvent.builder()
            .orderId(createdOrder.getId())
            .fromStatus(null)
            .toStatus(OrderStatus.NEW)
            .occurredAt(LocalDateTime.now())
            .actorType(ActorType.USER)
            .userId(1L) //gán userID
            .build()
    );

    return orderMapper.toResponseDto(createdOrder);
  }

  /**
   * tạo nhiều đơn hàng
   *
   * 1) lấy ra sequence mã đơn và thời gian hiện tại
   * 2) lặp qua từng dòng dữ liệu:
   *    - sinh mã đơn
   *    - map request -> entity
   *    - gán thêm thông tin cho Order
   *    - tăng sequence
   * 3) lưu vào db danh sách đơn hàng, lưu sequence
   * 4) bulk và publish event
   *
   * @param createOrderRequestList: danh sách requestDTO đọc từ file excel
   * @return
   */
  @Override
  @Transactional
  public int createOrders(List<CreateOrderRequest> createOrderRequestList) {
    List<Order> orderList = new ArrayList<>();
    //nếu có data
    if (!createOrderRequestList.isEmpty()) {
      //thời gian hiện tại
      LocalDate today = LocalDate.now();
      Long todaySequence = orderCodeService.generateTodaySequence(today);
      for (CreateOrderRequest req : createOrderRequestList) {
        //sinh mã đơn
        String code = orderCodeService.toOrderCode(today, todaySequence);
        //gán thêm thông tin cho Order
        Order order = orderMapper.toEntity(req);
        order.setStatus(OrderStatus.NEW);
        order.setCode(code);

        todaySequence++;
        orderList.add(order);
      }
      //save and flush để db generate id ngay
      orderList = orderRepository.saveAllAndFlush(orderList);
      orderCodeService.saveSequence(today, --todaySequence); //trừ đi 1 vì khi lấy mã nó đã +1 sẵn

      //publish event
      ListOrderStatusChangedEvent event = new ListOrderStatusChangedEvent();
      for (Order order : orderList) {
        event.add(
            OrderStatusChangedEvent.builder()
                .orderId(order.getId())
                .fromStatus(null)
                .toStatus(OrderStatus.NEW)
                .occurredAt(LocalDateTime.now())
                .actorType(ActorType.USER)
                .userId(1L) //gán userID
                .build()
        );
      }
      domainEventPublisher.publishEvent(event);

      return orderList.size();
    }
    throw new AppException(ErrorCode.ORDER_IMPORT_HAS_ERRORS);
  }

  /**
   * lấy thông tin đơn hàng theo id
   *
   * vì Order chỉ chứa warehouseID nên cần lấy thêm tên và mã kho rồi enrich vào responseDTO
   * @param id: id của đơn hàng
   * @return OrderResponseDTO
   */
  @Override
  public OrderResponse getOrderById(Long id) {
    Order order = orderRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
    OrderResponse response = orderMapper.toResponseDto(order);
    //enrich
    if (order.getWarehouseId() != null) {
      Warehouse w = warehouseService.getById(order.getWarehouseId());
      response.setWarehouseCode(w.getWarehouseCode());
      response.setWarehouseName(w.getName());
    }
    return response;
  }

  /**
   * Xử lý việc thay đổi trạng thái của đơn hàng khi phân phối đơn hàng cho kho
   * @param orderWarehouseMap: Map<orderId, warehouseId>
   * @param storedAt: thời điểm phân phối
   */
  @Override
  public void handleDispatch(Map<Long, Long> orderWarehouseMap, LocalDateTime storedAt) {
    //lấy ra danh sách đơn hàng được phân phối
    List<Order> orders = orderRepository.findAllById(orderWarehouseMap.keySet());

    //lặp qua từng đơn hàng và set trạng thái, kho
    for (Order order : orders) {
      Long warehouseId = orderWarehouseMap.get(order.getId());
      if (warehouseId == null) {
        throw new AppException(ErrorCode.FAILED);
      }

      order.setWarehouseId(warehouseId);
      order.setStatus(OrderStatus.STORED);
      order.setStoredAt(storedAt);
    }
    orderRepository.saveAll(orders);
  }

  /**
   * lấy thông tin warehouse của các đơn hàng trong page
   * @param orders: danh sách đơn hàng trong page
   * @return
   */
  private Map<Long, WarehouseBrief> getWarehouseMap(Page<Order> orders) {
    // lấy danh sách warehouseId
    Set<Long> warehouseIds = new HashSet<>();
    for (Order order : orders){
      if (order.getWarehouseId() != null) {
        warehouseIds.add(order.getWarehouseId());
      }
    }
    //trả về map
    return warehouseService.getByIds(warehouseIds);

  }

  /**
   * map request -> criteria
   *
   * vì requestDTO chứa warehouseCode mà truy vấn thì cần warehouseID nên cần map từ code -> id để tìm kiếm
   * @param request
   * @return
   */
  private SearchOrderCriteria mapToCriteria(SearchOrderRequest request) {
    SearchOrderCriteria criteria = new SearchOrderCriteria();

    criteria.setOrderCode(request.getOrderCode());
    criteria.setSupplierPhone(request.getSupplierPhone());
    criteria.setReceiverPhone(request.getReceiverPhone());
    criteria.setStatusCode(request.getStatusCode());

    if (request.getWarehouseCode() != null && !request.getWarehouseCode().isEmpty()) {
      Warehouse warehouse = warehouseRepository
          .findByWarehouseCode(request.getWarehouseCode())
          .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
      criteria.setWarehouseId(warehouse.getWarehouseId());
    }

    return criteria;
  }


}