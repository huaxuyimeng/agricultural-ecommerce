package com.example.vuehouduan.controller;

import com.example.vuehouduan.common.Result;
import com.example.vuehouduan.entity.Product;
import com.example.vuehouduan.mapper.ProductMapper;
import com.example.vuehouduan.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

@Api(tags = "商家商品管理")
@RestController
@RequestMapping("/api/merchant/products")
public class MerchantProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductMapper productMapper;

    @ApiOperation("获取商家商品列表")
    @GetMapping
    public Result<?> getMerchantProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Map<String, Object> result = productService.getMerchantProducts(merchantId, page, pageSize, status);
        return Result.success(result);
    }

    @ApiOperation("提交商品审核")
    @PostMapping("/{id}/submit")
    public Result<?> submitProduct(@PathVariable Long id, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Product product = productMapper.selectById(id);
        if (product == null) {
            return Result.error(404, "商品不存在");
        }
        if (!product.getMerchantId().equals(merchantId)) {
            return Result.error(403, "无权操作");
        }
        product.setStatus("pending");
        product.setSubmitTime(LocalDateTime.now());
        productMapper.updateById(product);
        return Result.success("提交成功");
    }
}
