package com.rk.WMS.order.dto.request;

import jakarta.validation.Valid;
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
  private Boolean success;

  private Long failureReasonId;

  private LocalDateTime confirmedAt;

  public Boolean isSuccess() {
    return success;
  }
}