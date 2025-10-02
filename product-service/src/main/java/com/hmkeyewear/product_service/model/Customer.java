package com.hmkeyewear.product_service.model;

import lombok.Getter;
import lombok.Setter;

import com.google.cloud.Timestamp;
import java.util.Date;

@Getter
@Setter
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

    private Timestamp createdAt;
    private String createdBy;

    private Timestamp updatedAt;
    private String updatedBy;
}

