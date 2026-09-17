package com.example.vuehouduan.controller;

import com.example.vuehouduan.common.Result;
import com.example.vuehouduan.entity.Cart;
import com.example.vuehouduan.service.CartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Api(tags = "购物车管理")
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @ApiOperation("获取购物车列表")
    @GetMapping
    public Result<?> getCartList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Cart> list = cartService.getCartList(userId);
        return Result.success(list);
    }

    @ApiOperation("添加商品到购物车")
    @PostMapping
    public Result<?> addToCart(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long productId = Long.valueOf(params.get("productId").toString());
        Integer count = Integer.valueOf(params.get("count").toString());
        try {
            cartService.addToCart(userId, productId, count);
            return Result.success("添加成功");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("更新购物车商品数量")
    @PutMapping("/{id}")
    public Result<?> updateCartItem(@PathVariable Long id, @RequestBody Map<String, Integer> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Integer quantity = params.get("quantity");
        try {
            cartService.updateCartItem(id, userId, quantity);
            return Result.success("更新成功");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("删除购物车商品")
    @DeleteMapping("/{id}")
    public Result<?> removeCartItem(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            cartService.removeCartItem(id, userId);
            return Result.success("删除成功");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("清空购物车")
    @DeleteMapping("/clear")
    public Result<?> clearCart(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.clearCart(userId);
        return Result.success("清空成功");
    }
}
