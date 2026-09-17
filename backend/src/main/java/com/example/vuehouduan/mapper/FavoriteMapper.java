package com.example.vuehouduan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuehouduan.entity.Favorite;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ============================================================
 * 收藏数据访问层 - 对应 favorites 表
 * ============================================================
 *
 * 【MyBatis-Plus 说明】
 * - 继承 BaseMapper<Favorite>：自动获得 CRUD 方法
 * - @Mapper：标记为 MyBatis Mapper 接口
 * - @Select/@Delete：注解方式编写 SQL
 *
 * 【方法说明】
 * - selectByUserId：查询用户的所有收藏（按时间降序）
 * - selectByUserIdAndType：查询用户指定类型的收藏（商品/商家）
 * - selectByUserIdAndTargetId：查询用户是否收藏了指定目标
 * - deleteByUserIdAndTargetId：取消收藏
 * - countByUserId：统计用户收藏总数
 * - countByUserIdAndType：统计用户指定类型的收藏数
 * - countByProductId：统计商品的收藏数
 * - existsByUserIdAndProductId：检查用户是否收藏了指定商品
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("SELECT * FROM favorites WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Favorite> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM favorites WHERE user_id = #{userId} AND type = #{type} ORDER BY create_time DESC")
    List<Favorite> selectByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);

    @Select("SELECT * FROM favorites WHERE user_id = #{userId} AND type = #{type} AND target_id = #{targetId}")
    Favorite selectByUserIdAndTargetId(@Param("userId") Long userId, @Param("type") String type,
            @Param("targetId") Long targetId);

    @Delete("DELETE FROM favorites WHERE user_id = #{userId} AND type = #{type} AND target_id = #{targetId}")
    int deleteByUserIdAndTargetId(@Param("userId") Long userId, @Param("type") String type,
            @Param("targetId") Long targetId);

    @Select("SELECT COUNT(*) FROM favorites WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM favorites WHERE user_id = #{userId} AND type = #{type}")
    int countByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);

    @Select("SELECT COUNT(*) FROM favorites WHERE target_id = #{targetId} AND type = 'product'")
    int countByProductId(@Param("targetId") Long targetId);

    @Select("SELECT EXISTS(SELECT 1 FROM favorites WHERE user_id = #{userId} AND type = 'product' AND target_id = #{targetId})")
    boolean existsByUserIdAndProductId(@Param("userId") Long userId, @Param("targetId") Long targetId);
}