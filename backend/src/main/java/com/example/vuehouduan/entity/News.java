package com.example.vuehouduan.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * ============================================================
 * 新闻/公告实体类 - 对应数据库 news 表
 * ============================================================
 *
 * 【表结构说明】
 * news 表存储系统新闻、公告、资讯等内容
 * 由管理员发布，供所有用户浏览
 *
 * 【前后端数据流】
 * 管理员发布新闻 → POST /api/news → 插入 news 表
 * 前端浏览新闻列表 → GET /api/news → 分页查询 → 按时间降序
 * 前端查看新闻详情 → GET /api/news/{id} → 浏览量+1
 * 前端点赞/分享 → PUT /api/news/{id}/like 或 /share → 对应计数+1
 *
 * 【字段说明】
 * - title：新闻标题
 * - description/excerpt：新闻摘要/简介
 * - content：新闻正文（HTML或Markdown格式）
 * - image：封面图片URL
 * - author/source：作者/来源
 * - views/likes/shares：浏览量/点赞数/分享数
 * - tags：标签（多个标签用逗号分隔）
 * - category：分类（如"公告"、"新闻"、"活动"）
 *
 * 【前端兼容字段】
 * 通过 @JsonProperty 注解实现字段别名：
 * - viewCount → views
 */
@Data
@TableName("news")
public class News {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String description;
    private String excerpt;
    private String content;
    private String image;
    private String category;
    private String author;
    private String source;
    private Integer views;
    private Integer likes;
    private Integer shares;
    private String tags;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ========== Frontend compatible fields ==========

    // Frontend alias: viewCount = views
    @JsonProperty("viewCount")
    public Integer getViewCount() {
        return views;
    }
}
