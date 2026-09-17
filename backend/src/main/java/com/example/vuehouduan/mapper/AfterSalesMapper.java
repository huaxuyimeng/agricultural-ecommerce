package com.example.vuehouduan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuehouduan.entity.AfterSales;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ============================================================
 * 售后数据访问层 - 对应 after_sales 表
 * ============================================================
 *
 * 【MyBatis-Plus 说明】
 * - 继承 BaseMapper<AfterSales>：自动获得 CRUD 方法
 * - @Mapper：标记为 MyBatis Mapper 接口
 * - @Select/@Update：注解方式编写 SQL
 *
 * 【方法说明】
 * - selectByUserId：查询用户的售后申请（按时间降序）
 * - selectByMerchantId：查询商家的售后申请（按时间降序）
 * - selectByOrderId：查询指定订单的售后申请
 * - selectByUserIdAndStatus：查询用户指定状态的售后申请
 * - selectByMerchantIdAndStatus：查询商家指定状态的售后申请
 * - updateStatus：更新售后状态
 * - approveAfterSales：审核通过售后（设置状态、退款状态、处理时间和备注）
 * - rejectAfterSales：拒绝售后申请（设置拒绝备注）
 * - completeAfterSales：完成售后（设置退款成功）
 * - countByUserIdAndStatus：统计用户指定状态的售后数量
 * - countByMerchantIdAndStatus：统计商家指定状态的售后数量
 */
@Mapper
public interface AfterSalesMapper extends BaseMapper<AfterSales> {

    @Select("SELECT * FROM after_sales WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<AfterSales> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM after_sales WHERE merchant_id = #{merchantId} ORDER BY create_time DESC")
    List<AfterSales> selectByMerchantId(@Param("merchantId") Long merchantId);

    @Select("SELECT * FROM after_sales WHERE order_id = #{orderId}")
    List<AfterSales> selectByOrderId(@Param("orderId") Long orderId);

    @Select("SELECT * FROM after_sales WHERE user_id = #{userId} AND status = #{status} ORDER BY create_time DESC")
    List<AfterSales> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Select("SELECT * FROM after_sales WHERE merchant_id = #{merchantId} AND status = #{status} ORDER BY create_time DESC")
    List<AfterSales> selectByMerchantIdAndStatus(@Param("merchantId") Long merchantId, @Param("status") String status);

    @Update("UPDATE after_sales SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE after_sales SET status = #{status}, refund_status = #{refundStatus}, handle_time = NOW(), remark = #{remark} WHERE id = #{id}")
    int approveAfterSales(@Param("id") Long id, @Param("status") String status,
            @Param("refundStatus") String refundStatus, @Param("remark") String remark);

    @Update("UPDATE after_sales SET status = 'rejected', handle_time = NOW(), remark = #{remark} WHERE id = #{id}")
    int rejectAfterSales(@Param("id") Long id, @Param("remark") String remark);

    @Update("UPDATE after_sales SET status = 'completed', refund_status = 'success', handle_time = NOW() WHERE id = #{id}")
    int completeAfterSales(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM after_sales WHERE user_id = #{userId} AND status = #{status}")
    int countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Select("SELECT COUNT(*) FROM after_sales WHERE merchant_id = #{merchantId} AND status = #{status}")
    int countByMerchantIdAndStatus(@Param("merchantId") Long merchantId, @Param("status") String status);
}