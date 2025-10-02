package com.hmkeyewear.user_service.dto;

import java.util.Date;
import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponseDto {
    private String customerId;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private boolean sex;
    private String email;
    private Date birthday;

    private Timestamp createdAt;
    private String createdBy;
    private Timestamp updatedAt;
    private String updatedBy;

    private String role;
    private String status;
}
