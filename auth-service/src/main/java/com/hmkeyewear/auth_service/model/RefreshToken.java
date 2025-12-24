package com.hmkeyewear.auth_service.model;

import com.google.cloud.Timestamp;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshToken {

    private String id;

    private String userId;

    private String tokenHash; // Hashed

    private Timestamp createAt;

    private Timestamp expiresAt;

    private boolean revoked = false;
}
