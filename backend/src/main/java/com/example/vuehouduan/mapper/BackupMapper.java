package com.example.vuehouduan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuehouduan.entity.Backup;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ============================================================
 * 数据备份数据访问层 - 对应 backups 表
 * ============================================================
 *
 * 【MyBatis-Plus 说明】
 * - 继承 BaseMapper<Backup>：自动获得 CRUD 方法
 * - @Mapper：标记为 MyBatis Mapper 接口
 * - @Select/@Update：注解方式编写 SQL
 *
 * 【方法说明】
 * - selectAllOrderByCreateTime：查询所有备份记录（按时间降序）
 * - selectByStatus：查询指定状态的备份记录（按时间降序）
 * - updateStatus：更新备份状态
 * - countAll：统计备份记录总数
 * - selectLatest：查询最新的备份记录
 */
@Mapper
public interface BackupMapper extends BaseMapper<Backup> {

    @Select("SELECT * FROM backups ORDER BY create_time DESC")
    List<Backup> selectAllOrderByCreateTime();

    @Select("SELECT * FROM backups WHERE status = #{status} ORDER BY create_time DESC")
    List<Backup> selectByStatus(@Param("status") String status);

    @Update("UPDATE backups SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Select("SELECT COUNT(*) FROM backups")
    int countAll();

    @Select("SELECT * FROM backups ORDER BY create_time DESC LIMIT #{limit}")
    List<Backup> selectLatest(@Param("limit") Integer limit);
}