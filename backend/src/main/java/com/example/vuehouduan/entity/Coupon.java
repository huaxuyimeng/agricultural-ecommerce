package com.example.vuehouduan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("coupon")
public class Coupon {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("coupon_code")
    private String couponCode;

    @TableField("name")
    private String name;

    @TableField("type")
    private Integer type;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("min_amount")
    private BigDecimal minAmount;

    @TableField("total_num")
    private Integer totalNum;

    @TableField("received_num")
    private Integer receivedNum;

    @TableField("used_num")
    private Integer usedNum;

    @TableField("per_limit")
    private Integer perLimit;

    @TableField("valid_type")
    private Integer validType;

    @TableField("valid_days")
    private Integer validDays;

    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    @TableField("source")
    private Integer source;

    @TableField("description")
    private String description;

    @TableField("sort")
    private Integer sort;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
