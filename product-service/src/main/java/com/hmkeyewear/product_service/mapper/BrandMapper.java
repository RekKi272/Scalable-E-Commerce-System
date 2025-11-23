package com.hmkeyewear.product_service.mapper;

import com.hmkeyewear.product_service.dto.BrandRequestDto;
import com.hmkeyewear.product_service.dto.BrandResponseDto;
import com.hmkeyewear.product_service.model.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BrandMapper {

    @Mapping(target = "brandId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Brand toBrand(BrandRequestDto dto);

    BrandResponseDto toBrandResponseDto(Brand brand);
}
