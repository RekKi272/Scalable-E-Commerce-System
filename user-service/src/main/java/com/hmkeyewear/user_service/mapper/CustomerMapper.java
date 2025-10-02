package com.hmkeyewear.user_service.mapper;

import com.hmkeyewear.user_service.dto.CustomerRequestDto;
import com.hmkeyewear.user_service.dto.CustomerResponseDto;
import com.hmkeyewear.user_service.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    // RequestDto -> Model
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "sex", target = "sex")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "password", target = "password")
    @Mapping(source = "birthday", target = "birthday")
    @Mapping(source = "role", target = "role")
    @Mapping(source = "status", target = "status")

    // ignore các field sẽ set bên Service
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Customer toCustomer(CustomerRequestDto dto);

    // Model -> ResponseDto
    @Mapping(source = "customerId", target = "customerId")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "sex", target = "sex")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "birthday", target = "birthday")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "updatedBy", target = "updatedBy")
    @Mapping(source = "role", target = "role")
    @Mapping(source = "status", target = "status")
    CustomerResponseDto toResponseDto(Customer customer);
}

