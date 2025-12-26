package com.hmkeyewear.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class RevenueYearChartResponseDto {
    // key: 01 -> 12
    private Map<String, Double> revenueByMonth;
}
