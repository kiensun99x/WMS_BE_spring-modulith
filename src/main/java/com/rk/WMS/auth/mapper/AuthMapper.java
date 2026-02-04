package com.rk.WMS.auth.mapper;

import com.rk.WMS.auth.dto.response.LoginResponse;
import com.rk.WMS.auth.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "warehouseId", source = "warehouse.warehouseId")
    LoginResponse toLoginResponse(User user);
}
