package com.example.vuehouduan.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ============================================================
 * 订单实体类 - 对应数据库 orders 表
 * ============================================================
 *
 * 【表结构说明】
 * orders 表存储订单主信息，订单商品详情存储在 order_product 表
 * 通过 order_id 字段关联订单主表和订单商品表
 *
 * 【订单状态流转】
 * PENDING（待支付）→ PAID（已支付）→ SHIPPED（已发货）→ DELIVERED（已签收）→ COMPLETED（已完成）
 * 任意状态 → CANCELLED（已取消）
 * 已支付状态 → REFUNDED（已退款）
 *
 * 【前后端数据流】
 * 前端创建订单 → POST /api/orders → OrderController → OrderService → 插入 orders 表和 order_product 表
 * 前端查询订单 → GET /api/orders/{id} → 返回 Order 对象（含 products 列表）
 *
 * 【字段说明】
 * - orderId：订单号（业务主键，格式：ORD+时间戳+随机数）
 * - userId：买家ID
 * - merchantId：商家ID
 * - totalAmount：订单总金额
 * - actualAmount：实际支付金额（可能有优惠）
 * - status：订单状态
 * - paymentMethod：支付方式（alipay/wechat/cash）
 * - paymentStatus：支付状态（pending/paid/refunded）
 * - expressCompany/expressNo：快递公司和快递单号（商家发货时填写）
 *
 * 【非数据库字段】
 * - userName：买家用户名（关联查询填充）
 * - merchantName：商家店铺名（关联查询填充）
 * - products：订单商品列表（关联 order_product 表查询）
 *
 * 【前端兼容字段】
 * 通过 @JsonProperty 注解实现字段别名，兼容前端不同版本的字段名：
 * - shippingName → contactName
 * - shippingPhone → contactPhone
 * - total → totalAmount
 */
@Data
@TableName("orders")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private String orderId;

    @TableField("user_id")
    private Long userId;

    @TableField("merchant_id")
    private Long merchantId;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("actual_amount")
    private BigDecimal actualAmount;

    @TableField("coupon_id")
    private Long couponId;

    @TableField("coupon_amount")
    private BigDecimal couponAmount;

    @TableField("coupon_code")
    private String couponCode;

    private String status;

    @TableField("payment_method")
    private String paymentMethod;

    @TableField("payment_status")
    private String paymentStatus;

    @TableField("shipping_address")
    private String shippingAddress;

    @TableField("contact_name")
    private String contactName;

    @TableField("contact_phone")
    private String contactPhone;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField("pay_time")
    private LocalDateTime payTime;

    @TableField("ship_time")
    private LocalDateTime shipTime;

    @TableField("deliver_time")
    private LocalDateTime deliverTime;

    @TableField("close_time")
    private LocalDateTime closeTime;

    // ========== Express fields ==========
    @TableField("express_company")
    private String expressCompany;

    @TableField("express_no")
    private String expressNo;

    // ========== Frontend compatible fields (alias) ==========

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String merchantName;

    @TableField(exist = false)
    private List<OrderProduct> products;

    // Frontend alias: shippingName = contactName
    @JsonProperty("shippingName")
    public String getShippingName() {
        return contactName;
    }

    // Frontend alias: shippingPhone = contactPhone
    @JsonProperty("shippingPhone")
    public String getShippingPhone() {
        return contactPhone;
    }

    // Frontend alias: total = totalAmount
    @JsonProperty("total")
    public BigDecimal getTotal() {
        return totalAmount;
    }

    // Frontend alias: paymentTime = payTime
    @JsonProperty("paymentTime")
    public LocalDateTime getPaymentTime() {
        return payTime;
    }

    @JsonProperty("receiver")
    public String getReceiver() {
        return contactName;
    }

    @JsonProperty("phone")
    public String getPhone() {
        return contactPhone;
    }

    @JsonProperty("address")
    public String getAddress() {
        return shippingAddress;
    }

    @JsonProperty("createdAt")
    public LocalDateTime getCreatedAt() {
        return createTime;
    }

    @JsonProperty("expressName")
    public String getExpressName() {
        return expressCompany;
    }

    @JsonProperty("trackingNumber")
    public String getTrackingNumber() {
        return expressNo;
    }

    @JsonProperty("shippingCompany")
    public String getShippingCompany() {
        return expressCompany;
    }
}
