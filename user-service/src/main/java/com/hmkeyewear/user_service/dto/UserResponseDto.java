package com.hmkeyewear.user_service.dto;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private String userId;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String gender;
    private Date birthday;
    private String addressProvince;
    private String addressWard;
    private String addressDetail;

    private Timestamp createdAt;
    private String createdBy;
    private Timestamp updatedAt;
    private String updatedBy;

    private String role;
    private String status;
    private String storeId;
}
