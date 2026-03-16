package com.rk.WMS.order.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "error_file", catalog = "order_db")
public class ErrorFileImport {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "error_file_id")
  private Long errorFileId;

  @Column(name = "path")
  private String path;

  @Column(name = "created_at")
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Column(name = "user_id")
  private Long createdBy;

}
