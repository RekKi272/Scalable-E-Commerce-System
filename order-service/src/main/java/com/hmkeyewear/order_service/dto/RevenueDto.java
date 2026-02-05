package com.hmkeyewear.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RevenueDto {
    private double expectedRevenue;
    private double actualRevenue;
    private double maxRevenue;
    private double minRevenue;
}
