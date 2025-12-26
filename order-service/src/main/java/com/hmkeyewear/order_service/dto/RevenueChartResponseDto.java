package com.hmkeyewear.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class RevenueChartResponseDto {
    private Map<String, Double> revenueByDate;
}
