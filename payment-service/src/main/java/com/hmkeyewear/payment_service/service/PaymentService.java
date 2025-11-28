package com.hmkeyewear.payment_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmkeyewear.payment_service.config.VNPayConfig;
import com.hmkeyewear.payment_service.dto.PaymentRequestDto;
import com.hmkeyewear.payment_service.dto.VNPayResponseDto;
import com.hmkeyewear.payment_service.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Map;

@Service
public class PaymentService {
    private final VNPayConfig vnPayConfig;
    private final ObjectMapper objectMapper;

    public PaymentService(VNPayConfig vnPayConfig, ObjectMapper objectMapper) {
        this.vnPayConfig = vnPayConfig;
        this.objectMapper = objectMapper;
    }

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

            // ======================
            // THÊM GIỎ HÀNG → VNP_ORDERINFO
            // ======================
            String cartJson = objectMapper.writeValueAsString(requestDto);
            String encodedCart = Base64.getUrlEncoder().encodeToString(cartJson.getBytes());
            vnpParamsMap.put("vnp_OrderInfo", encodedCart);

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
            return ResponseEntity.ok("Payment failed");
        }

        try {
            // giải mã giỏ hàng
            String cartJson = new String(Base64.getUrlDecoder().decode(encodedOrderInfo));
            PaymentRequestDto originalCart =
                    objectMapper.readValue(cartJson, PaymentRequestDto.class);

            // TODO: gửi originalCart sang order-service tại đây
            // orderEventProducer.sendOrderCreateEvent(originalCart);

            return ResponseEntity.ok("Payment success");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Decode cart failed");
        }
    }

}
