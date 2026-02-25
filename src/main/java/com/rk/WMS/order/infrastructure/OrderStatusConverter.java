package com.rk.WMS.order.infrastructure;

import com.rk.WMS.common.constants.OrderStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter between OrderStatus enum and its database representation (integer code).
 */
@Converter(autoApply = true)
public class OrderStatusConverter
    implements AttributeConverter<OrderStatus, Integer> {

  @Override
  public Integer convertToDatabaseColumn(OrderStatus status) {
    if (status == null) return null;
    return status.getCode();
  }

  @Override
  public OrderStatus convertToEntityAttribute(Integer dbValue) {
    if (dbValue == null) return null;
    return OrderStatus.fromCode(dbValue);
  }
}

