package com.rk.WMS.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class OrderImportResponse {
  private Long errorFileId;
  private Integer totalErrorRows;
  private Integer totalValidRows;
}
