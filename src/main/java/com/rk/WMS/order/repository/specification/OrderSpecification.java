package com.rk.WMS.order.repository.specification;

import com.rk.WMS.order.criteria.SearchOrderCriteria;
import com.rk.WMS.order.dto.request.SearchOrderRequestDTO;
import com.rk.WMS.order.model.Order;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpecification {
  public OrderSpecification() {}
  public static Specification<Order> search(SearchOrderCriteria request) {

    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (request.getOrderCode() != null) {
        predicates.add(cb.equal(root.get("code"), request.getOrderCode()));
      }

      if (request.getStatusCode() != null) {
        predicates.add(cb.equal(root.get("status"), request.getStatusCode()));
      }

      if (request.getReceiverPhone() != null) {
        predicates.add(cb.equal(root.get("receiverPhone"), request.getReceiverPhone()));
      }

      if (request.getSupplierPhone() != null) {
        predicates.add(cb.equal(root.get("supplierPhone"), request.getSupplierPhone()));
      }

      if (request.getWarehouseId() != null) {
        predicates.add(cb.equal(root.get("warehouseId"), request.getWarehouseId()));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

}
