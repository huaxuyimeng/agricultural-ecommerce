package com.example.vuehouduan.controller;

import com.example.vuehouduan.common.Result;
import com.example.vuehouduan.entity.AfterSales;
import com.example.vuehouduan.service.AfterSalesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api(tags = "售后管理")
@RestController
@RequestMapping("/api/after-sales")
public class AfterSalesController {

    @Autowired
    private AfterSalesService afterSalesService;

    @ApiOperation("创建售后申请")
    @PostMapping
    public Result<?> createAfterSales(@RequestBody Map<String, Object> data, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            AfterSales afterSales = afterSalesService.createAfterSales(userId, data);
            return Result.success(afterSales);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("获取用户售后列表")
    @GetMapping
    public Result<?> getUserAfterSales(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(afterSalesService.getUserAfterSales(userId, page, pageSize, status));
    }

    @ApiOperation("获取售后详情")
    @GetMapping("/{id}")
    public Result<?> getAfterSalesById(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            AfterSales afterSales = afterSalesService.getAfterSalesById(id, userId);
            return Result.success(afterSales);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("商家审核通过售后")
    @PutMapping("/{id}/approve")
    public Result<?> approveAfterSales(@PathVariable Long id, @RequestBody(required = false) Map<String, String> params, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        String remark = params != null ? params.get("remark") : null;
        try {
            afterSalesService.approveAfterSales(id, merchantId, remark);
            return Result.success("审核通过");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("商家拒绝售后")
    @PutMapping("/{id}/reject")
    public Result<?> rejectAfterSales(@PathVariable Long id, @RequestBody(required = false) Map<String, String> params, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        String remark = params != null ? params.get("remark") : null;
        try {
            afterSalesService.rejectAfterSales(id, merchantId, remark);
            return Result.success("已拒绝");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("商家完成售后")
    @PutMapping("/{id}/complete")
    public Result<?> completeAfterSales(@PathVariable Long id, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        try {
            afterSalesService.completeAfterSales(id, merchantId);
            return Result.success("售后已完成");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("用户取消售后申请")
    @PutMapping("/{id}/cancel")
    public Result<?> cancelAfterSales(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            afterSalesService.cancelAfterSales(id, userId);
            return Result.success("已取消");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("用户填写退货物流信息")
    @PutMapping("/{id}/express")
    public Result<?> updateExpress(@PathVariable Long id, @RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String expressCompany = params.get("expressCompany");
        String expressNo = params.get("expressNo");
        try {
            afterSalesService.updateExpress(id, userId, expressCompany, expressNo);
            return Result.success("物流信息已更新");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("商家获取售后列表")
    @GetMapping("/merchant")
    public Result<?> getMerchantAfterSales(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        return Result.success(afterSalesService.getMerchantAfterSales(merchantId, page, pageSize, status));
    }
}
