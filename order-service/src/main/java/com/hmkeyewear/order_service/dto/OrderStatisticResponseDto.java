package com.hmkeyewear.order_service.dto;

import com.hmkeyewear.common_dto.dto.OrderResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class OrderStatisticResponseDto {

    private RevenueDto revenue;

    private int totalOrders;

    private Map<String, Long> orderStatusCount;

    private List<OrderResponseDto> orders;

    private List<NameValueDto> revenueChart;

    private List<OrderResponseDto> topOrders;

    private List<TopCustomerDto> topCustomers;

    private List<TopProductDto> topProducts;
}
