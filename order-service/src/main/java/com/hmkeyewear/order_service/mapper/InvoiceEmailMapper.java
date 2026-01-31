package com.hmkeyewear.order_service.mapper;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.google.cloud.Timestamp;
import com.hmkeyewear.common_dto.dto.InvoiceEmailEvent;
import com.hmkeyewear.common_dto.dto.OrderItemDto;
import com.hmkeyewear.order_service.model.Order;

@Component
public class InvoiceEmailMapper {

    public InvoiceEmailEvent toEvent(Order order) {
        InvoiceEmailEvent e = new InvoiceEmailEvent();

        e.setOrderId(order.getOrderId());
        e.setUserId(order.getUserId());
        e.setCreatedBy(order.getCreatedBy());

        e.setEmail(order.getEmail());
        e.setFullName(order.getFullName());
        e.setPhone(order.getPhone());

        e.setPaymentMethod(order.getPaymentMethod());
        e.setStatus(order.getStatus());

        e.setPriceTemp(order.getPriceTemp());
        e.setPriceDecreased(order.getPriceDecreased());
        e.setSummary(order.getSummary());

        e.setDetails(
                order.getDetails().stream()
                        .map(d -> {
                            OrderItemDto item = new OrderItemDto();
                            item.setProductId(d.getProductId());
                            item.setVariantId(d.getVariantId());
                            item.setProductName(d.getProductName());
                            item.setUnitPrice(d.getUnitPrice());
                            item.setQuantity(d.getQuantity());
                            item.setTotalPrice(d.getUnitPrice() * d.getQuantity());
                            return item;
                        })
                        .collect(Collectors.toList()));

        e.setShip(order.getShip());
        e.setDiscount(order.getDiscount());
        e.setNote(order.getNote());

        if (order.getUpdatedAt() != null) {
            e.setUpdatedAt(convertTimestamp(order.getUpdatedAt()));
        }

        return e;
    }

    private Instant convertTimestamp(Timestamp ts) {
        return ts.toDate().toInstant();
    }
}
