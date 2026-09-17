package com.example.vuehouduan.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ============================================================
 * 售后实体类 - 对应数据库 after_sales 表
 * ============================================================
 *
 * 【表结构说明】
 * after_sales 表存储用户的售后申请记录（退款、退货退款、换货）
 * 通过 order_id 关联 orders 表，通过 product_id 关联 products 表
 *
 * 【售后类型 type】
 * - refund_only：仅退款（未发货状态）
 * - return_refund：退货退款（已收货状态）
 * - exchange：换货（已收货状态）
 *
 * 【售后状态 status】
 * - pending：待商家审核
 * - approved：商家已同意
 * - rejected：商家已拒绝
 * - completed：售后已完成
 * - cancelled：用户已取消
 *
 * 【退款状态 refundStatus】
 * - none：无退款
 * - processing：退款处理中
 * - success：退款成功
 * - failed：退款失败
 *
 * 【前后端数据流】
 * 前端申请售后 → POST /api/after-sales → 插入 after_sales 表
 * 商家审核售后 → PUT /api/after-sales/{id}/approve → 更新 status
 * 用户填写退货物流 → PUT /api/after-sales/{id}/express → 更新 expressCompany/expressNo
 * 商家完成售后 → PUT /api/after-sales/{id}/complete → 退款 + 恢复库存
 */
@Data
@TableName("after_sales")
public class AfterSales {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private Long orderId;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Long userId;

    @TableField("merchant_id")
    private Long merchantId;

    @TableField("product_id")
    private Long productId;

    @TableField("product_name")
    private String productName;

    @TableField("product_image")
    private String productImage;

    private Integer quantity;

    @TableField("refund_amount")
    private BigDecimal refundAmount;

    private String type;

    private String reason;

    private String description;

    private String images;

    private String status;

    @TableField("refund_status")
    private String refundStatus;

    @TableField("express_company")
    private String expressCompany;

    @TableField("express_no")
    private String expressNo;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("handle_time")
    private LocalDateTime handleTime;

    // ========== Frontend alias ==========

    @JsonProperty("orderId")
    public Long getOrderId() {
        return orderId;
    }

    @JsonProperty("orderNo")
    public String getOrderNo() {
        return orderNo;
    }

    @JsonProperty("productName")
    public String getProductName() {
        return productName;
    }

    @JsonProperty("productImage")
    public String getProductImage() {
        return productImage;
    }

    @JsonProperty("refundAmount")
    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    @JsonProperty("expressCompany")
    public String getExpressCompany() {
        return expressCompany;
    }

    @JsonProperty("expressNo")
    public String getExpressNo() {
        return expressNo;
    }

    @JsonProperty("createTime")
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    @JsonProperty("handleTime")
    public LocalDateTime getHandleTime() {
        return handleTime;
    }
}
