package com.hmkeyewear.user_service.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.IgnoreExtraProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@IgnoreExtraProperties
public class User {
    private String userId;

    private String firstName;
    private String lastName;

    private String gender;
    private Date birthday;

    private String phone;
    private String email;
    private String addressProvince;
    private String addressWard;
    private String addressDetail;

    private String role;
    private String status;
    private String storeId;

    private Timestamp createdAt;
    private String createdBy;
    private Timestamp updatedAt;
    private String updatedBy;
}
