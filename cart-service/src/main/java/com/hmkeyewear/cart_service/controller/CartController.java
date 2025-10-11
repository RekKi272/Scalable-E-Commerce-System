package com.hmkeyewear.cart_service.controller;

import com.hmkeyewear.cart_service.dto.CartRequestDto;
import com.hmkeyewear.cart_service.dto.CartResponseDto;
import com.hmkeyewear.cart_service.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/create")
    public ResponseEntity<CartResponseDto> createCart(@RequestBody CartRequestDto dto) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(cartService.createCart(dto));
    }

    @GetMapping("/get")
    public ResponseEntity<CartResponseDto> getCart(@RequestParam String customerId) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(cartService.getCart(customerId));
    }

    @PutMapping("/update/{customerId}")
    public ResponseEntity<CartResponseDto> updateCart(@PathVariable String customerId, @RequestBody CartRequestDto dto) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(cartService.updateCart(customerId, dto));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteCart(@RequestParam String customerId) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(cartService.deleteCart(customerId));
    }
}
