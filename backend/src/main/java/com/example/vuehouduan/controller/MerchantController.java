package com.example.vuehouduan.controller;

import com.example.vuehouduan.common.Result;
import com.example.vuehouduan.service.MerchantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "商家管理")
@RestController
@RequestMapping("/api")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    @ApiOperation("获取商家列表")
    @GetMapping("/admin/merchants")
    public Result<?> getMerchantList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return Result.success(merchantService.getMerchantList(page, pageSize, status, keyword));
    }

    @ApiOperation("获取商家详情")
    @GetMapping("/merchants/{id}")
    public Result<?> getMerchantById(@PathVariable Long id) {
        return Result.success(merchantService.getMerchantById(id));
    }

    @ApiOperation("审核通过商家")
    @PutMapping("/admin/merchants/{id}/approve")
    public Result<?> approveMerchant(@PathVariable Long id) {
        try {
            merchantService.approveMerchant(id);
            return Result.success("审核通过");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("禁用商家")
    @PutMapping("/admin/merchants/{id}/disable")
    public Result<?> disableMerchant(@PathVariable Long id) {
        try {
            merchantService.disableMerchant(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
