package com.hmkeyewear.order_service.dto;

import com.hmkeyewear.order_service.model.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDto {
    private String userId;
    private double summary;
    private String status;
    private double shipFee;
    private String discountId;
    private List<OrderDetail> details;
}
