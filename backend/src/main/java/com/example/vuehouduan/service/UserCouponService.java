package com.example.vuehouduan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.vuehouduan.entity.UserCoupon;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 用户优惠券业务逻辑接口
 */
public interface UserCouponService extends IService<UserCoupon> {

    /**
     * 获取用户的所有优惠券
     */
    List<Map<String, Object>> getUserCoupons(Long userId);

    /**
     * 根据状态获取用户优惠券
     */
    List<Map<String, Object>> getUserCouponsByStatus(Long userId, Integer status);

    /**
     * 获取用户可用的优惠券
     */
    List<Map<String, Object>> getAvailableUserCoupons(Long userId);

    /**
     * 获取结算时可用的优惠券（根据订单金额过滤）
     */
    List<Map<String, Object>> getCheckoutCoupons(Long userId, BigDecimal orderAmount);

    /**
     * 使用优惠券（核销）
     */
    Map<String, Object> useCoupon(Long userCouponId, String orderId);

    /**
     * 获取用户优惠券统计信息
     */
    Map<String, Object> getUserCouponStats(Long userId);
}
