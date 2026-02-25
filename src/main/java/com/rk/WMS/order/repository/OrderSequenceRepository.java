package com.rk.WMS.order.repository;

import com.rk.WMS.order.model.OrderSequence;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface OrderSequenceRepository extends JpaRepository<OrderSequence, Long> {

  /**
   * Tìm OrderSequence theo ngày với khóa tránh xung đột
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT s FROM OrderSequence s WHERE s.sequenceDate = :date")
  Optional<OrderSequence> findBySequenceDateWithLock(LocalDate date);

}
