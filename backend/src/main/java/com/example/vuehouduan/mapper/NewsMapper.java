package com.example.vuehouduan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuehouduan.entity.News;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ============================================================
 * 新闻数据访问层 - 对应 news 表
 * ============================================================
 *
 * 【MyBatis-Plus 说明】
 * - 继承 BaseMapper<News>：自动获得 CRUD 方法
 * - @Mapper：标记为 MyBatis Mapper 接口
 * - @Select/@Update：注解方式编写 SQL
 *
 * 【方法说明】
 * - selectByCategory：按分类查询新闻（按时间降序）
 * - incrementViews：浏览量+1
 * - incrementLikes：点赞数+1
 * - incrementShares：分享数+1
 * - searchByKeyword：关键词搜索新闻（标题/内容）
 * - selectTopViews：查询浏览量最高的新闻
 * - selectLatest：查询最新新闻
 */
@Mapper
public interface NewsMapper extends BaseMapper<News> {

    @Select("SELECT * FROM news WHERE category = #{category} ORDER BY create_time DESC")
    List<News> selectByCategory(@Param("category") String category);

    @Update("UPDATE news SET views = COALESCE(views, 0) + 1 WHERE id = #{id}")
    int incrementViews(@Param("id") Long id);

    @Update("UPDATE news SET likes = COALESCE(likes, 0) + 1 WHERE id = #{id}")
    int incrementLikes(@Param("id") Long id);

    @Update("UPDATE news SET shares = COALESCE(shares, 0) + 1 WHERE id = #{id}")
    int incrementShares(@Param("id") Long id);

    @Select("SELECT * FROM news WHERE title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%') ORDER BY create_time DESC")
    List<News> searchByKeyword(@Param("keyword") String keyword);

    @Select("SELECT * FROM news ORDER BY views DESC LIMIT #{limit}")
    List<News> selectTopViews(@Param("limit") Integer limit);

    @Select("SELECT * FROM news ORDER BY create_time DESC LIMIT #{limit}")
    List<News> selectLatest(@Param("limit") Integer limit);
}