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

    public double calculatePriceDecreased(double priceTemp, DiscountDetail discount) {
        if (discount == null)
            return 0;

        double decreased = 0;

        if ("percentage".equalsIgnoreCase(discount.getValueType())) {
            decreased = priceTemp * discount.getValueDiscount() / 100;
        } else if ("fixed".equalsIgnoreCase(discount.getValueType())) {
            decreased = discount.getValueDiscount();
        }

        return decreased;
    }

    public double calculateShippingFee(ShipInfo ship) {
        if (ship == null)
            return 0;
        return ship.getShippingFee();
    }

    public double calculateSummary(
            double priceTemp,
            double priceDecreased,
            double shippingFee) {

        double total = priceTemp - priceDecreased + shippingFee;
        return total < 0 ? 0 : total;
    }
}
