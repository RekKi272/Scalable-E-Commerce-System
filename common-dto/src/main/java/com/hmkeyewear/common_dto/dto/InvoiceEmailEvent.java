package com.hmkeyewear.common_dto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceEmailEvent {

    private String orderId;
    private String userId;
    private String createdBy;
    private String email;
    private String fullName;
    private String phone;

    private String paymentMethod;
    private String status;

    private Double priceTemp;
    private Double priceDecreased;
    private Double summary;

    private List<OrderItemDto> details;
    private ShipInfoDto ship;
    private DiscountDto discount;

    private String note;
    private Instant updatedAt;
}
