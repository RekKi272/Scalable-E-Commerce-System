package com.hmkeyewear.order_service.controller;

import com.hmkeyewear.order_service.dto.OrderRequestDto;
import com.hmkeyewear.order_service.dto.OrderResponseDto;
import com.hmkeyewear.order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody OrderRequestDto orderRequestDto) throws ExecutionException, InterruptedException {
        OrderResponseDto orderResponseDto = orderService.createOrder(orderRequestDto);
        return ResponseEntity.ok(orderResponseDto);
    }

    @GetMapping("/get")
    public ResponseEntity<OrderResponseDto> getOrder(@RequestParam String orderId) throws ExecutionException, InterruptedException {
        OrderResponseDto orderResponseDto = orderService.getOrder(orderId);
        return ResponseEntity.ok(orderResponseDto);
    }

    @PutMapping("/update/{orderId}")
    public ResponseEntity<OrderResponseDto> updateOrder(@PathVariable String orderId, @RequestBody OrderRequestDto orderRequestDto) throws ExecutionException, InterruptedException {
        OrderResponseDto orderResponseDto = orderService.updateOrder(orderId, orderRequestDto);
        return ResponseEntity.ok(orderResponseDto);
    }

    @DeleteMapping("/delete")
    public String deleteOrder(@RequestParam String orderId) throws ExecutionException, InterruptedException {
        return  orderService.deleteOrder(orderId);
    }
}
