package com.example.vuehouduan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("deliveries")
public class Delivery {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private String orderId;

    @TableField("express_company")
    private String expressCompany;

    @TableField("express_number")
    private String expressNumber;

    @TableField("status")
    private String status;

    @TableField("current_location")
    private String currentLocation;

    @TableField("ship_time")
    private LocalDateTime shipTime;

    @TableField("signed_time")
    private LocalDateTime signedTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
