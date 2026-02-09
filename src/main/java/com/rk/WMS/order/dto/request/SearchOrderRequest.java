package com.rk.WMS.order.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SearchOrderRequest {
  private String orderCode;
  private String supplierPhone;
  private String receiverPhone;
  private Integer statusCode;
  private String warehouseCode;
}
