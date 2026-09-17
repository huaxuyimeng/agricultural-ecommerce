package com.example.vuehouduan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vuehouduan.entity.Order;
import com.example.vuehouduan.entity.OrderProduct;
import com.example.vuehouduan.entity.ProductReview;
import com.example.vuehouduan.entity.Product;
import com.example.vuehouduan.entity.User;
import com.example.vuehouduan.mapper.OrderMapper;
import com.example.vuehouduan.mapper.OrderProductMapper;
import com.example.vuehouduan.mapper.ProductMapper;
import com.example.vuehouduan.mapper.ProductReviewMapper;
import com.example.vuehouduan.mapper.UserMapper;
import com.example.vuehouduan.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================
 * 评价服务实现 - 商品评价管理
 * ============================================================
 *
 * 【前后端评价流程】
 *
 * 1. 添加评价：
 * 前端订单详情页 → 点击"评价"按钮
 * 前端 POST /api/reviews
 * Body: { orderId: "ORD123...", rating: 5, content: "商品很好", images: "url1,url2"
 * }
 * 后端 → 验证订单存在 → 获取订单商品信息 → 创建评价记录 → 商品评价数+1
 * 返回 → 成功消息
 *
 * 2. 查看商品评价：
 * 前端 GET /api/reviews/product/{productId}?page=1&pageSize=10
 * 后端 → 根据productId查询 → 只返回APPROVED状态的评价 → 计算平均分
 * 返回 → { list: [...], total: 100, averageRating: 4.5 }
 *
 * 3. 商家回复评价：
 * 前端 PUT /api/reviews/{id}/reply
 * Body: { replyContent: "感谢您的支持" }
 * 后端 → 更新回复内容和回复时间
 *
 * 【数据表关系】
 * product_review 表：评价记录（product_id, user_id, order_id, rating, content）
 * order 表：订单表（验证用户是否购买过该商品）
 * product 表：商品表（评价数同步更新）
 */
@Service
public class ReviewServiceImpl extends ServiceImpl<ProductReviewMapper, ProductReview> implements ReviewService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderProductMapper orderProductMapper;

    /**
     * 获取商品评价列表（分页）
     * 查询商品所有APPROVED状态的评价 → 填充用户信息 → 计算平均分
     */
    @Override
    public Map<String, Object> getProductReviews(Long productId, Integer page, Integer pageSize) {
        Page<ProductReview> pageObj = new Page<>(page, pageSize);
        QueryWrapper<ProductReview> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId).eq("status", "APPROVED").orderByDesc("create_time");
        Page<ProductReview> result = baseMapper.selectPage(pageObj, wrapper);

        for (ProductReview r : result.getRecords()) {
            User user = userMapper.selectById(r.getUserId());
            if (user != null) {
                r.setUserName(user.getName());
                r.setUserAvatar(user.getAvatar());
            }
        }

        QueryWrapper<ProductReview> countWrapper = new QueryWrapper<>();
        countWrapper.eq("product_id", productId).eq("status", "APPROVED");
        long total = baseMapper.selectCount(countWrapper);

        BigDecimal avgRating = BigDecimal.ZERO;
        List<ProductReview> all = baseMapper.selectList(countWrapper);
        if (!all.isEmpty()) {
            int sum = all.stream().mapToInt(ProductReview::getRating).sum();
            avgRating = new BigDecimal(sum).divide(new BigDecimal(all.size()), 1, RoundingMode.HALF_UP);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", total);
        map.put("averageRating", avgRating);
        return map;
    }

    /**
     * 添加商品评价
     *
     * 【前后端数据流】
     * 前端 POST /api/reviews
     * Body: { orderId: "ORD123...", rating: 5, content: "商品很好", images: "url1,url2"
     * }
     * 后端 → 验证订单存在 → 获取订单商品信息 → 创建评价记录 → 商品评价数+1
     */
    @Override
    public void addReview(Long userId, String orderId, Integer rating, String content, String images) {
        // Find the order and get product info
        QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
        orderWrapper.eq("order_id", orderId);
        Order order = orderMapper.selectOne(orderWrapper);

        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // Get the first product from the order for the review
        QueryWrapper<OrderProduct> productWrapper = new QueryWrapper<>();
        productWrapper.eq("order_id", orderId).last("LIMIT 1");
        OrderProduct orderProduct = orderProductMapper.selectOne(productWrapper);

        ProductReview review = new ProductReview();
        if (orderProduct != null) {
            review.setProductId(orderProduct.getProductId());
        }
        review.setUserId(userId);
        review.setOrderId(orderId);
        review.setRating(rating);
        review.setContent(content);
        review.setImages(images);
        review.setIsAnonymous(false);
        review.setStatus("APPROVED");
        review.setCreateTime(LocalDateTime.now());
        baseMapper.insert(review);

        // Update product review count
        if (orderProduct != null) {
            Product product = productMapper.selectById(orderProduct.getProductId());
            if (product != null) {
                product.setReviews(product.getReviews() == null ? 1 : product.getReviews() + 1);
                productMapper.updateById(product);
            }
        }
    }

    /**
     * 获取评价列表（管理员端，分页）
     * 查询所有评价 → 填充用户信息 → 分页返回
     */
    @Override
    public Map<String, Object> getReviewPage(Integer page, Integer pageSize) {
        Page<ProductReview> pageObj = new Page<>(page, pageSize);
        QueryWrapper<ProductReview> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        Page<ProductReview> result = baseMapper.selectPage(pageObj, wrapper);

        for (ProductReview r : result.getRecords()) {
            User user = userMapper.selectById(r.getUserId());
            if (user != null) {
                r.setUserName(user.getName());
                r.setUserAvatar(user.getAvatar());
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        return map;
    }

    /**
     * 获取商品评价统计信息
     * 计算总评价数和平均评分
     */
    @Override
    public Map<String, Object> getProductReviewStats(Long productId) {
        QueryWrapper<ProductReview> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId).eq("status", "APPROVED");
        List<ProductReview> all = baseMapper.selectList(wrapper);

        long total = all.size();
        BigDecimal avgRating = BigDecimal.ZERO;
        if (!all.isEmpty()) {
            int sum = all.stream().mapToInt(ProductReview::getRating).sum();
            avgRating = new BigDecimal(sum).divide(new BigDecimal(all.size()), 1, RoundingMode.HALF_UP);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("averageRating", avgRating);
        return stats;
    }

    @Override
    public void replyReview(Long id, String replyContent) {
        ProductReview review = baseMapper.selectById(id);
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }
        review.setReplyContent(replyContent);
        review.setReplyTime(LocalDateTime.now());
        baseMapper.updateById(review);
    }

    @Override
    public void deleteReview(Long id) {
        baseMapper.deleteById(id);
    }
}
