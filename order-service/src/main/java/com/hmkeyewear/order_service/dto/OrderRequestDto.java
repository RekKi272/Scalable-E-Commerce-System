package com.hmkeyewear.order_service.dto;

import com.hmkeyewear.order_service.model.OrderDetail;
import com.hmkeyewear.order_service.model.DiscountDetail;
import com.hmkeyewear.order_service.model.ShipInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDto {

    private String userId;
    private String email;
    private String fullname;
    private String phone;

    private String paymentMethod; // CASH, COD, BANK_TRANSFER

    // object đơn
    private DiscountDetail discount;
    private ShipInfo ship;

    // danh sách sản phẩm
    private List<OrderDetail> details;

    private String note;

    private String status;

    // audit
    private String createdBy;
}
