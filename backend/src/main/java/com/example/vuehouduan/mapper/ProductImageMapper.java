package com.example.vuehouduan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuehouduan.entity.ProductImage;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ============================================================
 * 商品图片数据访问层 - 对应 product_images 表
 * ============================================================
 *
 * 【MyBatis-Plus 说明】
 * - 继承 BaseMapper<ProductImage>：自动获得 CRUD 方法
 * - @Mapper：标记为 MyBatis Mapper 接口
 * - @Select/@Update/@Delete：注解方式编写 SQL
 *
 * 【方法说明】
 * - selectByProductId：查询商品的所有图片（按排序字段升序）
 * - selectMainByProductId：查询商品的主图
 * - deleteByProductId：删除商品的所有图片（商品删除时级联）
 * - clearMainImage：取消商品的所有主图标记
 * - setMainImage：设置指定图片为主图
 * - countByProductId：统计商品的图片数量
 */
@Mapper
public interface ProductImageMapper extends BaseMapper<ProductImage> {

    @Select("SELECT * FROM product_images WHERE product_id = #{productId} ORDER BY sort_order ASC")
    List<ProductImage> selectByProductId(@Param("productId") Long productId);

    @Select("SELECT * FROM product_images WHERE product_id = #{productId} AND is_main = 1 LIMIT 1")
    ProductImage selectMainByProductId(@Param("productId") Long productId);

    @Delete("DELETE FROM product_images WHERE product_id = #{productId}")
    int deleteByProductId(@Param("productId") Long productId);

    @Update("UPDATE product_images SET is_main = 0 WHERE product_id = #{productId}")
    int clearMainImage(@Param("productId") Long productId);

    @Update("UPDATE product_images SET is_main = 1 WHERE id = #{id}")
    int setMainImage(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM product_images WHERE product_id = #{productId}")
    int countByProductId(@Param("productId") Long productId);
}