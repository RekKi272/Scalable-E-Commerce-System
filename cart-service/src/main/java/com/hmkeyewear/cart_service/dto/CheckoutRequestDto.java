package com.hmkeyewear.cart_service.dto;

import com.hmkeyewear.common_dto.dto.DiscountDto;
import com.hmkeyewear.common_dto.dto.ShipInfoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutRequestDto {

    // lấy từ header X-User-Name, FE không được gửi
    private String userId;
    private String email;

    // FE gửi
    private String fullName;
    private String phone;
    private String paymentMethod;
    private String note;

    // FE gửi
    private ShipInfoDto ship;

    // FE chỉ gửi discountId
    private DiscountDto discount;
}
