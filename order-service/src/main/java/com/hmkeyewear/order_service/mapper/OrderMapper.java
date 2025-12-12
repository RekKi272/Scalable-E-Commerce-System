package com.hmkeyewear.order_service.mapper;

import com.hmkeyewear.order_service.dto.OrderDetailRequestDto;
import com.hmkeyewear.order_service.dto.OrderRequestDto;
import com.hmkeyewear.order_service.dto.OrderResponseDto;
import com.hmkeyewear.order_service.dto.OrderSaveRequestDto;
import com.hmkeyewear.order_service.model.Order;
import com.hmkeyewear.order_service.model.OrderDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    // RequestDto -> Order
    Order toOrder(OrderRequestDto dto);

    // Order -> ResponseDto
    OrderResponseDto toOrderResponseDto(Order order);

    // OrderSaveRequestDto -> OrderRequestDto
    @Mapping(source = "items", target = "details")
    OrderRequestDto toOrderRequestDto(OrderSaveRequestDto orderSaveRequestDto);

    // OrderDetails -> OrderDetailRequestDto
    OrderDetailRequestDto toOrderDetailRequestDto(OrderDetail orderDetail);
}
