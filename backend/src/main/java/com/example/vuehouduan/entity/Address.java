package com.example.vuehouduan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * ============================================================
 * 收货地址实体类 - 对应数据库 addresses 表
 * ============================================================
 *
 * 【表结构说明】
 * addresses 表存储用户收货地址，一个用户可以有多个地址
 * 通过 user_id 关联 users 表（一对多关系）
 *
 * 【前后端数据流】
 * 前端新增地址 → POST /api/addresses → 插入 addresses 表
 * 前端获取地址列表 → GET /api/addresses → 按 is_default 排序 → 默认地址排最前
 * 前端下单时选择地址 → 将地址信息复制到订单表
 *
 * 【字段说明】
 * - userId：用户ID
 * - receiverName：收货人姓名
 * - phone：联系电话
 * - province/city/district：省/市/区
 * - detailAddress：详细地址（街道、门牌号等）
 * - label：地址标签（如"家"、"公司"）
 * - isDefault：是否默认地址（每个用户只能有一个默认地址）
 *
 * 【业务逻辑】
 * - 设置新默认地址时，需先取消其他地址的默认状态
 * - getFullAddress() 方法拼接完整地址用于前端显示
 */
@Data
@TableName("addresses")
public class Address {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String receiverName;

    private String phone;

    private String province;

    private String city;

    private String district;

    private String detailAddress;

    private String label;

    @TableField("is_default")
    private Boolean isDefault;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 前端兼容字段：完整地址
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (province != null) sb.append(province);
        if (city != null) sb.append(city);
        if (district != null) sb.append(district);
        if (detailAddress != null) sb.append(detailAddress);
        return sb.toString();
    }
}
