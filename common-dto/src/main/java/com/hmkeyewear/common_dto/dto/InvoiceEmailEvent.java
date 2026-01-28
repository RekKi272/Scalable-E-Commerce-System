package com.hmkeyewear.common_dto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceEmailEvent {

    // ---- BASIC INFO ----
    private String orderId;
    private String userId;
    private String email;
    private String fullname;
    private String phone;

    private String paymentMethod;
    private String status;

    // ---- PRICE ----
    private Double priceTemp;
    private Double priceDecreased;
    private Double summary;

    // ---- DETAIL ----
    private List<OrderDetail> details;
    private ShipInfo ship;
    private DiscountDetail discount;

    private String note;

    /*
     * =========================
     * INNER DTO CLASSES
     * =========================
     */

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDetail {
        private String productId;
        private String variantId;
        private String productName;
        private int quantity;
        private double unitPrice;
        private double totalPrice;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShipInfo {
        private String addressProvince;
        private String addressWard;
        private String addressDetail;
        private double shippingFee;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscountDetail {
        private String discountId;
        private String valueType; // percentage | fixed
        private Long valueDiscount;
    }
}
