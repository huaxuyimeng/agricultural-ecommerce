package com.example.vuehouduan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * ============================================================
 * 收藏实体类 - 对应数据库 favorites 表
 * ============================================================
 *
 * 【表结构说明】
 * favorites 表存储用户收藏记录，支持收藏多种类型的内容（商品、店铺等）
 * 通过 user_id 关联 users 表，通过 target_id 关联目标内容
 *
 * 【收藏类型 type】
 * - product：商品收藏
 * - merchant：店铺收藏
 *
 * 【前后端数据流】
 * 前端收藏商品 → POST /api/favorites → 插入 favorites 表 → 商品收藏数+1
 * 前端查看收藏 → GET /api/favorites → 查询 favorites 表 → 填充商品信息
 * 前端取消收藏 → DELETE /api/favorites/{id} → 删除记录 → 商品收藏数-1
 *
 * 【字段说明】
 * - userId：用户ID
 * - type：收藏类型（product/merchant）
 * - targetId：目标ID（商品ID或店铺ID）
 * - targetName：目标名称（冗余字段，方便显示）
 *
 * 【非数据库字段】
 * - product：商品对象（关联查询填充，用于前端显示商品详情）
 */
@Data
@TableName("favorites")
public class Favorite {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    private String type;

    @TableField("target_id")
    private Long targetId;

    @TableField("target_name")
    private String targetName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private Product product;
}
