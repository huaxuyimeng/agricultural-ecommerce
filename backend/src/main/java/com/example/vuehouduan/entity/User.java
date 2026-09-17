package com.example.vuehouduan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ============================================================
 * 用户实体类 - 对应数据库 users 表
 * ============================================================
 *
 * 【表结构说明】
 * users 表存储系统所有用户（普通用户、商家、管理员）
 * 通过 role 字段区分用户角色：USER（普通用户）、MERCHANT（商家）、ADMIN（管理员）
 *
 * 【ORM映射说明】
 * - @TableName("users")：映射到数据库 users 表
 * - @TableId(type = IdType.AUTO)：主键自增
 * - @TableField("xxx")：指定数据库字段名（驼峰转下划线）
 * - @TableField(exist = false)：非数据库字段，仅用于业务逻辑
 * - @TableField(fill = FieldFill.INSERT)：插入时自动填充（如 createTime）
 * - @TableField(fill = FieldFill.INSERT_UPDATE)：插入和更新时自动填充（如 updateTime）
 *
 * 【前后端数据流】
 * 前端请求 → Controller → Service → Mapper → 数据库
 * 数据库返回 → Mapper → Service → Controller → 前端响应
 *
 * 【商家相关字段】
 * 当用户角色为 MERCHANT 时，以下非数据库字段会从 merchant 表关联查询填充：
 * - shopName：店铺名称
 * - shopAddress：店铺地址
 * - shopDescription：店铺描述
 * - shopLogo：店铺Logo
 * - businessLicense：营业执照
 * - approvalStatus：审核状态（PENDING/APPROVED/REJECTED）
 */
@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;
    private String name;

    @TableField("role")
    private String role;

    private String avatar;
    private String email;
    private String phone;
    private BigDecimal account;
    private String token;

    @TableField("student_id")
    private String studentId;

    private Integer status;
    private String description;
    private String gender;
    private LocalDate birthday;
    private String address;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    // ========== Merchant related fields (for MERCHANT role) ==========

    @TableField(exist = false)
    private String shopName;

    @TableField(exist = false)
    private String shopAddress;

    @TableField(exist = false)
    private String shopDescription;

    @TableField(exist = false)
    private String shopLogo;

    @TableField(exist = false)
    private String businessLicense;

    @TableField(exist = false)
    private String approvalStatus;
}
