package com.hmkeyewear.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class RevenueChartWithOrdersResponseDto {
    private Map<String, Double> revenue;
    private List<OrderResponseDto> orders;
}
