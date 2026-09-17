package com.example.vuehouduan.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ============================================================
 * 购物车实体类 - 对应数据库 cart 表
 * ============================================================
 *
 * 【表结构说明】
 * cart 表存储用户购物车记录，通过 user_id 和 product_id 关联
 *
 * 【前后端数据流】
 * 前端加入购物车 → POST /api/cart → 插入 cart 记录
 * 前端查看购物车 → GET /api/cart → 查询 cart 表 → 填充商品信息
 * 前端修改数量 → PUT /api/cart/{id} → 更新 quantity 和 totalPrice
 *
 * 【字段说明】
 * - userId：用户ID
 * - productId：商品ID
 * - quantity：购买数量
 * - price：商品单价（加入购物车时的价格）
 * - totalPrice：小计（quantity * price）
 * - isSelected：是否勾选（用于结算时选择商品）
 *
 * 【非数据库字段】
 * - productName：商品名称（关联查询填充）
 * - productImage：商品图片（关联查询填充）
 */
@Data
@TableName("cart")
public class Cart {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("product_id")
    private Long productId;

    private Integer quantity;
    private BigDecimal price;

    @TableField("total_price")
    private BigDecimal totalPrice;

    @TableField("sku_id")
    private Long skuId;

    @TableField("sku_attributes")
    private String skuAttributes;

    @TableField("is_selected")
    private Boolean isSelected;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String productName;

    @TableField(exist = false)
    private String productImage;

    // ========== Frontend compatible fields ==========

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
}
