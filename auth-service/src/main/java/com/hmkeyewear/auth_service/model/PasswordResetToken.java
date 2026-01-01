package com.hmkeyewear.auth_service.model;

import com.google.cloud.Timestamp;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
public class PasswordResetToken {
    private String id;
    private String tokenHash;
    private String userId;
    private Timestamp expiryTime;
    private Boolean used = false;
    private Timestamp createdAt;
}
