package com.hmkeyewear.order_service.dto;

import com.google.cloud.Timestamp;
import com.hmkeyewear.order_service.model.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {
    private String orderId;
    private String userId;
    private String email;
    private double summary;
    private String status;
    private double shipFee;
    private String discountId;
    private Timestamp createdAt;
    private String createdBy;
    private Timestamp updatedAt;
    private String updatedBy;
    private List<OrderDetail> details;
}
