package com.hmkeyewear.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDto {
    private String customerId;
    private String email;
    private String role;
    private String token;
}
