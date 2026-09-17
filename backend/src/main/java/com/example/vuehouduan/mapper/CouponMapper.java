package com.example.vuehouduan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuehouduan.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    @Select("SELECT * FROM coupon WHERE status = 1 AND start_time <= NOW() AND end_time >= NOW() ORDER BY sort DESC, create_time DESC")
    List<Coupon> selectAvailableCoupons();

    @Select("SELECT * FROM coupon WHERE source = #{source} AND status = 1 ORDER BY sort DESC, create_time DESC")
    List<Coupon> selectBySource(@Param("source") Integer source);

    @Select("SELECT * FROM coupon WHERE type = #{type} AND status = 1 ORDER BY sort DESC, create_time DESC")
    List<Coupon> selectByType(@Param("type") Integer type);
}
