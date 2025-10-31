package com.hmkeyewear.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDto {
    private String userId;
    private String email;
    private String role;
    private String token;
    private String firstName;
    private String lastName;
}
