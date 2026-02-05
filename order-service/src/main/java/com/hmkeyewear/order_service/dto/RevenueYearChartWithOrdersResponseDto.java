package com.hmkeyewear.order_service.dto;

import com.hmkeyewear.common_dto.dto.OrderResponseDto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class RevenueYearChartWithOrdersResponseDto {
    private Map<String, Double> revenueByMonth;
    private List<OrderResponseDto> orders;
}
