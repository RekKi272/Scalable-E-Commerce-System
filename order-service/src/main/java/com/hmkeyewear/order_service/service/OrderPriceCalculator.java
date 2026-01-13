package com.hmkeyewear.order_service.service;

import com.hmkeyewear.order_service.model.DiscountDetail;
import com.hmkeyewear.order_service.model.OrderDetail;
import com.hmkeyewear.order_service.model.ShipInfo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderPriceCalculator {

    public double calculatePriceTemp(List<OrderDetail> details) {
        double total = 0;
        if (details == null)
            return 0;

        for (OrderDetail d : details) {
            total += d.getUnitPrice() * d.getQuantity();
        }
        return total;
    }

    public double calculatePriceDecreased(double priceTemp, List<DiscountDetail> discounts) {
        double decreased = 0;

        if (discounts == null)
            return 0;

        for (DiscountDetail d : discounts) {
            if ("percentage".equalsIgnoreCase(d.getValueType())) {
                decreased += priceTemp * d.getValueDiscount() / 100;
            } else if ("fixed".equalsIgnoreCase(d.getValueType())) {
                decreased += d.getValueDiscount();
            }
        }
        return decreased;
    }

    public double calculateShippingFee(List<ShipInfo> ships) {
        double fee = 0;
        if (ships == null)
            return 0;

        for (ShipInfo s : ships) {
            fee += s.getShippingFee();
        }
        return fee;
    }

    public double calculateSummary(double priceTemp, double priceDecreased, double shippingFee) {
        return priceTemp - priceDecreased + shippingFee;
    }
}