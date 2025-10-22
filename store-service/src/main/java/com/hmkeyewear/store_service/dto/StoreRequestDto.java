package com.hmkeyewear.store_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreRequestDto {
    @NotBlank(message = "Store name is required")
    private String storeName;
    private String address;
    private String ward;
    private String province;
    private String status;
}
