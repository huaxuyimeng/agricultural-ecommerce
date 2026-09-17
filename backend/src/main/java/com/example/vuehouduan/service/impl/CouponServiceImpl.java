package com.example.vuehouduan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vuehouduan.entity.Coupon;
import com.example.vuehouduan.entity.UserCoupon;
import com.example.vuehouduan.mapper.CouponMapper;
import com.example.vuehouduan.mapper.UserCouponMapper;
import com.example.vuehouduan.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Override
    public List<Coupon> getAvailableCoupons() {
        return couponMapper.selectAvailableCoupons();
    }

    @Override
    public List<Map<String, Object>> getAvailableCouponsWithStatus(Long userId) {
        List<Coupon> coupons = couponMapper.selectAvailableCoupons();
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        for (Coupon coupon : coupons) {
            Map<String, Object> couponMap = new HashMap<>();
            couponMap.put("id", coupon.getId());
            couponMap.put("couponCode", coupon.getCouponCode());
            couponMap.put("name", coupon.getName());
            couponMap.put("type", coupon.getType());
            couponMap.put("amount", coupon.getAmount());
            couponMap.put("minAmount", coupon.getMinAmount());
            couponMap.put("totalNum", coupon.getTotalNum());
            couponMap.put("receivedNum", coupon.getReceivedNum());
            couponMap.put("usedNum", coupon.getUsedNum());
            couponMap.put("perLimit", coupon.getPerLimit());
            couponMap.put("validType", coupon.getValidType());
            couponMap.put("validDays", coupon.getValidDays());
            couponMap.put("startTime", coupon.getStartTime());
            couponMap.put("endTime", coupon.getEndTime());
            couponMap.put("source", coupon.getSource());
            couponMap.put("description", coupon.getDescription());
            couponMap.put("sort", coupon.getSort());
            couponMap.put("status", coupon.getStatus());

            // 检查用户是否已领取
            boolean alreadyReceived = false;
            if (userId != null) {
                int receivedCount = userCouponMapper.countReceivedByUserAndCoupon(userId, coupon.getId());
                alreadyReceived = receivedCount > 0;
            }
            couponMap.put("alreadyReceived", alreadyReceived);

            result.add(couponMap);
        }

        return result;
    }

    @Override
    public List<Coupon> getCouponsBySource(Integer source) {
        return couponMapper.selectBySource(source);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> receiveCoupon(Long userId, Long couponId) {
        Map<String, Object> result = new HashMap<>();

        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            result.put("success", false);
            result.put("message", "优惠券不存在");
            return result;
        }

        if (coupon.getStatus() != 1) {
            result.put("success", false);
            result.put("message", "优惠券已失效");
            return result;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime())) {
            result.put("success", false);
            result.put("message", "优惠券尚未开始");
            return result;
        }
        if (now.isAfter(coupon.getEndTime())) {
            result.put("success", false);
            result.put("message", "优惠券已过期");
            return result;
        }

        if (coupon.getTotalNum() != -1 && coupon.getReceivedNum() >= coupon.getTotalNum()) {
            result.put("success", false);
            result.put("message", "优惠券已领完");
            return result;
        }

        if (coupon.getPerLimit() != -1) {
            int receivedCount = userCouponMapper.countReceivedByUserAndCoupon(userId, couponId);
            if (receivedCount >= coupon.getPerLimit()) {
                result.put("success", false);
                result.put("message", "已达到领取上限");
                return result;
            }
        }

        LocalDateTime validStart = now;
        LocalDateTime validEnd = coupon.getEndTime();
        if (coupon.getValidType() == 2 && coupon.getValidDays() != null) {
            validEnd = now.plusDays(coupon.getValidDays());
        }

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setCouponCode(coupon.getCouponCode());
        userCoupon.setStatus(1);
        userCoupon.setReceiveTime(now);
        userCoupon.setValidStart(validStart);
        userCoupon.setValidEnd(validEnd);
        userCoupon.setCreateTime(now);
        userCoupon.setUpdateTime(now);

        userCouponMapper.insert(userCoupon);

        coupon.setReceivedNum(coupon.getReceivedNum() + 1);
        coupon.setUpdateTime(now);
        couponMapper.updateById(coupon);

        result.put("success", true);
        result.put("message", "领取成功");
        result.put("userCouponId", userCoupon.getId());
        result.put("validEnd", validEnd);
        return result;
    }

    @Override
    public Map<String, Object> checkReceiveable(Long userId, Long couponId) {
        Map<String, Object> result = new HashMap<>();

        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            result.put("receiveable", false);
            result.put("reason", "优惠券不存在");
            return result;
        }

        if (coupon.getStatus() != 1) {
            result.put("receiveable", false);
            result.put("reason", "优惠券已失效");
            return result;
        }

        if (coupon.getTotalNum() != -1 && coupon.getReceivedNum() >= coupon.getTotalNum()) {
            result.put("receiveable", false);
            result.put("reason", "优惠券已领完");
            return result;
        }

        if (coupon.getPerLimit() != -1) {
            int receivedCount = userCouponMapper.countReceivedByUserAndCoupon(userId, couponId);
            if (receivedCount >= coupon.getPerLimit()) {
                result.put("receiveable", false);
                result.put("reason", "已达到领取上限");
                return result;
            }
        }

        result.put("receiveable", true);
        result.put("reason", "可以领取");
        return result;
    }
}
