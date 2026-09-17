package com.example.vuehouduan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuehouduan.entity.OrderProduct;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ============================================================
 * 订单商品数据访问层 - 对应 order_products 表
 * ============================================================
 *
 * 【MyBatis-Plus 说明】
 * - 继承 BaseMapper<OrderProduct>：自动获得 CRUD 方法
 * - @Mapper：标记为 MyBatis Mapper 接口
 * - @Select/@Delete：注解方式编写 SQL
 *
 * 【方法说明】
 * - selectByOrderId：查询订单包含的商品列表
 * - deleteByOrderId：删除订单的所有商品（订单删除时级联）
 * - selectByProductId：查询包含指定商品的所有订单
 * - selectByOrderIdAndProductId：查询订单中的指定商品
 * - countByProductId：统计商品被购买的次数（用于销量统计）
 */
@Mapper
public interface OrderProductMapper extends BaseMapper<OrderProduct> {

    @Select("SELECT * FROM order_products WHERE order_id = #{orderId}")
    List<OrderProduct> selectByOrderId(@Param("orderId") String orderId);

    @Delete("DELETE FROM order_products WHERE order_id = #{orderId}")
    int deleteByOrderId(@Param("orderId") String orderId);

    @Select("SELECT * FROM order_products WHERE product_id = #{productId}")
    List<OrderProduct> selectByProductId(@Param("productId") Long productId);

    @Select("SELECT * FROM order_products WHERE order_id = #{orderId} AND product_id = #{productId}")
    OrderProduct selectByOrderIdAndProductId(@Param("orderId") String orderId, @Param("productId") Long productId);

    @Select("SELECT COUNT(*) FROM order_products WHERE product_id = #{productId}")
    int countByProductId(@Param("productId") Long productId);
}