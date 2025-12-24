package com.hmkeyewear.common_dto.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class OrderSaveRequestDto {
    private String userId;
    private String email;
    private double summary;
    private List<CartItemDto> items; // GIỮ nguyên từ cart-service
    private String discountId;
}