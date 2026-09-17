package com.example.vuehouduan.controller;

import com.example.vuehouduan.common.Result;
import com.example.vuehouduan.service.FavoriteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Api(tags = "收藏管理")
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @ApiOperation("获取收藏列表")
    @GetMapping
    public Result<?> getFavorites(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Map<String, Object>> list = favoriteService.getFavorites(userId);
        return Result.success(list);
    }

    @ApiOperation("添加收藏")
    @PostMapping
    public Result<?> addFavorite(@RequestBody Map<String, Long> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long productId = params.get("productId");
        try {
            favoriteService.addFavorite(userId, productId);
            return Result.success("收藏成功");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("取消收藏")
    @DeleteMapping("/{id}")
    public Result<?> removeFavorite(@PathVariable Long id, @RequestParam(required = false) Long productId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            if (productId != null) {
                favoriteService.removeFavoriteByProductId(userId, productId);
            } else {
                favoriteService.removeFavorite(id, userId);
            }
            return Result.success("取消成功");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
