package com.hmkeyewear.product_service.feign;

import com.hmkeyewear.product_service.model.Customer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.concurrent.ExecutionException;

@FeignClient("USER-SERVICE")
public interface ProductInterface {
    @GetMapping("/user/get")
    public Customer getCustomer(@RequestParam String customerId) throws InterruptedException, ExecutionException;
}
