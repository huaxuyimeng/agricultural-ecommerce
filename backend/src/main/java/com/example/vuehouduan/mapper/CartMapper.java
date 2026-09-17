package com.example.vuehouduan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuehouduan.entity.Cart;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ============================================================
 * 购物车数据访问层 - 对应 cart 表
 * ============================================================
 *
 * 【MyBatis-Plus 说明】
 * - 继承 BaseMapper<Cart>：自动获得 CRUD 方法
 * - @Mapper：标记为 MyBatis Mapper 接口
 * - @Select/@Update/@Delete：注解方式编写 SQL
 *
 * 【方法说明】
 * - selectByUserId：查询用户的购物车（按时间降序）
 * - selectByUserIdAndProductId：查询用户购物车中的指定商品
 * - deleteByUserId：清空用户购物车
 * - updateQuantity：更新商品数量（同时重新计算总价）
 * - updateSelected：更新商品勾选状态
 * - updateAllSelected：批量更新用户购物车的勾选状态
 * - countByUserId：统计用户购物车商品种类数
 * - sumQuantityByUserId：统计用户购物车商品总数量
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {

    @Select("SELECT * FROM cart WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Cart> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM cart WHERE user_id = #{userId} AND product_id = #{productId}")
    Cart selectByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    @Delete("DELETE FROM cart WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Update("UPDATE cart SET quantity = #{quantity}, total_price = price * #{quantity}, update_time = NOW() WHERE id = #{id}")
    int updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Update("UPDATE cart SET is_selected = #{isSelected}, update_time = NOW() WHERE id = #{id}")
    int updateSelected(@Param("id") Long id, @Param("isSelected") Boolean isSelected);

    @Update("UPDATE cart SET is_selected = #{isSelected}, update_time = NOW() WHERE user_id = #{userId}")
    int updateAllSelected(@Param("userId") Long userId, @Param("isSelected") Boolean isSelected);

    @Select("SELECT COUNT(*) FROM cart WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    @Select("SELECT COALESCE(SUM(quantity), 0) FROM cart WHERE user_id = #{userId}")
    int sumQuantityByUserId(@Param("userId") Long userId);
}