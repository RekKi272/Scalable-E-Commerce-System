package com.hmkeyewear.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RevenueStatisticResponseDto {
    private double totalRevenue;
    private long totalOrders;
}
