package com.hmkeyewear.order_service.mapper;

import com.hmkeyewear.common_dto.dto.CartItemDto;
import com.hmkeyewear.common_dto.dto.OrderDetailRequestDto;
import com.hmkeyewear.common_dto.dto.OrderRequestDto;
import com.hmkeyewear.common_dto.dto.OrderResponseDto;
import com.hmkeyewear.order_service.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    // ===== Request -> Model =====
    Order toOrder(OrderRequestDto dto);

    // ===== Model -> Response =====
    @Mapping(source = "details", target = "items")
    OrderResponseDto toOrderResponseDto(Order order);

    // ===== Detail -> CartItem =====
    CartItemDto toCartItemDto(OrderDetailRequestDto detail);
}
