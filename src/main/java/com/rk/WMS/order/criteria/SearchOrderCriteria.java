package com.rk.WMS.order.criteria;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchOrderCriteria {
  String orderCode;
  String supplierPhone;
  String receiverPhone;
  Integer statusCode;
  Integer warehouseId;
}
