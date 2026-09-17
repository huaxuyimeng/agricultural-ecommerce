package com.example.vuehouduan.controller;

import com.example.vuehouduan.common.Result;
import com.example.vuehouduan.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api(tags = "商家订单管理")
@RestController
@RequestMapping("/api/merchant/orders")
public class MerchantOrderController {

    @Autowired
    private OrderService orderService;

    @ApiOperation("获取商家订单列表")
    @GetMapping
    public Result<?> getMerchantOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Map<String, Object> result = orderService.getMerchantOrders(merchantId, page, pageSize, status);
        return Result.success(result);
    }

    @ApiOperation("商家发货")
    @PutMapping("/{id}/ship")
    public Result<?> shipOrder(@PathVariable Long id, @RequestBody Map<String, String> params, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        String expressCompany = params.get("expressCompany");
        String expressNo = params.get("expressNo");
        try {
            orderService.shipOrder(id, merchantId, expressCompany, expressNo);
            return Result.success("发货成功");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
