package com.rk.WMS.order.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Setter
@Getter
@Table(name = "order_sequences")
public class OrderSequence {
  @Id
  @Column(name = "sequence_date", nullable = false)
  private LocalDate sequenceDate; // Ngày tạo đơn

  @Column(nullable = false)
  private Long currentValue; // Số thứ tự hiện tại

  @Column(name = "created_at", nullable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  @UpdateTimestamp
  private LocalDateTime updatedAt;
}
