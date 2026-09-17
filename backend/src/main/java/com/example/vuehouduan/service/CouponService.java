package com.example.vuehouduan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.vuehouduan.entity.Coupon;

import java.util.List;
import java.util.Map;

/**
 * 优惠券业务逻辑接口
 */
public interface CouponService extends IService<Coupon> {

    /**
     * 获取所有可用的优惠券列表
     */
    List<Coupon> getAvailableCoupons();

    /**
     * 获取所有可用的优惠券列表（包含用户领取状态）
     */
    List<Map<String, Object>> getAvailableCouponsWithStatus(Long userId);

    /**
     * 根据来源获取优惠券
     */
    List<Coupon> getCouponsBySource(Integer source);

    /**
     * 用户领取优惠券
     */
    Map<String, Object> receiveCoupon(Long userId, Long couponId);

    /**
     * 检查用户是否可以领取某优惠券
     */
    Map<String, Object> checkReceiveable(Long userId, Long couponId);
}
