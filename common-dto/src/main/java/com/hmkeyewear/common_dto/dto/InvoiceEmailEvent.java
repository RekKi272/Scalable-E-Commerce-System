package com.hmkeyewear.common_dto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceEmailEvent {
    private String orderId;
    private String email;
    private Double totalAmount;
    private String invoiceUrl; // Link file
}
