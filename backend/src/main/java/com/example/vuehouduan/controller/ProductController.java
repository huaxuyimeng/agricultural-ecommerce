package com.example.vuehouduan.controller;

import com.example.vuehouduan.common.Result;
import com.example.vuehouduan.entity.Product;
import com.example.vuehouduan.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Api(tags = "商品管理")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @ApiOperation("获取商品列表")
    @GetMapping
    public Result<?> getProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isHot,
            @RequestParam(required = false) Boolean isNew,
            @RequestParam(required = false) Boolean isRecommend,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        Map<String, Object> result = productService.getProductList(page, pageSize, category, keyword, isHot, isNew, isRecommend, merchantId, sortBy, sortOrder);
        return Result.success(result);
    }

    @ApiOperation("获取商品详情")
    @GetMapping("/{id}")
    public Result<?> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return Result.success(product);
    }

    @ApiOperation("搜索商品")
    @GetMapping("/search")
    public Result<?> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        List<Product> list = productService.searchProducts(keyword, category);
        return Result.success(list);
    }

    @ApiOperation("获取热门商品")
    @GetMapping("/hot")
    public Result<?> getHotProducts(@RequestParam(required = false) Integer limit) {
        List<Product> list = productService.getHotProducts(limit != null ? limit : 10);
        return Result.success(list);
    }

    @ApiOperation("获取新品商品")
    @GetMapping("/new")
    public Result<?> getNewProducts(@RequestParam(required = false) Integer limit) {
        List<Product> list = productService.getNewProducts(limit != null ? limit : 10);
        return Result.success(list);
    }

    @ApiOperation("获取推荐商品")
    @GetMapping("/recommended")
    public Result<?> getRecommendedProducts(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String category) {
        List<Product> list = productService.getRecommendedProducts(limit != null ? limit : 10, category);
        return Result.success(list);
    }

    @ApiOperation("获取商品分类")
    @GetMapping("/categories")
    public Result<?> getCategories() {
        List<Map<String, String>> categories = java.util.Arrays.asList(
            java.util.Map.of("value", "agricultural", "label", "农产品"),
            java.util.Map.of("value", "livestock", "label", "畜牧产品"),
            java.util.Map.of("value", "processed", "label", "加工产品")
        );
        return Result.success(categories);
    }

    @ApiOperation("创建商品")
    @PostMapping
    public Result<?> createProduct(@RequestBody Product product, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        try {
            Product created = productService.createProduct(product, merchantId);
            return Result.success(created);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("更新商品")
    @PutMapping("/{id}")
    public Result<?> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            Product updated = productService.updateProduct(id, product);
            return Result.success(updated);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("删除商品")
    @DeleteMapping("/{id}")
    public Result<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return Result.success("删除成功");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("审核通过商品")
    @PutMapping("/{id}/approve")
    public Result<?> approveProduct(@PathVariable Long id) {
        try {
            productService.approveProduct(id);
            return Result.success(productService.getById(id));
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("驳回商品")
    @PutMapping("/{id}/reject")
    public Result<?> rejectProduct(@PathVariable Long id) {
        try {
            productService.rejectProduct(id);
            return Result.success(productService.getById(id));
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("获取商品统计")
    @GetMapping("/stats")
    public Result<?> getProductStats() {
        return Result.success(productService.getProductStats());
    }

    @ApiOperation("获取待审核商品")
    @GetMapping("/pending")
    public Result<?> getPendingProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = productService.getPendingProducts(page, pageSize);
        return Result.success(result);
    }
}
