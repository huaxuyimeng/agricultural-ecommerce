package com.example.vuehouduan.controller;

import com.example.vuehouduan.common.Result;
import com.example.vuehouduan.service.ReviewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api(tags = "评价管理")
@RestController
@RequestMapping("/api")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @ApiOperation("获取商品评价")
    @GetMapping("/products/{id}/reviews")
    public Result<?> getProductReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = reviewService.getProductReviews(id, page, pageSize);
        return Result.success(result);
    }

    @ApiOperation("添加评价")
    @PostMapping("/orders/{orderId}/reviews")
    public Result<?> addReview(
            @PathVariable String orderId,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Integer rating = Integer.valueOf(params.get("rating").toString());
        String content = params.get("content").toString();
        String images = params.get("images") != null ? params.get("images").toString() : "[]";
        try {
            reviewService.addReview(userId, orderId, rating, content, images);
            return Result.success("评价成功");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("获取评价列表")
    @GetMapping("/reviews")
    public Result<?> getReviewPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = reviewService.getReviewPage(page, pageSize);
        return Result.success(result);
    }

    @ApiOperation("获取商品评价统计")
    @GetMapping("/products/{id}/review-stats")
    public Result<?> getProductReviewStats(@PathVariable Long id) {
        Map<String, Object> stats = reviewService.getProductReviewStats(id);
        return Result.success(stats);
    }

    @ApiOperation("回复评价")
    @PutMapping("/reviews/{id}/reply")
    public Result<?> replyReview(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String replyContent = params.get("replyContent");
        reviewService.replyReview(id, replyContent);
        return Result.success("回复成功");
    }

    @ApiOperation("删除评价")
    @DeleteMapping("/reviews/{id}")
    public Result<?> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return Result.success("删除成功");
    }
}
