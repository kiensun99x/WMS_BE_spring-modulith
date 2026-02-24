package com.rk.WMS.order.infrastructure.readExcel;

import com.rk.WMS.order.dto.request.CreateOrderRequest;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReadResult {
  private List<CreateOrderRequest> valid;
  private List<RowError> errors;
}
