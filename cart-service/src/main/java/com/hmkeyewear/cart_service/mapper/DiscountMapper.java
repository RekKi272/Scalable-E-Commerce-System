package com.hmkeyewear.cart_service.mapper;

import com.hmkeyewear.cart_service.dto.DiscountRequestDto;
import com.hmkeyewear.cart_service.dto.DiscountResponseDto;
import com.hmkeyewear.cart_service.model.Discount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DiscountMapper {

    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    Discount toDiscount(DiscountRequestDto dto);

    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    DiscountResponseDto toDiscountResponseDto(Discount discount);
}
