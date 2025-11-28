package com.hmkeyewear.payment_service.controller;

import com.hmkeyewear.payment_service.dto.VNPayResponseDto;
import com.hmkeyewear.payment_service.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("payment")
public class PaymentController {
    private final PaymentService paymentService;
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/vn-pay")
    public ResponseEntity<?> pay(
            @RequestHeader("X-User-Id") String userId,
            HttpServletRequest request) {
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        VNPayResponseDto vnPayResponseDto = paymentService.createVnPayment(request);
        return new ResponseEntity<>(vnPayResponseDto, HttpStatus.OK);
    }

    @GetMapping("/vn-pay-callback")
    public ResponseEntity<?> payCallbackHandler(
            HttpServletRequest request) {
        return paymentService.handleVNPayCallback(request);
    }
}
