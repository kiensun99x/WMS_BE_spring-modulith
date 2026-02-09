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
import com.rk.WMS.order.model.OrderSequence;
import com.rk.WMS.order.repository.OrderRepository;
import com.rk.WMS.order.repository.OrderSequenceRepository;
import com.rk.WMS.order.repository.specification.OrderSpecification;
import com.rk.WMS.order.service.OrderService;
import com.rk.WMS.warehouse.dto.WarehouseBrief;
import com.rk.WMS.warehouse.model.Warehouse;
import com.rk.WMS.warehouse.repository.WarehouseRepository;
import com.rk.WMS.warehouse.service.WarehouseService;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
  private final OrderSequenceRepository sequenceRepository;

  public Page<OrderResponse> getAllOrders(Pageable pageable) {
    //get order entity
    Page<Order> orders = orderRepository.findAll(pageable);

    Map<Integer, WarehouseBrief> warehouseMap = getWarehouseMap(orders);

    // map order entity + warehouse name -> dto
    Page<OrderResponse> dtoPage = orders.map(
        (order) -> orderMapper.toResponseDto(order, warehouseMap)
    );

//    // enrich
//    dtoPage.forEach(dto -> {
//      WarehouseBriefDTO wh = warehouseMap.get(dto.getWarehouseId());
//      if (wh != null) {
//        dto.setWarehouseCode(wh.getCode());
//        dto.setWarehouseName(wh.getName());
//        dto.setWarehouseId(null);
//      }
//      dto.setWarehouseId(null);
//    });

    return dtoPage;
  }

  @Override
  public Page<OrderResponse> getSearchOrders(SearchOrderRequest request, Pageable pageable) {
    SearchOrderCriteria criteria = mapToCriteria(request);
    //dynamic search: criteria builder
    Specification<Order> specification = OrderSpecification.search(criteria);
    Page<Order> orders = orderRepository.findAll(specification, pageable);
    if (orders.isEmpty()) {
      throw new AppException(ErrorCode.ORDER_NOT_FOUND);
    }
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
    String code = generateOrderCode();

    //map request -> entity
    Order createdOrder = orderMapper.toEntity(order);
    createdOrder.setCode(code);
    createdOrder.setStatus(OrderStatus.NEW);

    createdOrder = orderRepository.saveAndFlush(createdOrder);
    return orderMapper.toResponseDto(createdOrder);
  }

  private Map<Integer, WarehouseBrief> getWarehouseMap(Page<Order> orders) {
    // lấy danh sách warehouseId
    Set<Integer> warehouseIds = orders.stream()
        .map(Order::getWarehouseId)
        .collect(Collectors.toSet());
    //trả về map
    return warehouseService.getByIds(warehouseIds);

  }

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

  private String generateOrderCode() {
    LocalDate today = LocalDate.now();

    // 1. Tìm hoặc tạo mới sequence cho ngày hôm nay
    OrderSequence sequence = sequenceRepository.findBySequenceDateWithLock(today)
        .orElseGet(() -> {
          OrderSequence newSeq = new OrderSequence();
          newSeq.setSequenceDate(today);
          newSeq.setCurrentValue(0L);
          return sequenceRepository.saveAndFlush(newSeq);
        });

    // 2. Tăng giá trị hiện tại lên 1
    Long nextValue = sequence.getCurrentValue() + 1;
    sequence.setCurrentValue(nextValue);
    sequenceRepository.save(sequence);

    // 3. Định dạng chuỗi: DH + yyMMdd + XXXXX
    String datePart = today.format(DateTimeFormatter.ofPattern("yyMMdd"));
    String sequencePart = String.format("%05d", nextValue);

    return "DH-" + datePart + "-" + sequencePart;
  }
}