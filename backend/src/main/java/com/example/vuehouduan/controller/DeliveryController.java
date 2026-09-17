package com.example.vuehouduan.controller;

import com.example.vuehouduan.common.Result;
import com.example.vuehouduan.service.DeliveryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "物流管理")
@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @ApiOperation("根据订单号查询物流信息")
    @GetMapping("/tracking/{orderId}")
    public Result<?> getLogistics(@PathVariable String orderId) {
        return Result.success(deliveryService.getLogisticsByOrderId(orderId));
    }
}
