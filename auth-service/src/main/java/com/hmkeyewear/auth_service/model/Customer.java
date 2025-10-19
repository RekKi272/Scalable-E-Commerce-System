package com.hmkeyewear.auth_service.model;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Customer {
    private String customerId;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private boolean sex;
    private String email;
    private String password;
    private Date birthday;
    private String storeId;

    private Timestamp createdAt;
    private String createdBy;

    private Timestamp updatedAt;
    private String updatedBy;

    private String role; // e.g. ROLE_USER
    private String status;
}
