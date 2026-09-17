package com.example.vuehouduan.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.vuehouduan.common.Result;
import com.example.vuehouduan.entity.Coupon;
import com.example.vuehouduan.service.CouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 优惠券管理控制器
 */
@Api(tags = "优惠券管理")
@RestController
@RequestMapping("/api/coupon")
public class CouponController {

    @Autowired
    private CouponService couponService;

    /**
     * 获取所有可用的优惠券列表
     */
    @ApiOperation("获取可用优惠券列表")
    @GetMapping("/available")
    public Result<?> getAvailableCoupons() {
        List<Coupon> coupons = couponService.getAvailableCoupons();
        return Result.success(coupons);
    }

    /**
     * 获取优惠券列表（首页展示用）
     */
    @ApiOperation("获取优惠券列表")
    @GetMapping("/list")
    public Result<?> getCouponList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Map<String, Object>> coupons = couponService.getAvailableCouponsWithStatus(userId);
        return Result.success(coupons);
    }

    /**
     * 根据来源获取优惠券
     * source: 1-系统发放，2-活动领取，3-充值赠送，4-消费返券
     */
    @ApiOperation("根据来源获取优惠券")
    @GetMapping("/source/{source}")
    public Result<?> getCouponsBySource(@PathVariable Integer source) {
        List<Coupon> coupons = couponService.getCouponsBySource(source);
        return Result.success(coupons);
    }

    /**
     * 用户领取优惠券
     */
    @ApiOperation("领取优惠券")
    @PostMapping("/receive/{couponId}")
    public Result<?> receiveCoupon(@PathVariable Long couponId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }

        Map<String, Object> result = couponService.receiveCoupon(userId, couponId);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return Result.success(result);
        } else {
            return Result.error(result.get("message").toString());
        }
    }

    /**
     * 检查用户是否可以领取某优惠券
     */
    @ApiOperation("检查是否可领取")
    @GetMapping("/check/{couponId}")
    public Result<?> checkReceiveable(@PathVariable Long couponId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }

        Map<String, Object> result = couponService.checkReceiveable(userId, couponId);
        return Result.success(result);
    }

    /**
     * 管理员：分页查询所有优惠券
     */
    @ApiOperation("管理员分页查询优惠券")
    @GetMapping("/admin/page")
    public Result<?> adminPageCoupons(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {
        Page<Coupon> pageObj = new Page<>(pageNum, pageSize);
        QueryWrapper<Coupon> wrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        if (status != null && !status.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            switch (status) {
                case "active":
                    wrapper.le("start_time", now)
                            .ge("end_time", now)
                            .eq("status", 1);
                    break;
                case "expired":
                    wrapper.lt("end_time", now);
                    break;
                case "pending":
                    wrapper.gt("start_time", now);
                    break;
                case "exhausted":
                    wrapper.eq("status", 1)
                            .ne("total_num", -1)
                            .apply("received_num >= total_num");
                    break;
            }
        }
        wrapper.orderByDesc("create_time");
        Page<Coupon> result = couponService.page(pageObj, wrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        return Result.success(map);
    }

    /**
     * 管理员：创建优惠券
     */
    @ApiOperation("管理员创建优惠券")
    @PostMapping("/admin")
    public Result<?> adminCreateCoupon(@RequestBody Coupon coupon) {
        try {
            coupon.setCouponCode("CPN" + System.currentTimeMillis());
            coupon.setStatus(1);
            coupon.setReceivedNum(0);
            coupon.setUsedNum(0);
            coupon.setCreateTime(LocalDateTime.now());
            coupon.setUpdateTime(LocalDateTime.now());
            couponService.save(coupon);
            return Result.success(coupon);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 管理员：更新优惠券
     */
    @ApiOperation("管理员更新优惠券")
    @PutMapping("/admin/{id}")
    public Result<?> adminUpdateCoupon(@PathVariable Long id, @RequestBody Coupon coupon) {
        try {
            coupon.setId(id);
            coupon.setUpdateTime(LocalDateTime.now());
            couponService.updateById(coupon);
            return Result.success(coupon);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 管理员：删除优惠券
     */
    @ApiOperation("管理员删除优惠券")
    @DeleteMapping("/admin/{id}")
    public Result<?> adminDeleteCoupon(@PathVariable Long id) {
        try {
            couponService.removeById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }
}
