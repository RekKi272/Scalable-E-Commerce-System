package com.hmkeyewear.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterCustomerRequestDto {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String phone;

    @Email
    private String email;

    private String addressProvince;
    private String addressWard;
    private String addressDetail;

    private String status = "ACTIVE";

    @NotBlank
    private String password;
}
