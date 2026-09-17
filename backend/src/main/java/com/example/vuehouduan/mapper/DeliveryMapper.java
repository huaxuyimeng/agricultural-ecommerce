package com.example.vuehouduan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuehouduan.entity.Delivery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DeliveryMapper extends BaseMapper<Delivery> {

    @Select("SELECT * FROM deliveries WHERE order_id = #{orderId}")
    Delivery selectByOrderId(@Param("orderId") String orderId);
}
