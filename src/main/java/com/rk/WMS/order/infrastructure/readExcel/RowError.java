package com.rk.WMS.order.infrastructure.readExcel;

import com.rk.WMS.order.dto.request.CreateOrderRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RowError {
  private int rowNumber;
  private String errorMessage;
  private CreateOrderRequest req;

}
