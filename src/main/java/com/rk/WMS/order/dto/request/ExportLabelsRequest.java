package com.rk.WMS.order.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportLabelsRequest {

  @NotEmpty
  @Size(max = 10, message = "Tối đa 10 đơn hàng mỗi lần xuất nhãn")
  private List<Integer> orderIds;
}