package com.hmkeyewear.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

@Data
public class UserRequestDto {

    private String userId;
    private String firstName;
    private String lastName;
    private String gender;
    private Date birthday;
    private String phone;

    @Email
    @NotBlank
    private String email;

    private String addressProvince;
    private String addressWard;
    private String addressDetail;

    private String role;
    private String status = "ACTIVE";
    private String storeId;
}
