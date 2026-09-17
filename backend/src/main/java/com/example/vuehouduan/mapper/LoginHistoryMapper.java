package com.example.vuehouduan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuehouduan.entity.LoginHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ============================================================
 * 登录历史数据访问层 - 对应 login_history 表
 * ============================================================
 *
 * 【MyBatis-Plus 说明】
 * - 继承 BaseMapper<LoginHistory>：自动获得 CRUD 方法
 * - @Mapper：标记为 MyBatis Mapper 接口
 * - @Select/@Update/@Delete：注解方式编写 SQL
 *
 * 【方法说明】
 * - selectByUserId：查询用户的登录历史（按时间降序）
 * - selectLatestByUserId：查询用户最近一次登录记录
 * - recordLogout：记录登出时间并计算在线时长（秒）
 * - countByUserId：统计用户的登录次数
 * - countByStatus：统计指定登录状态的记录数（成功/失败）
 * - deleteBeforeDate：清理指定日期之前的登录记录（数据清理）
 */
@Mapper
public interface LoginHistoryMapper extends BaseMapper<LoginHistory> {

    @Select("SELECT * FROM login_history WHERE user_id = #{userId} ORDER BY login_time DESC")
    List<LoginHistory> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM login_history WHERE user_id = #{userId} ORDER BY login_time DESC LIMIT 1")
    LoginHistory selectLatestByUserId(@Param("userId") Long userId);

    @Update("UPDATE login_history SET logout_time = NOW(), duration = TIMESTAMPDIFF(SECOND, login_time, NOW()) WHERE id = #{id}")
    int recordLogout(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM login_history WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM login_history WHERE login_status = #{status}")
    int countByStatus(@Param("status") String status);

    @Delete("DELETE FROM login_history WHERE login_time < #{beforeDate}")
    int deleteBeforeDate(@Param("beforeDate") String beforeDate);
}