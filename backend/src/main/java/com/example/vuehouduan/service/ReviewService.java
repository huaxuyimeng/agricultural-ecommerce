package com.example.vuehouduan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.vuehouduan.entity.ProductReview;
import java.util.Map;

public interface ReviewService extends IService<ProductReview> {
    Map<String, Object> getProductReviews(Long productId, Integer page, Integer pageSize);
    void addReview(Long userId, String orderId, Integer rating, String content, String images);
    Map<String, Object> getReviewPage(Integer page, Integer pageSize);
    Map<String, Object> getProductReviewStats(Long productId);
    void replyReview(Long id, String replyContent);
    void deleteReview(Long id);
}
