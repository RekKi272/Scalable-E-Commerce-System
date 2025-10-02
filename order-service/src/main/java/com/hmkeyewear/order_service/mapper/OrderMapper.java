package com.hmkeyewear.order_service.mapper;

import com.google.cloud.Timestamp;
import com.hmkeyewear.order_service.dto.OrderRequestDto;
import com.hmkeyewear.order_service.dto.OrderResponseDto;
import com.hmkeyewear.order_service.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface OrderMapper {

    // RequestDto -> Order
    Order toOrder(OrderRequestDto dto);

    // Order -> ResponseDto
    OrderResponseDto toOrderResponseDto(Order order);
}
