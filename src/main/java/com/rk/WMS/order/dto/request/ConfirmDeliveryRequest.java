package com.rk.WMS.order.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmDeliveryRequest {

  @NotNull
  private Boolean isSuccess;

  private Long failureReasonId;

  private LocalDateTime confirmedAt;
}