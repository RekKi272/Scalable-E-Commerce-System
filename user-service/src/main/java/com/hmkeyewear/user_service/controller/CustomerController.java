package com.hmkeyewear.user_service.controller;

import com.hmkeyewear.user_service.dto.CustomerRequestDto;
import com.hmkeyewear.user_service.dto.CustomerResponseDto;
import com.hmkeyewear.user_service.model.Customer;
import com.hmkeyewear.user_service.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @PostMapping("/create")
    public ResponseEntity<CustomerResponseDto> createCustomer(
            @RequestBody CustomerRequestDto dto
    ) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(customerService.createCustomer(dto));
    }

    @GetMapping("/get/{customerId}")
    public ResponseEntity<CustomerResponseDto> getCustomer(@PathVariable String customerId) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(customerService.getCustomer(customerId));
    }

    @PutMapping("/update/{customerId}")
    public ResponseEntity<CustomerResponseDto> updateCustomer(
            @PathVariable String customerId,
            @RequestParam String updatedBy,
            @RequestBody CustomerRequestDto dto
    ) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(customerService.updateCustomer(customerId, updatedBy, dto));
    }

    @DeleteMapping("/delete/{customerId}")
    public ResponseEntity<String> deleteCustomer(@PathVariable String customerId) throws ExecutionException, InterruptedException {
        customerService.deleteCustomer(customerId);
        return ResponseEntity.ok("Deleted customer with ID: " + customerId);
    }
}
