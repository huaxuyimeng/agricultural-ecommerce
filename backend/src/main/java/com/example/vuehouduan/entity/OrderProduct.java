package com.example.vuehouduan.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;

/**
 * ============================================================
 * 订单商品实体类 - 对应数据库 order_products 表
 * ============================================================
 *
 * 【表结构说明】
 * order_products 表存储订单中的商品详情，一个订单可以包含多个商品
 * 通过 order_id 字段关联 orders 表（一对多关系）
 *
 * 【前后端数据流】
 * 前端创建订单 → POST /api/orders → 遍历商品列表 → 插入多条 order_products 记录
 * 前端查看订单详情 → GET /api/orders/{id} → 查询 order_products 表 → 填充商品图片
 *
 * 【字段说明】
 * - orderId：订单号（关联 orders 表的 order_id 字段）
 * - productId：商品ID
 * - productName：商品名称（下单时快照，后续商品改名不影响订单）
 * - quantity：购买数量
 * - price：商品单价（下单时价格）
 * - totalPrice：小计（quantity * price）
 *
 * 【非数据库字段】
 * - productImage：商品图片（关联查询填充）
 *
 * 【前端兼容字段】
 * 通过 @JsonProperty 注解实现字段别名：
 * - count → quantity
 * - subtotal → totalPrice
 */
@Data
@TableName("order_products")
public class OrderProduct {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private String orderId;

    @TableField("product_id")
    private Long productId;

    @TableField("product_name")
    private String productName;

    private Integer quantity;
    private BigDecimal price;

    @TableField("total_price")
    private BigDecimal totalPrice;

    @TableField("sku_id")
    private Long skuId;

    @TableField("sku_attributes")
    private String skuAttributes;

    // ========== Frontend compatible fields ==========

    @TableField(exist = false)
    private String productImage;

    // Frontend alias: count = quantity
    @JsonProperty("count")
    public Integer getCount() {
        return quantity;
    }

    // Frontend alias: subtotal = totalPrice
    @JsonProperty("subtotal")
    public BigDecimal getSubtotal() {
        return totalPrice;
    }

    @JsonProperty("name")
    public String getName() {
        return productName;
    }

    @JsonProperty("img")
    public String getImg() {
        return productImage;
    }
}
