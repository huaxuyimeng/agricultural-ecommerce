package com.example.vuehouduan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuehouduan.entity.SystemLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ============================================================
 * 系统日志数据访问层 - 对应 system_logs 表
 * ============================================================
 *
 * 【MyBatis-Plus 说明】
 * - 继承 BaseMapper<SystemLog>：自动获得 CRUD 方法
 * - @Mapper：标记为 MyBatis Mapper 接口
 * - @Select/@Delete：注解方式编写 SQL
 *
 * 【方法说明】
 * - selectByLevel：按日志级别查询日志（按时间降序）
 * - selectByCategory：按日志分类查询日志（按时间降序）
 * - searchByKeyword：关键词搜索日志（消息内容）
 * - selectByUserId：查询用户的操作日志（按时间降序）
 * - countByLevel：统计指定级别的日志数量
 * - deleteBeforeDate：清理指定日期之前的日志（数据清理）
 * - selectLatest：查询最新的日志记录
 */
@Mapper
public interface SystemLogMapper extends BaseMapper<SystemLog> {

    @Select("SELECT * FROM system_logs WHERE level = #{level} ORDER BY create_time DESC")
    List<SystemLog> selectByLevel(@Param("level") String level);

    @Select("SELECT * FROM system_logs WHERE category = #{category} ORDER BY create_time DESC")
    List<SystemLog> selectByCategory(@Param("category") String category);

    @Select("SELECT * FROM system_logs WHERE message LIKE CONCAT('%', #{keyword}, '%') ORDER BY create_time DESC")
    List<SystemLog> searchByKeyword(@Param("keyword") String keyword);

    @Select("SELECT * FROM system_logs WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<SystemLog> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM system_logs WHERE level = #{level}")
    int countByLevel(@Param("level") String level);

    @Delete("DELETE FROM system_logs WHERE create_time < #{beforeDate}")
    int deleteBeforeDate(@Param("beforeDate") String beforeDate);

    @Select("SELECT * FROM system_logs ORDER BY create_time DESC LIMIT #{limit}")
    List<SystemLog> selectLatest(@Param("limit") Integer limit);
}