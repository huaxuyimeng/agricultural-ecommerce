package com.example.vuehouduan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuehouduan.entity.ProductReview;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * ============================================================
 * 商品评价数据访问层 - 对应 product_reviews 表
 * ============================================================
 *
 * 【MyBatis-Plus 说明】
 * - 继承 BaseMapper<ProductReview>：自动获得 CRUD 方法
 * - @Mapper：标记为 MyBatis Mapper 接口
 * - @Select/@Update/@Delete：注解方式编写 SQL
 *
 * 【方法说明】
 * - selectByProductId：查询商品的所有评价（按时间降序）
 * - selectByProductIdAndStatus：查询商品指定状态的评价
 * - selectByUserId：查询用户的所有评价
 * - selectByOrderId：查询指定订单的评价
 * - countByProductId：统计商品的评价数量
 * - countByProductIdAndStatus：统计商品指定状态的评价数量
 * - avgRatingByProductId：计算商品的平均评分（仅统计已审核评价）
 * - updateStatus：更新评价状态
 * - replyReview：商家回复评价（同时设置回复时间）
 * - deleteByProductId：删除商品的所有评价（商品删除时级联）
 */
@Mapper
public interface ProductReviewMapper extends BaseMapper<ProductReview> {

    @Select("SELECT * FROM product_reviews WHERE product_id = #{productId} ORDER BY create_time DESC")
    List<ProductReview> selectByProductId(@Param("productId") Long productId);

    @Select("SELECT * FROM product_reviews WHERE product_id = #{productId} AND status = #{status} ORDER BY create_time DESC")
    List<ProductReview> selectByProductIdAndStatus(@Param("productId") Long productId, @Param("status") String status);

    @Select("SELECT * FROM product_reviews WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<ProductReview> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM product_reviews WHERE order_id = #{orderId}")
    List<ProductReview> selectByOrderId(@Param("orderId") String orderId);

    @Select("SELECT COUNT(*) FROM product_reviews WHERE product_id = #{productId}")
    int countByProductId(@Param("productId") Long productId);

    @Select("SELECT COUNT(*) FROM product_reviews WHERE product_id = #{productId} AND status = #{status}")
    int countByProductIdAndStatus(@Param("productId") Long productId, @Param("status") String status);

    @Select("SELECT COALESCE(AVG(rating), 0) FROM product_reviews WHERE product_id = #{productId} AND status = 'APPROVED'")
    BigDecimal avgRatingByProductId(@Param("productId") Long productId);

    @Update("UPDATE product_reviews SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE product_reviews SET reply_content = #{replyContent}, reply_time = NOW() WHERE id = #{id}")
    int replyReview(@Param("id") Long id, @Param("replyContent") String replyContent);

    @Delete("DELETE FROM product_reviews WHERE product_id = #{productId}")
    int deleteByProductId(@Param("productId") Long productId);
}