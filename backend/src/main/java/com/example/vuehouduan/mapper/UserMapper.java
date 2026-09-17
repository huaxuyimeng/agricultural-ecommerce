package com.example.vuehouduan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuehouduan.entity.User;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * ============================================================
 * 用户数据访问层 - 对应 users 表
 * ============================================================
 *
 * 【MyBatis-Plus 说明】
 * - 继承 BaseMapper<User>：自动获得 CRUD 方法（selectById, insert, updateById, deleteById
 * 等）
 * - @Mapper：标记为 MyBatis Mapper 接口
 * - @Select/@Update/@Insert/@Delete：注解方式编写 SQL
 * - @Param：指定参数名，用于 XML 中引用
 *
 * 【方法说明】
 * - selectByUsername：根据用户名查询用户（登录时使用）
 * - selectByRole：根据角色查询用户列表
 * - selectActiveByRole：根据角色查询启用的用户（status=1）
 * - countByUsername：检查用户名是否存在（注册时使用）
 * - updateStatus：更新用户状态（启用/禁用）
 * - updatePassword：更新用户密码
 * - updateAccount：更新用户账户余额
 * - updateAvatar：更新用户头像
 * - updateLastLoginTime：更新最后登录时间
 * - updateToken：更新用户 Token（用于踢出登录）
 * - searchByKeyword：关键词搜索用户（用户名/姓名/手机号）
 * - countByRole：统计某角色的用户数量
 * - selectActiveMerchants：查询所有已启用的商家
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM users WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM users WHERE role = #{role}")
    List<User> selectByRole(@Param("role") String role);

    @Select("SELECT * FROM users WHERE role = #{role} AND status = 1")
    List<User> selectActiveByRole(@Param("role") String role);

    @Select("SELECT COUNT(*) FROM users WHERE username = #{username}")
    int countByUsername(@Param("username") String username);

    @Update("UPDATE users SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE users SET password = #{password}, update_time = NOW() WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Update("UPDATE users SET account = #{account}, update_time = NOW() WHERE id = #{id}")
    int updateAccount(@Param("id") Long id, @Param("account") BigDecimal account);

    @Update("UPDATE users SET avatar = #{avatar}, update_time = NOW() WHERE id = #{id}")
    int updateAvatar(@Param("id") Long id, @Param("avatar") String avatar);

    @Update("UPDATE users SET last_login_time = NOW() WHERE id = #{id}")
    int updateLastLoginTime(@Param("id") Long id);

    @Update("UPDATE users SET token = #{token} WHERE id = #{id}")
    int updateToken(@Param("id") Long id, @Param("token") String token);

    @Select("SELECT * FROM users WHERE username LIKE CONCAT('%', #{keyword}, '%') OR name LIKE CONCAT('%', #{keyword}, '%') OR phone LIKE CONCAT('%', #{keyword}, '%')")
    List<User> searchByKeyword(@Param("keyword") String keyword);

    @Select("SELECT COUNT(*) FROM users WHERE role = #{role}")
    int countByRole(@Param("role") String role);

    @Select("SELECT * FROM users WHERE role = 'MERCHANT' AND status = 1")
    List<User> selectActiveMerchants();
}