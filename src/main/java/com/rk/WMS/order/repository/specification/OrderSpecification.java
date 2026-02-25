package com.rk.WMS.order.repository.specification;

import com.rk.WMS.order.criteria.SearchOrderCriteria;
import com.rk.WMS.order.model.Order;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpecification {
  public OrderSpecification() {}

  /**
   * build Specification for searching Order based on SearchOrderCriteria
   *
   * @param request
   * @return
   */
  public static Specification<Order> search(SearchOrderCriteria request) {

    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (request.getOrderCode() != null && !request.getOrderCode().isEmpty()) {
        predicates.add(cb.like(cb.lower(
            root.get("code")), "%" + request.getOrderCode().toLowerCase() + "%")
        );
      }

      if (request.getStatusCode() != null) {
        predicates.add(cb.equal(root.get("status"), request.getStatusCode()));
      }

      if (request.getReceiverPhone() != null && !request.getReceiverPhone().isEmpty()) {
        predicates.add(cb.like(cb.lower(
            root.get("receiverPhone")), "%" + request.getReceiverPhone().toLowerCase() + "%")
        );
      }

      if (request.getSupplierPhone() != null && !request.getSupplierPhone().isEmpty()) {
        predicates.add(cb.like(cb.lower(
            root.get("supplierPhone")), "%" + request.getSupplierPhone().toLowerCase() + "%")
        );
      }

      if (request.getWarehouseId() != null) {
        predicates.add(cb.equal(root.get("warehouseId"), request.getWarehouseId()));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

}
