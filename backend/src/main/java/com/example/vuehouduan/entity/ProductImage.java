package com.example.vuehouduan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * ============================================================
 * 商品图片实体类 - 对应数据库 product_images 表
 * ============================================================
 *
 * 【表结构说明】
 * product_images 表存储商品的多张图片，一个商品可以有多张图片
 * 通过 product_id 关联 products 表（一对多关系）
 *
 * 【前后端数据流】
 * 商家发布商品 → POST /api/products → 上传图片 → 插入 product_images 表
 * 前端查看商品详情 → GET /api/products/{id} → 查询 product_images 表 → 按 sort_order 排序
 *
 * 【字段说明】
 * - productId：商品ID
 * - imageUrl：图片URL
 * - isMain：是否主图（用于商品列表显示）
 * - sortOrder：排序顺序（数字越小越靠前）
 */
@Data
@TableName("product_images")
public class ProductImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("product_id")
    private Long productId;

    @TableField("image_url")
    private String imageUrl;

    @TableField("is_main")
    private Boolean isMain;

    @TableField("sort_order")
    private Integer sortOrder;
}
