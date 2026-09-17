package com.example.vuehouduan.common;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import java.io.Serializable;

@Data
@ApiModel(value = "用户注册请求")
public class RegisterRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private String name;
    private String role;
    private String email;
    private String phone;
    private String studentId;
    private String gender;
    private String birthday;
    private String address;

    private String shopName;
    private String shopAddress;
    private String shopDescription;
    private String businessLicense;
}
