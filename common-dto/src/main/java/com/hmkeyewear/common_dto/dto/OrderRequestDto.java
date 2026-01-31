package com.hmkeyewear.common_dto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDto {
    private String email;
    private String fullName;
    private String phone;

    // ===== ORDER INFO =====
    private String paymentMethod; // COD | BANK_TRANSFER | CASH
    private String note;

    // ===== ORDER DATA =====
    private List<OrderDetailRequestDto> details;
    private ShipInfoDto ship;
    private DiscountDto discount;
}
