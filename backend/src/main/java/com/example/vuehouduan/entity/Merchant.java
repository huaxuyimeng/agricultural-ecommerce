package com.example.vuehouduan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * ============================================================
 * 商家实体类 - 对应数据库 merchants 表
 * ============================================================
 *
 * 【表结构说明】
 * merchants 表存储商家额外信息，通过 id 关联 users 表（一对一关系）
 * 商家首先是一个用户（role=MERCHANT），然后扩展商家专属信息
 *
 * 【审核状态】
 * - PENDING：待审核（商家注册后默认状态）
 * - APPROVED：审核通过（可以发布商品）
 * - REJECTED：审核被拒（需要重新提交）
 *
 * 【前后端数据流】
 * 商家注册 → POST /api/auth/register → 创建 user 记录（role=MERCHANT）→ 创建 merchant 记录（PENDING）
 * 管理员审核 → PUT /api/admin/merchants/{id}/approve → 更新 approval_status 为 APPROVED
 * 商家发布商品 → POST /api/products → 验证商家状态为 APPROVED
 *
 * 【字段说明】
 * - id：与 users 表的 id 相同（一对一关联）
 * - shopName：店铺名称
 * - shopAddress：店铺地址
 * - shopDescription：店铺描述
 * - shopLogo：店铺Logo（图片URL）
 * - businessLicense：营业执照（图片URL）
 * - approvalStatus：审核状态
 * - approveTime：审核通过时间
 */
@Data
@TableName("merchants")
public class Merchant {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("shop_name")
    private String shopName;

    @TableField("shop_address")
    private String shopAddress;

    @TableField("shop_description")
    private String shopDescription;

    @TableField("shop_logo")
    private String shopLogo;

    @TableField("business_license")
    private String businessLicense;

    @TableField("approval_status")
    private String approvalStatus;

    @TableField("approve_time")
    private LocalDateTime approveTime;
}
