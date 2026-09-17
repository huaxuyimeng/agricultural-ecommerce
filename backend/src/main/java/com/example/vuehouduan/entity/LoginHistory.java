package com.example.vuehouduan.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * ============================================================
 * 登录历史实体类 - 对应数据库 login_history 表
 * ============================================================
 *
 * 【表结构说明】
 * login_history 表存储用户的登录/登出记录，用于安全审计和用户行为分析
 * 通过 user_id 关联 users 表（一对多关系）
 *
 * 【前后端数据流】
 * 用户登录成功 → AuthController.login() → 插入 login_history 记录
 * 用户登出 → AuthController.logout() → 更新 logout_time 和 duration
 * 用户查看登录历史 → GET /api/user/login-history → 查询 login_history 表
 * 管理员查看所有登录历史 → GET /api/admin/login-history → 分页查询
 *
 * 【字段说明】
 * - userId：用户ID
 * - username：用户名（冗余字段，方便查询）
 * - ipAddress：登录IP地址
 * - userAgent：浏览器UserAgent字符串
 * - loginTime/logoutTime：登录/登出时间
 * - duration：在线时长（秒）
 * - loginStatus：登录状态（success/failed）
 * - errorMessage：登录失败时的错误信息
 */
@Data
@TableName("login_history")
public class LoginHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    private String username;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("user_agent")
    private String userAgent;

    @TableField("login_time")
    private LocalDateTime loginTime;

    @TableField("logout_time")
    private LocalDateTime logoutTime;

    private Integer duration;

    @TableField("login_status")
    private String loginStatus;

    @TableField("error_message")
    private String errorMessage;

    // ========== Frontend compatible fields ==========

    // Frontend alias: loginTime = loginTime
    @JsonProperty("loginTime")
    public LocalDateTime getLoginTimeValue() {
        return loginTime;
    }

    // Frontend alias: logoutTime = logoutTime
    @JsonProperty("logoutTime")
    public LocalDateTime getLogoutTimeValue() {
        return logoutTime;
    }
}
