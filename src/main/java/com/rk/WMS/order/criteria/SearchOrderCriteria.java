package com.rk.WMS.order.criteria;

import lombok.Getter;
import lombok.Setter;

/**
 * chuyển đổi từ searchDTO chứa warehouse_code sang criteria chứa warehouse_id để truy vấn đơn hàng
 */
@Getter
@Setter
public class SearchOrderCriteria {
  String orderCode;
  String supplierPhone;
  String receiverPhone;
  Integer statusCode;
  Integer warehouseId;
}
