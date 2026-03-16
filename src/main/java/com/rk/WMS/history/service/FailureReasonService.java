package com.rk.WMS.history.service;

import com.rk.WMS.history.model.FailureReason;
import java.util.List;
import org.springframework.stereotype.Service;


public interface FailureReasonService {

  List<FailureReason> getFailureReasons();
  public boolean isFailureReasonExist(Long id);

}
