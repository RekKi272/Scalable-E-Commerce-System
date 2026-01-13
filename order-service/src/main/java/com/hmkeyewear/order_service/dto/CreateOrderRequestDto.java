package com.hmkeyewear.order_service.dto;

import com.hmkeyewear.order_service.model.DiscountDetail;
import com.hmkeyewear.order_service.model.OrderDetail;
import com.hmkeyewear.order_service.model.ShipInfo;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequestDto {

    private String userId;
    private String email;
    private String fullname;

    private String paymentMethod; // CASH, COD, BANK_TRANSFER

    private List<DiscountDetail> discount;
    private List<OrderDetail> details;
    private List<ShipInfo> ship;

    private String note;
}
