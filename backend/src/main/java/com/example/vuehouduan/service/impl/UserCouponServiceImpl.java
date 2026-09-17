package com.example.vuehouduan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vuehouduan.entity.UserCoupon;
import com.example.vuehouduan.mapper.UserCouponMapper;
import com.example.vuehouduan.service.UserCouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements UserCouponService {

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Override
    public List<Map<String, Object>> getUserCoupons(Long userId) {
        return userCouponMapper.selectByUserId(userId);
    }

    @Override
    public List<Map<String, Object>> getUserCouponsByStatus(Long userId, Integer status) {
        return userCouponMapper.selectByUserIdAndStatus(userId, status);
    }

    @Override
    public List<Map<String, Object>> getAvailableUserCoupons(Long userId) {
        return userCouponMapper.selectAvailableByUserId(userId);
    }

    @Override
    public List<Map<String, Object>> getCheckoutCoupons(Long userId, BigDecimal orderAmount) {
        List<Map<String, Object>> coupons = userCouponMapper.selectAvailableByUserId(userId);
        LocalDateTime now = LocalDateTime.now();

        for (Map<String, Object> coupon : coupons) {
            Object validEndObj = coupon.get("valid_end");
            LocalDateTime validEnd = null;
            if (validEndObj instanceof LocalDateTime) {
                validEnd = (LocalDateTime) validEndObj;
            }

            // 检查是否过期
            if (validEnd != null && now.isAfter(validEnd)) {
                coupon.put("usable", false);
                coupon.put("disableReason", "已过期");
                continue;
            }

            // 检查使用门槛
            Object minAmountObj = coupon.get("coupon_min_amount");
            BigDecimal minAmount = BigDecimal.ZERO;
            if (minAmountObj instanceof Number) {
                minAmount = new BigDecimal(minAmountObj.toString());
            }

            if (orderAmount != null && minAmount.compareTo(BigDecimal.ZERO) > 0) {
                if (orderAmount.compareTo(minAmount) < 0) {
                    coupon.put("usable", false);
                    coupon.put("disableReason", "满" + minAmount + "元可用");
                } else {
                    coupon.put("usable", true);
                    coupon.put("disableReason", "");
                }
            } else {
                coupon.put("usable", true);
                coupon.put("disableReason", "");
            }
        }

        // 排序：可用的排前面，按优惠力度排序
        coupons.sort((a, b) -> {
            boolean aUsable = Boolean.TRUE.equals(a.get("usable"));
            boolean bUsable = Boolean.TRUE.equals(b.get("usable"));
            if (aUsable && !bUsable)
                return -1;
            if (!aUsable && bUsable)
                return 1;

            Object aAmount = a.get("coupon_amount");
            Object bAmount = b.get("coupon_amount");
            BigDecimal aVal = aAmount instanceof Number ? new BigDecimal(aAmount.toString()) : BigDecimal.ZERO;
            BigDecimal bVal = bAmount instanceof Number ? new BigDecimal(bAmount.toString()) : BigDecimal.ZERO;
            return bVal.compareTo(aVal);
        });

        return coupons;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> useCoupon(Long userCouponId, String orderId) {
        Map<String, Object> result = new HashMap<>();

        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null) {
            result.put("success", false);
            result.put("message", "优惠券不存在");
            return result;
        }

        if (userCoupon.getStatus() != 1) {
            result.put("success", false);
            result.put("message", "优惠券已使用或已过期");
            return result;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(userCoupon.getValidStart())) {
            result.put("success", false);
            result.put("message", "优惠券尚未生效");
            return result;
        }
        if (now.isAfter(userCoupon.getValidEnd())) {
            result.put("success", false);
            result.put("message", "优惠券已过期");
            return result;
        }

        userCoupon.setStatus(2);
        userCoupon.setUseTime(now);
        userCoupon.setOrderNo(orderId);
        userCoupon.setUpdateTime(now);

        userCouponMapper.updateById(userCoupon);

        result.put("success", true);
        result.put("message", "优惠券使用成功");
        return result;
    }

    @Override
    public Map<String, Object> getUserCouponStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        List<Map<String, Object>> allCoupons = userCouponMapper.selectByUserId(userId);

        int unusedCount = 0;
        int usedCount = 0;
        int expiredCount = 0;

        LocalDateTime now = LocalDateTime.now();
        for (Map<String, Object> uc : allCoupons) {
            Object statusObj = uc.get("status");
            int status = 0;
            if (statusObj instanceof Integer) {
                status = (Integer) statusObj;
            } else if (statusObj instanceof Boolean) {
                status = (Boolean) statusObj ? 1 : 0;
            } else if (statusObj instanceof Number) {
                status = ((Number) statusObj).intValue();
            }
            if (status == 0)
                continue;

            if (status == 1) {
                Object validEndObj = uc.get("valid_end");
                LocalDateTime validEnd = null;
                if (validEndObj instanceof LocalDateTime) {
                    validEnd = (LocalDateTime) validEndObj;
                }
                if (validEnd != null && now.isAfter(validEnd)) {
                    expiredCount++;
                } else {
                    unusedCount++;
                }
            } else if (status == 2) {
                usedCount++;
            } else if (status == 3) {
                expiredCount++;
            }
        }

        stats.put("total", allCoupons.size());
        stats.put("unused", unusedCount);
        stats.put("used", usedCount);
        stats.put("expired", expiredCount);

        return stats;
    }
}
