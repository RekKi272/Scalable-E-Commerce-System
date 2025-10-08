package com.hmkeyewear.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequestDto {
    @NotBlank
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private boolean sex;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    private String role; // optional, default ROLE_USER
}
