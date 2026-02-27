package com.rk.WMS.order.service;

import static com.rk.WMS.common.constants.DateTimePattern.ORDER_CODE_DATE;

import com.rk.WMS.order.model.OrderSequence;
import com.rk.WMS.order.repository.OrderSequenceRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrderCodeService {
  private final OrderSequenceRepository sequenceRepository;

  private static final String ORDER_CODE_PREFIX = "DH";

  /**
   * sinh mã đơn hàng
   * @param today: ngày tạo đơn
   * @param todaySequence: số thứ tự của đơn hàng trong ngày
   * @return
   */
  public String toOrderCode(LocalDate today, Long todaySequence) {
    //Định dạng chuỗi: DH + yyMMdd + XXXXX
    String datePart = today.format(DateTimeFormatter.ofPattern(ORDER_CODE_DATE));
    String sequencePart = String.format("%05d", todaySequence);

    return ORDER_CODE_PREFIX + "-" + datePart + "-" + sequencePart;
  }

  /**
   * Lấy mã thứ tự đơn hàng theo ngày tự động tăng.
   * @param today
   * @return
   */
  public Long generateTodaySequence(LocalDate today){
    // 1. Tìm hoặc tạo mới sequence cho ngày hôm nay
    OrderSequence sequence = sequenceRepository.findBySequenceDate(today)
        .orElseGet(() -> {
          OrderSequence newSeq = new OrderSequence();
          newSeq.setSequenceDate(today);
          newSeq.setCurrentValue(0L);
          return sequenceRepository.saveAndFlush(newSeq);
        });

    // 2. Tăng giá trị hiện tại lên 1
    Long nextValue = sequence.getCurrentValue() + 1;
    sequence.setCurrentValue(nextValue);
    sequenceRepository.save(sequence);

    return nextValue;
  }

  /**
   * lưu số thứ tự của mã đơn trong ngày
   * @param today
   * @param value
   */
  public void saveSequence(LocalDate today, Long value) {
    OrderSequence sequence = sequenceRepository.findBySequenceDate(today).orElseThrow();
    sequence.setCurrentValue(value);
    sequenceRepository.save(sequence);
  }
}
