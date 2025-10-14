package com.hmkeyewear.cart_service.mapper;

import com.hmkeyewear.cart_service.dto.CartRequestDto;
import com.hmkeyewear.cart_service.dto.CartResponseDto;
import com.hmkeyewear.cart_service.model.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {

    // RequestDto -> Cart
    @Mapping(source = "customerId", target = "customerId")
    @Mapping(source = "items", target = "items")
    Cart toCart(CartRequestDto dto);

    // Cart -> ResponseDto
    @Mapping(source = "customerId", target = "customerId")
    @Mapping(source = "items", target = "items")
    @Mapping(source = "total", target = "total")
    CartResponseDto toResponseDto(Cart cart);
}
