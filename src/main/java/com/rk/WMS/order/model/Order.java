package com.rk.WMS.order.model;


import com.rk.WMS.common.constants.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;
    @Column(name = "order_code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    //supplier
    @Column(name = "supplier_name", nullable = false,length = 100)
    private String supplierName;

    @Column(name = "supplier_address", nullable = false,length = 200)
    private String supplierAddress;

    @Column(name = "supplier_phone", nullable = false,length = 20)
    private String supplierPhone;

    @Column(name = "supplier_email", nullable = false,length = 100)
    private String supplierEmail;

    //receiver
    @Column(name = "receiver_name", nullable = false,length = 100)
    private String receiverName;

    @Column(name = "receiver_address", nullable = false,length = 200)
    private String receiverAddress;

    @Column(name = "receiver_phone", nullable = false,length = 20)
    private String receiverPhone;

    @Column(name = "receiver_email", nullable = false,length = 100)
    private String receiverEmail;

    @Column(name = "receiver_lat", nullable = false)
    private BigDecimal receiverLat;
    @Column(name = "receiver_lon", nullable = false)
    private BigDecimal receiverLon;

    //common
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "stored_at")
    private LocalDateTime storedAt;

    @Column(name = "dispatch_at")
    private LocalDateTime dispatchAt;

    @Column(name = "delivery_at")
    private LocalDateTime deliveryAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Column(name = "failed_delivery_count")
    private Integer failedDeliveryCount = 0;
}
