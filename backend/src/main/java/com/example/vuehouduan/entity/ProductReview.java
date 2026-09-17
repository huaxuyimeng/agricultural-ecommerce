package com.example.vuehouduan.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * ============================================================
 * 商品评价实体类 - 对应数据库 product_reviews 表
 * ============================================================
 *
 * 【表结构说明】
 * product_reviews 表存储用户对商品的评价记录
 * 通过 product_id 关联 products 表，通过 user_id 关联 users 表，通过 order_id 关联 orders 表
 *
 * 【评价状态 status】
 * - pending：待审核
 * - approved：审核通过（公开展示）
 * - rejected：审核不通过
 *
 * 【前后端数据流】
 * 前端添加评价 → POST /api/reviews → 插入 product_reviews 表 → 商品评价数+1
 * 前端查看商品评价 → GET /api/reviews/product/{id} → 查询 approved 状态的评价
 * 商家回复评价 → PUT /api/reviews/{id}/reply → 更新 replyContent 和 replyTime
 *
 * 【字段说明】
 * - productId：商品ID
 * - userId：评价用户ID
 * - orderId：订单号（验证用户是否购买过该商品）
 * - rating：评分（1-5星）
 * - content：评价内容
 * - images：评价图片（多张图片用逗号分隔）
 * - isAnonymous：是否匿名评价
 * - replyContent/replyTime：商家回复内容和时间
 *
 * 【非数据库字段】
 * - userName：评价用户名（关联查询填充）
 * - userAvatar：用户头像（关联查询填充）
 */
@Data
@TableName("product_reviews")
public class ProductReview {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("product_id")
    private Long productId;

    @TableField("user_id")
    private Long userId;

    @TableField("order_id")
    private String orderId;

    private Integer rating;
    private String content;
    private String images;

    @TableField("is_anonymous")
    private Boolean isAnonymous;

    private String status;

    @TableField("reply_content")
    private String replyContent;

    @TableField("reply_time")
    private LocalDateTime replyTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // ========== Non-persistent fields ==========

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String userAvatar;

    // ========== Frontend compatible fields ==========

    // Frontend alias: userId -> userId (same field, added for compatibility)
    @JsonProperty("userId")
    public Long getUserIdValue() {
        return userId;
    }

    // Frontend alias: productId -> productId (same field, added for compatibility)
    @JsonProperty("productId")
    public Long getProductIdValue() {
        return productId;
    }
}
