package com.example.vuehouduan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * ============================================================
 * 系统日志实体类 - 对应数据库 system_logs 表
 * ============================================================
 *
 * 【表结构说明】
 * system_logs 表存储系统运行日志，用于问题排查和安全审计
 *
 * 【日志级别 level】
 * - INFO：普通信息
 * - WARN：警告信息
 * - ERROR：错误信息
 * - DEBUG：调试信息
 *
 * 【前后端数据流】
 * 系统运行中自动记录 → 插入 system_logs 表
 * 管理员查看日志 → GET /api/system/logs → 按级别和关键词筛选 → 分页返回
 *
 * 【字段说明】
 * - level：日志级别（INFO/WARN/ERROR/DEBUG）
 * - category：日志分类（如"订单"、"用户"、"系统"）
 * - message：日志内容
 * - userId：操作用户ID
 * - username：操作用户名（冗余字段）
 * - ipAddress：操作IP地址
 * - createTime：日志创建时间
 */
@Data
@TableName("system_logs")
public class SystemLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String level;
    private String category;
    private String message;

    @TableField("user_id")
    private Long userId;

    private String username;

    @TableField("ip_address")
    private String ipAddress;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
