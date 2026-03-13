package com.rk.WMS.history.service.impl;

import com.rk.WMS.history.model.FailureReason;
import com.rk.WMS.history.repository.FailureReasonRepository;
import com.rk.WMS.history.service.FailureReasonService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FailureReasonServiceImpl implements FailureReasonService {
  private final FailureReasonRepository failureReasonRepository;

  @Override
  public List<FailureReason> getFailureReasons(){
    return failureReasonRepository.findAll();
  }

  @Override
  public boolean isFailureReasonExist(Long id) {
    return failureReasonRepository.existsById(id);
  }
}
