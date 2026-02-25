package com.rk.WMS.warehouse.mapper;

import com.rk.WMS.warehouse.dto.WarehouseBrief;
import com.rk.WMS.warehouse.model.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {
  @Mapping(target = "id", source = "warehouse.warehouseId")
  @Mapping(target = "code", source = "warehouse.warehouseCode")
  @Mapping(target = "name", source = "warehouse.name")
  WarehouseBrief toWarehouseBriefDTO(Warehouse warehouse);
}
