package com.hmkeyewear.order_service.service;

import com.hmkeyewear.order_service.constant.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusResolver {

    public String resolveInitStatus(PaymentMethod method) {
        switch (method) {
            case CASH:
                return "COMPLETED";
            case COD:
                return "PENDING";
            case BANK_TRANSFER:
                return "PENDING";
            default:
                throw new RuntimeException("Phương thức thanh toán không được hỗ trợ");
        }
    }
}
