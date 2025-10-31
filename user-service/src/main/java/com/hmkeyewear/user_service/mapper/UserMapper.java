package com.hmkeyewear.user_service.mapper;

import com.hmkeyewear.user_service.dto.UserRequestDto;
import com.hmkeyewear.user_service.dto.UserResponseDto;
import com.hmkeyewear.user_service.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // RequestDto -> Model
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "addressProvince", target = "addressProvince")
    @Mapping(source = "addressWard", target = "addressWard")
    @Mapping(source = "addressDetail", target = "addressDetail")
    @Mapping(source = "gender", target = "gender")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "birthday", target = "birthday")
    @Mapping(source = "role", target = "role")
    @Mapping(source = "status", target = "status")
    // ignore các field sẽ set bên Service
    @Mapping(target = "userId", ignore = true)
    @Mapping(source = "storeId", target = "storeId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    User toUser(UserRequestDto dto);

    // Model -> ResponseDto
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "gender", target = "gender")
    @Mapping(source = "birthday", target = "birthday")
    @Mapping(source = "addressProvince", target = "addressProvince")
    @Mapping(source = "addressWard", target = "addressWard")
    @Mapping(source = "addressDetail", target = "addressDetail")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "updatedBy", target = "updatedBy")
    @Mapping(source = "role", target = "role")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "storeId", target = "storeId")
    UserResponseDto toResponseDto(User user);
}
