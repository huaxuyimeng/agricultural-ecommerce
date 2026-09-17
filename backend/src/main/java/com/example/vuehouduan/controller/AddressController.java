package com.example.vuehouduan.controller;

import com.example.vuehouduan.common.Result;
import com.example.vuehouduan.entity.Address;
import com.example.vuehouduan.service.AddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "收货地址管理")
@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @ApiOperation("获取收货地址列表")
    @GetMapping
    public Result<?> getAddressList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        List<Address> list = addressService.getAddressList(userId);
        return Result.success(list);
    }

    @ApiOperation("添加收货地址")
    @PostMapping
    public Result<?> addAddress(@RequestBody Address address, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        try {
            Address added = addressService.addAddress(userId, address);
            return Result.success(added);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("更新收货地址")
    @PutMapping("/{id}")
    public Result<?> updateAddress(@PathVariable Long id, @RequestBody Address address, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        try {
            Address updated = addressService.updateAddress(id, address, userId);
            return Result.success(updated);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("删除收货地址")
    @DeleteMapping("/{id}")
    public Result<?> deleteAddress(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        try {
            addressService.deleteAddress(id, userId);
            return Result.success("删除成功");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("设置默认收货地址")
    @PutMapping("/{id}/default")
    public Result<?> setDefaultAddress(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        try {
            addressService.setDefaultAddress(id, userId);
            return Result.success("设置成功");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
