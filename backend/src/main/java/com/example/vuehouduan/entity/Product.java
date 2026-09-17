package com.example.vuehouduan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ============================================================
 * 商品实体类 - 对应数据库 products 表
 * ============================================================
 *
 * 【表结构说明】
 * products 表存储商品基本信息，商品图片存储在 product_image 表
 * 通过 merchant_id 关联商家（users 表或 merchants 表）
 *
 * 【商品状态】
 * - pending：待审核（商家发布后）
 * - approved：已审核通过（可展示）
 * - rejected：审核被拒
 * - offline：已下架
 *
 * 【前后端数据流】
 * 前端浏览商品 → GET /api/products → 分页查询 → 填充商家信息和图片列表
 * 商家发布商品 → POST /api/products → 插入 products 表 → 状态设为 pending
 * 管理员审核 → PUT /api/admin/products/{id}/approve → 状态改为 approved
 *
 * 【字段说明】
 * - price：当前售价
 * - originalPrice：原价（用于显示折扣）
 * - image/ img：商品主图（前端兼容别名）
 * - category：商品分类（蔬菜、水果、粮食等）
 * - stock：库存数量
 * - unit：计量单位（斤、kg、个等）
 * - views/sales/favorites/reviews：浏览量/销量/收藏数/评价数
 * - isHot/isNew/isRecommend：是否热门/新品/推荐
 *
 * 【非数据库字段】
 * - merchantName：商家店铺名（关联查询填充）
 * - merchantAvatar：商家头像（关联查询填充）
 * - images：商品图片列表（从 product_image 表查询）
 * - img：image 字段的别名（前端兼容）
 */
@Data
@TableName("products")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String description;
    private BigDecimal price;

    @TableField("original_price")
    private BigDecimal originalPrice;

    // Both 'image' and 'img' are exposed for frontend compatibility
    // Note: image field is TEXT type in database to support base64 images
    private String image;

    private String category;
    private Integer stock;
    private String unit;
    private String weight;
    private String origin;
    private String brand;

    @TableField("is_hot")
    private Boolean isHot;

    @TableField("is_new")
    private Boolean isNew;

    @TableField("is_recommend")
    private Boolean isRecommend;

    private Integer views;
    private Integer sales;
    private Integer favorites;
    private Integer reviews;
    private String status;

    @TableField("merchant_id")
    private Long merchantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("approve_time")
    private LocalDateTime approveTime;

    @TableField("submit_time")
    private LocalDateTime submitTime;

    // ========== Non-persistent fields for join queries ==========

    @TableField(exist = false)
    private String merchantName;

    @TableField(exist = false)
    private String merchantAvatar;

    // ========== Frontend compatible alias fields ==========

    // Frontend alias: img = image (both are exposed)
    @TableField(exist = false)
    private String img;

    @TableField(exist = false)
    private java.util.List<String> images;
}
