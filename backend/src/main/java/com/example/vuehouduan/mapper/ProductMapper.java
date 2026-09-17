package com.example.vuehouduan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuehouduan.entity.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ============================================================
 * 商品数据访问层 - 对应 products 表
 * ============================================================
 *
 * 【MyBatis-Plus 说明】
 * - 继承 BaseMapper<Product>：自动获得 CRUD 方法
 * - @Mapper：标记为 MyBatis Mapper 接口
 * - @Select/@Update：注解方式编写 SQL
 *
 * 【方法说明】
 * - selectByCategory：按分类查询已审核的商品
 * - selectByMerchantId：查询商家的所有商品
 * - selectByStatus：按状态查询商品
 * - selectByMerchantIdAndStatus：查询商家指定状态的商品
 * - searchByKeyword：关键词搜索商品（名称/描述）
 * - selectHotProducts：查询热门商品（按销量降序）
 * - selectNewProducts：查询新品（按创建时间降序）
 * - selectRecommendedProducts：查询推荐商品（按销量降序）
 * - updateStock：更新库存（delta 可正可负）
 * - incrementViews：浏览量+1
 * - incrementSales：销量增加指定数量
 * - incrementFavorites：收藏数+1
 * - decrementFavorites：收藏数-1（仅当收藏数>0时）
 * - incrementReviews：评价数+1
 * - updateStatus：更新商品状态
 * - approveProduct：审核通过商品（同时设置审核时间）
 * - countByMerchantId：统计商家的商品数量
 * - countByStatus：统计指定状态的商品数量
 * - selectTopSales：查询销量最高的商品
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Select("SELECT * FROM products WHERE category = #{category} AND status = 'approved' ORDER BY create_time DESC")
    List<Product> selectByCategory(@Param("category") String category);

    @Select("SELECT * FROM products WHERE merchant_id = #{merchantId} ORDER BY create_time DESC")
    List<Product> selectByMerchantId(@Param("merchantId") Long merchantId);

    @Select("SELECT * FROM products WHERE status = #{status} ORDER BY create_time DESC")
    List<Product> selectByStatus(@Param("status") String status);

    @Select("SELECT * FROM products WHERE merchant_id = #{merchantId} AND status = #{status} ORDER BY create_time DESC")
    List<Product> selectByMerchantIdAndStatus(@Param("merchantId") Long merchantId, @Param("status") String status);

    @Select("SELECT * FROM products WHERE (name LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%')) AND status = 'approved' ORDER BY sales DESC")
    List<Product> searchByKeyword(@Param("keyword") String keyword);

    @Select("SELECT * FROM products WHERE is_hot = 1 AND status = 'approved' ORDER BY sales DESC LIMIT #{limit}")
    List<Product> selectHotProducts(@Param("limit") Integer limit);

    @Select("SELECT * FROM products WHERE is_new = 1 AND status = 'approved' ORDER BY create_time DESC LIMIT #{limit}")
    List<Product> selectNewProducts(@Param("limit") Integer limit);

    @Select("SELECT * FROM products WHERE is_recommend = 1 AND status = 'approved' ORDER BY sales DESC LIMIT #{limit}")
    List<Product> selectRecommendedProducts(@Param("limit") Integer limit);

    @Update("UPDATE products SET stock = stock + #{delta} WHERE id = #{id}")
    int updateStock(@Param("id") Long id, @Param("delta") Integer delta);

    @Update("UPDATE products SET views = COALESCE(views, 0) + 1 WHERE id = #{id}")
    int incrementViews(@Param("id") Long id);

    @Update("UPDATE products SET sales = COALESCE(sales, 0) + #{count} WHERE id = #{id}")
    int incrementSales(@Param("id") Long id, @Param("count") Integer count);

    @Update("UPDATE products SET favorites = COALESCE(favorites, 0) + 1 WHERE id = #{id}")
    int incrementFavorites(@Param("id") Long id);

    @Update("UPDATE products SET favorites = COALESCE(favorites, 0) - 1 WHERE id = #{id} AND favorites > 0")
    int decrementFavorites(@Param("id") Long id);

    @Update("UPDATE products SET reviews = COALESCE(reviews, 0) + 1 WHERE id = #{id}")
    int incrementReviews(@Param("id") Long id);

    @Update("UPDATE products SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE products SET status = 'approved', approve_time = NOW() WHERE id = #{id}")
    int approveProduct(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM products WHERE merchant_id = #{merchantId}")
    int countByMerchantId(@Param("merchantId") Long merchantId);

    @Select("SELECT COUNT(*) FROM products WHERE status = #{status}")
    int countByStatus(@Param("status") String status);

    @Select("SELECT * FROM products WHERE status = 'approved' ORDER BY sales DESC LIMIT #{limit}")
    List<Product> selectTopSales(@Param("limit") Integer limit);
}