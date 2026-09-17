package com.example.vuehouduan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * ============================================================
 * 数据备份实体类 - 对应数据库 backups 表
 * ============================================================
 *
 * 【表结构说明】
 * backups 表存储数据库备份记录，用于数据恢复和安全管理
 *
 * 【备份类型 backupType】
 * - FULL：全量备份
 * - INCREMENTAL：增量备份
 *
 * 【备份状态 status】
 * - PENDING：备份中
 * - COMPLETED：备份完成
 * - FAILED：备份失败
 *
 * 【前后端数据流】
 * 管理员创建备份 → POST /api/system/backup → 插入 backups 表
 * 管理员查看备份列表 → GET /api/system/backups → 查询 backups 表
 *
 * 【字段说明】
 * - backupName：备份名称
 * - backupType：备份类型（FULL/INCREMENTAL）
 * - backupPath：备份文件存储路径
 * - backupSize：备份文件大小
 * - status：备份状态
 * - createdBy：备份操作人
 * - duration：备份耗时
 * - createTime：备份创建时间
 */
@Data
@TableName("backups")
public class Backup {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("backup_name")
    private String backupName;

    @TableField("backup_type")
    private String backupType;

    @TableField("backup_path")
    private String backupPath;

    @TableField("backup_size")
    private String backupSize;

    private String status;

    @TableField("created_by")
    private String createdBy;

    private String duration;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
