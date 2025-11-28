package com.hmkeyewear.cart_service.mapper;

import com.hmkeyewear.cart_service.dto.DiscountRequestDto;
import com.hmkeyewear.cart_service.dto.DiscountResponseDto;
import com.hmkeyewear.cart_service.model.Discount;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DiscountMapper {
    DiscountResponseDto toDiscountResponseDto(Discount discount);

    Discount toDiscount(DiscountRequestDto discountRequestDto);
}
