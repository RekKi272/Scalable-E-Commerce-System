package com.hmkeyewear.common_dto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusEventDto {
    private String orderId;
    private String status; // PAID, COMPLETED, FAILD, CANCEL
}
