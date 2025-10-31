package com.hmkeyewear.auth_service.dto;

import java.util.Date;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterStaffRequestDto {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String gender;
    private Date birthday;

    @NotBlank
    private String phone;

    @Email
    @NotBlank
    private String email;

    private String addressProvince;
    private String addressWard;
    private String addressDetail;

    @NotBlank
    private String storeId;

    @NotBlank
    private String role;

    private String status = "ACTIVE";
}
