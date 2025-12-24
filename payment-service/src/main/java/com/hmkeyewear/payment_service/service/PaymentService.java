package com.hmkeyewear.payment_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmkeyewear.payment_service.config.VNPayConfig;
import com.hmkeyewear.common_dto.dto.PaymentRequestDto;
import com.hmkeyewear.common_dto.dto.VNPayResponseDto;
import com.hmkeyewear.common_dto.dto.OrderPaymentStatusUpdateDto;
import com.hmkeyewear.payment_service.messaging.OrderSaveRequestProducer;
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
    private final ObjectMapper objectMapper;
    private final OrderSaveRequestProducer orderSaveRequestProducer;
    private final OrderStatusUpdateProducer orderStatusUpdateProducer;

    public VNPayResponseDto createVnPayment(HttpServletRequest request) {
        long amount = Integer.parseInt(request.getParameter("amount")) * 100L;
        String bankCode = request.getParameter("bankCode");
        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount));
        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParamsMap.put("vnp_BankCode", bankCode);
        }
        vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));
        //build query url
        String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getVnp_SecretKey(), hashData);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;

        return VNPayResponseDto.builder()
                .code("ok")
                .message("success")
                .paymentUrl(paymentUrl).build();
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
        String encodedOrderInfo = request.getParameter("vnp_OrderInfo");

        // Không thành công
        if (!"00".equals(responseCode)) {
            OrderPaymentStatusUpdateDto statusUpdateDto = new OrderPaymentStatusUpdateDto(request.getParameter("vnp_TxnRef"), "FAILED");
            orderStatusUpdateProducer.sendUpdateStatusRequest(statusUpdateDto);
            return ResponseEntity.ok("Payment failed");
        }

        try {
            OrderPaymentStatusUpdateDto statusUpdateDto = new OrderPaymentStatusUpdateDto(request.getParameter("vnp_TxnRef"), "DELIVERING");
            orderStatusUpdateProducer.sendUpdateStatusRequest(statusUpdateDto);
            return ResponseEntity.ok("Payment success");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Decode cart failed");
        }
    }

}
