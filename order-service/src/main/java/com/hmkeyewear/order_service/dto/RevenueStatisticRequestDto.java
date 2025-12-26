package com.hmkeyewear.order_service.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RevenueStatisticRequestDto {
    private LocalDate fromDate;
    private LocalDate toDate;
}
