package com.hmkeyewear.payment_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmkeyewear.payment_service.config.VNPayConfig;
import com.hmkeyewear.common_dto.dto.PaymentRequestDto;
import com.hmkeyewear.common_dto.dto.VNPayResponseDto;
import com.hmkeyewear.common_dto.dto.OrderPaymentStatusUpdateDto;
import com.hmkeyewear.common_dto.dto.OrderStatusEventDto;
import com.hmkeyewear.payment_service.messaging.OrderStatusUpdateProducer;
import com.hmkeyewear.payment_service.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@AllArgsConstructor
public class PaymentService {
    private final VNPayConfig vnPayConfig;
    private final OrderStatusUpdateProducer orderStatusUpdateProducer;

    public VNPayResponseDto createVnPayment(HttpServletRequest request) {

        String orderId = request.getParameter("orderId");
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId is required");
        }

        long amount = Long.parseLong(request.getParameter("amount")) * 100L;
        String bankCode = request.getParameter("bankCode");

        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();

        // Case: Retry giao dịch
        String txnRef = orderId + "_" + System.currentTimeMillis();

        vnpParamsMap.put("vnp_TxnRef", txnRef);
        vnpParamsMap.put("vnp_OrderInfo", "Thanh toan don hang: " + orderId);
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount));
        vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));

        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParamsMap.put("vnp_BankCode", bankCode);
        }

        String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
        String vnpSecureHash = VNPayUtil.hmacSHA512(
                vnPayConfig.getVnp_SecretKey(), hashData);

        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;

        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;

        return VNPayResponseDto.builder()
                .code("ok")
                .message("success")
                .paymentUrl(paymentUrl)
                .build();
    }

    // Thanh toán từ cart-service (có giỏ hàng)
    public VNPayResponseDto createPaymentForCartService(PaymentRequestDto requestDto) {
        try {
            long amountVND = (long) (requestDto.getTotal() * 100L);

            Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();
            vnpParamsMap.put("vnp_Amount", String.valueOf(amountVND));

            vnpParamsMap.put("vnp_TxnRef", requestDto.getOrderId());
            vnpParamsMap.put("vnp_OrderInfo", "Thanh toan don hang: " + requestDto.getOrderId());

            if (requestDto.getBankCode() != null && !requestDto.getBankCode().isEmpty()) {
                vnpParamsMap.put("vnp_BankCode", requestDto.getBankCode());
            }

            vnpParamsMap.put("vnp_IpAddr", requestDto.getIpAddress());

            // build URL
            String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
            String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
            String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getVnp_SecretKey(), hashData);

            queryUrl += "&vnp_SecureHash=" + vnpSecureHash;

            String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;

            return VNPayResponseDto.builder()
                    .code("ok")
                    .message("success")
                    .paymentUrl(paymentUrl)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to encode cart data", e);
        }
    }

    public ResponseEntity<?> handleVNPayCallback(HttpServletRequest request) {
        String responseCode = request.getParameter("vnp_ResponseCode");

        if ("00".equals(responseCode)) {
            return ResponseEntity.ok(
                    "Thanh toán thành công. Đơn hàng đang được xác nhận.");
        }

        return ResponseEntity.ok("Thanh toán thất bại hoặc bị hủy.");
    }

    public ResponseEntity<?> handleVNPayIpn(HttpServletRequest request) {
        Map<String, String> params = VNPayUtil.getParamsMap(request);

        // Verify chữ ký
        boolean isValidSignature = VNPayUtil.verifySignature(
                params,
                vnPayConfig.getVnp_SecretKey());

        if (!isValidSignature) {
            return ResponseEntity.ok(
                    "{\"RspCode\":\"97\",\"Message\":\"Invalid signature\"}");
        }

        String responseCode = params.get("vnp_ResponseCode");

        // Tách timestamp để lấy giao dịch
        String txnRef = params.get("vnp_TxnRef");
        String orderId = txnRef.split("_")[0];

        // SEND to order-service to update order status
        if ("00".equals(responseCode)) {
            orderStatusUpdateProducer.sendUpdateStatusRequest(
                    new OrderStatusEventDto(orderId, "PAID"));
        } else {
            orderStatusUpdateProducer.sendUpdateStatusRequest(
                    new OrderStatusEventDto(orderId, "FAILED"));
        }

        // Trả kết quả cho VNPay
        return ResponseEntity.ok(
                "{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}");
    }
}
