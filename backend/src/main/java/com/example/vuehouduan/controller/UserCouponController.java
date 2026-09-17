package com.example.vuehouduan.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.vuehouduan.common.Result;
import com.example.vuehouduan.entity.UserCoupon;
import com.example.vuehouduan.mapper.UserCouponMapper;
import com.example.vuehouduan.service.UserCouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户优惠券管理控制器
 */
@Api(tags = "用户优惠券管理")
@RestController
@RequestMapping("/api/user-coupon")
public class UserCouponController {

    @Autowired
    private UserCouponService userCouponService;

    @Autowired
    private UserCouponMapper userCouponMapper;

    /**
     * 获取用户的所有优惠券
     */
    @ApiOperation("获取我的优惠券")
    @GetMapping("/my")
    public Result<?> getMyCoupons(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }

        List<Map<String, Object>> coupons = userCouponService.getUserCoupons(userId);
        return Result.success(coupons);
    }

    /**
     * 根据状态获取用户优惠券
     * status: 1-未使用，2-已使用，3-已过期
     */
    @ApiOperation("根据状态获取优惠券")
    @GetMapping("/my/status")
    public Result<?> getMyCouponsByStatus(@RequestParam Integer status, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }

        List<Map<String, Object>> coupons = userCouponService.getUserCouponsByStatus(userId, status);
        return Result.success(coupons);
    }

    /**
     * 获取用户可用的优惠券
     */
    @ApiOperation("获取可用优惠券")
    @GetMapping("/available")
    public Result<?> getAvailableCoupons(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }

        List<Map<String, Object>> coupons = userCouponService.getAvailableUserCoupons(userId);
        return Result.success(coupons);
    }

    /**
     * 获取结算时可用的优惠券（根据订单金额过滤）
     */
    @ApiOperation("获取结算可用优惠券")
    @GetMapping("/checkout")
    public Result<?> getCheckoutCoupons(@RequestParam(required = false) BigDecimal orderAmount,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }

        List<Map<String, Object>> coupons = userCouponService.getCheckoutCoupons(userId, orderAmount);
        return Result.success(coupons);
    }

    /**
     * 使用优惠券（核销）
     */
    @ApiOperation("使用优惠券")
    @PostMapping("/use/{userCouponId}")
    public Result<?> useCoupon(@PathVariable Long userCouponId, @RequestBody Map<String, String> params,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }

        String orderId = params.get("orderId");
        Map<String, Object> result = userCouponService.useCoupon(userCouponId, orderId);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return Result.success(result);
        } else {
            return Result.error(result.get("message").toString());
        }
    }

    /**
     * 获取用户优惠券统计信息
     */
    @ApiOperation("获取优惠券统计")
    @GetMapping("/stats")
    public Result<?> getCouponStats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }

        Map<String, Object> stats = userCouponService.getUserCouponStats(userId);
        return Result.success(stats);
    }

    /**
     * 管理员：获取优惠券使用历史
     */
    @ApiOperation("管理员获取优惠券使用历史")
    @GetMapping("/history")
    public Result<?> getCouponUsageHistory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String couponName,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        List<Map<String, Object>> allRecords = userCouponMapper.selectUsageHistory(username, couponName, startTime,
                endTime);
        int total = allRecords.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<Map<String, Object>> pageData = fromIndex < total ? allRecords.subList(fromIndex, toIndex)
                : new java.util.ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("list", pageData);
        map.put("total", total);
        return Result.success(map);
    }
}
