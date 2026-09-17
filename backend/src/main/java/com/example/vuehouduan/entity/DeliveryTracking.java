package com.example.vuehouduan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("delivery_tracking")
public class DeliveryTracking {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("delivery_id")
    private Long deliveryId;

    @TableField("location")
    private String location;

    @TableField("description")
    private String description;

    @TableField("create_time")
    private LocalDateTime createTime;
}
