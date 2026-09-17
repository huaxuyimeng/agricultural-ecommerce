package com.example.vuehouduan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuehouduan.entity.Address;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ============================================================
 * 地址数据访问层 - 对应 addresses 表
 * ============================================================
 *
 * 【MyBatis-Plus 说明】
 * - 继承 BaseMapper<Address>：自动获得 CRUD 方法
 * - @Mapper：标记为 MyBatis Mapper 接口
 * - @Select/@Update：注解方式编写 SQL
 *
 * 【方法说明】
 * - selectByUserId：查询用户的地址列表（默认地址优先，按时间降序）
 * - selectDefaultByUserId：查询用户的默认地址
 * - clearDefaultByUserId：取消用户所有地址的默认状态
 * - setDefault：设置指定地址为默认地址
 * - countByUserId：统计用户的地址数量
 */
@Mapper
public interface AddressMapper extends BaseMapper<Address> {

    @Select("SELECT * FROM addresses WHERE user_id = #{userId} ORDER BY is_default DESC, create_time DESC")
    List<Address> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM addresses WHERE user_id = #{userId} AND is_default = 1 LIMIT 1")
    Address selectDefaultByUserId(@Param("userId") Long userId);

    @Update("UPDATE addresses SET is_default = 0 WHERE user_id = #{userId}")
    int clearDefaultByUserId(@Param("userId") Long userId);

    @Update("UPDATE addresses SET is_default = 1 WHERE id = #{id}")
    int setDefault(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM addresses WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);
}