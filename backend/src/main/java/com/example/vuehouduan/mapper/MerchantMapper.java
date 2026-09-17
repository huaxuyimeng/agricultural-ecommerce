package com.example.vuehouduan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuehouduan.entity.Merchant;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ============================================================
 * 商家数据访问层 - 对应 merchants 表
 * ============================================================
 *
 * 【MyBatis-Plus 说明】
 * - 继承 BaseMapper<Merchant>：自动获得 CRUD 方法
 * - @Mapper：标记为 MyBatis Mapper 接口
 * - @Select/@Update：注解方式编写 SQL
 *
 * 【方法说明】
 * - selectByApprovalStatus：查询指定审核状态的商家
 * - updateApprovalStatus：更新商家审核状态（同时设置审核时间）
 * - updateShopInfo：更新商家店铺信息（名称、地址、描述）
 * - updateShopLogo：更新商家店铺 Logo
 * - countByApprovalStatus：统计指定审核状态的商家数量
 */
@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {

    @Select("SELECT * FROM merchants WHERE approval_status = #{status}")
    List<Merchant> selectByApprovalStatus(@Param("status") String status);

    @Update("UPDATE merchants SET approval_status = #{status}, approve_time = NOW() WHERE id = #{id}")
    int updateApprovalStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE merchants SET shop_name = #{shopName}, shop_address = #{shopAddress}, shop_description = #{shopDescription} WHERE id = #{id}")
    int updateShopInfo(@Param("id") Long id, @Param("shopName") String shopName,
            @Param("shopAddress") String shopAddress, @Param("shopDescription") String shopDescription);

    @Update("UPDATE merchants SET shop_logo = #{shopLogo} WHERE id = #{id}")
    int updateShopLogo(@Param("id") Long id, @Param("shopLogo") String shopLogo);

    @Select("SELECT COUNT(*) FROM merchants WHERE approval_status = #{status}")
    int countByApprovalStatus(@Param("status") String status);
}