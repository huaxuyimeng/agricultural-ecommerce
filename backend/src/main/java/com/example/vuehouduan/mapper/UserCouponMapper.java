package com.example.vuehouduan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuehouduan.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {

        @Select("SELECT uc.*, c.name as coupon_name, c.type as coupon_type, c.amount as coupon_amount, " +
                        "c.min_amount as coupon_min_amount, c.description as coupon_description, " +
                        "c.valid_type as coupon_valid_type, c.valid_days as coupon_valid_days " +
                        "FROM user_coupon uc " +
                        "LEFT JOIN coupon c ON uc.coupon_id = c.id " +
                        "WHERE uc.user_id = #{userId} " +
                        "ORDER BY uc.receive_time DESC")
        List<Map<String, Object>> selectByUserId(@Param("userId") Long userId);

        @Select("SELECT uc.*, c.name as coupon_name, c.type as coupon_type, c.amount as coupon_amount, " +
                        "c.min_amount as coupon_min_amount, c.description as coupon_description " +
                        "FROM user_coupon uc " +
                        "LEFT JOIN coupon c ON uc.coupon_id = c.id " +
                        "WHERE uc.user_id = #{userId} AND uc.status = #{status} " +
                        "ORDER BY uc.receive_time DESC")
        List<Map<String, Object>> selectByUserIdAndStatus(@Param("userId") Long userId,
                        @Param("status") Integer status);

        @Select("SELECT COUNT(*) FROM user_coupon WHERE user_id = #{userId} AND coupon_id = #{couponId}")
        int countReceivedByUserAndCoupon(@Param("userId") Long userId, @Param("couponId") Long couponId);

        @Select("SELECT uc.*, c.name as coupon_name, c.type as coupon_type, c.amount as coupon_amount, " +
                        "c.min_amount as coupon_min_amount, c.description as coupon_description " +
                        "FROM user_coupon uc " +
                        "LEFT JOIN coupon c ON uc.coupon_id = c.id " +
                        "WHERE uc.user_id = #{userId} AND uc.status = 1 " +
                        "AND uc.valid_start <= NOW() AND uc.valid_end >= NOW() " +
                        "ORDER BY uc.valid_end ASC")
        List<Map<String, Object>> selectAvailableByUserId(@Param("userId") Long userId);

        @Select("<script>" +
                        "SELECT uc.id, u.username, c.name as couponName, c.amount as amount, c.min_amount as minAmount, "
                        +
                        "uc.order_no as orderNo, uc.discount_amount as orderAmount, uc.use_time as useTime, uc.status, "
                        +
                        "uc.receive_time as receiveTime " +
                        "FROM user_coupon uc " +
                        "LEFT JOIN users u ON uc.user_id = u.id " +
                        "LEFT JOIN coupon c ON uc.coupon_id = c.id " +
                        "WHERE uc.status = 2 " +
                        "<if test='username != null and username != \"\"'>" +
                        "AND u.username LIKE CONCAT('%', #{username}, '%') " +
                        "</if>" +
                        "<if test='couponName != null and couponName != \"\"'>" +
                        "AND c.name LIKE CONCAT('%', #{couponName}, '%') " +
                        "</if>" +
                        "<if test='startTime != null and startTime != \"\"'>" +
                        "AND uc.use_time &gt;= #{startTime} " +
                        "</if>" +
                        "<if test='endTime != null and endTime != \"\"'>" +
                        "AND uc.use_time &lt;= #{endTime} " +
                        "</if>" +
                        "ORDER BY uc.use_time DESC" +
                        "</script>")
        List<Map<String, Object>> selectUsageHistory(
                        @Param("username") String username,
                        @Param("couponName") String couponName,
                        @Param("startTime") String startTime,
                        @Param("endTime") String endTime);
}
