package com.rk.WMS.order.service.impl;

import com.rk.WMS.common.constants.OrderStatus;
import com.rk.WMS.common.exception.AppException;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.order.criteria.SearchOrderCriteria;
import com.rk.WMS.order.dto.request.CreateOrderRequest;
import com.rk.WMS.order.dto.request.SearchOrderRequest;
import com.rk.WMS.order.dto.response.OrderResponse;
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
import java.util.HashSet;
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

  public Page<OrderResponse> getAllOrders(Pageable pageable) {
    //get order entity
    Page<Order> orders = orderRepository.findAll(pageable);

    Map<Integer, WarehouseBrief> warehouseMap = getWarehouseMap(orders);
    System.out.println(warehouseMap.toString());
    // map order entity + warehouse name -> dto
    Page<OrderResponse> dtoPage = orders.map(
        (order) -> orderMapper.toResponseDto(order, warehouseMap)
    );
    return dtoPage;
  }

  @Override
  public Page<OrderResponse> getSearchOrders(SearchOrderRequest request, Pageable pageable) {
    SearchOrderCriteria criteria = mapToCriteria(request);
    //criteria builder for dynamic query
    Specification<Order> specification = OrderSpecification.search(criteria);
    //get order entity
    Page<Order> orders = orderRepository.findAll(specification, pageable);
    if (orders.isEmpty()) {
      throw new AppException(ErrorCode.ORDER_NOT_FOUND);
    }
    //get warehouse map
    Map<Integer, WarehouseBrief> warehouseMap = getWarehouseMap(orders);

    // map order entity + warehouse name -> dto
    Page<OrderResponse> dtoPage = orders.map(
        (order) -> orderMapper.toResponseDto(order, warehouseMap)
    );
    return dtoPage;
  }

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
    return orderMapper.toResponseDto(createdOrder);
  }

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
   * lấy thông tin warehouse của các đơn hàng trong page
   * @param orders: danh sách đơn hàng trong page
   * @return
   */
  private Map<Integer, WarehouseBrief> getWarehouseMap(Page<Order> orders) {
    // lấy danh sách warehouseId
    Set<Integer> warehouseIds = new HashSet<>();
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
   * @param request
   * @return
   */
  private SearchOrderCriteria mapToCriteria(SearchOrderRequest request) {
    SearchOrderCriteria criteria = new SearchOrderCriteria();

    criteria.setOrderCode(request.getOrderCode());
    criteria.setSupplierPhone(request.getSupplierPhone());
    criteria.setReceiverPhone(request.getReceiverPhone());
    criteria.setStatusCode(request.getStatusCode());

    if (request.getWarehouseCode() != null) {
      Warehouse warehouse = warehouseRepository
          .findByWarehouseCode(request.getWarehouseCode())
          .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR));
      criteria.setWarehouseId(warehouse.getWarehouseId());
    }

    return criteria;
  }


}