package com.hmkeyewear.product_service.mapper;

import com.hmkeyewear.product_service.dto.AttributeRequestDto;
import com.hmkeyewear.product_service.dto.AttributeResponseDto;
import com.hmkeyewear.product_service.model.Attribute;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttributeMapper {

    @Mapping(target = "attributeId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Attribute toEntity(AttributeRequestDto dto);

    AttributeResponseDto toResponseDto(Attribute entity);
}
