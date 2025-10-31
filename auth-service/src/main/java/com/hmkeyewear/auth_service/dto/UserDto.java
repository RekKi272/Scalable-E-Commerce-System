package com.hmkeyewear.auth_service.dto;

import lombok.Data;

@Data
public class UserDto {
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
}
