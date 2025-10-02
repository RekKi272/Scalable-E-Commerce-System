package com.hmkeyewear.product_service.mapper;

import com.hmkeyewear.product_service.dto.BrandRequestDto;
import com.hmkeyewear.product_service.dto.BrandResponseDto;
import com.hmkeyewear.product_service.model.Brand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    Brand toBrand(BrandRequestDto dto);
    BrandResponseDto toBrandResponseDto(Brand brand);
}
