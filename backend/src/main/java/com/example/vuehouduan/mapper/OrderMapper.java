package com.example.vuehouduan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuehouduan.entity.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ============================================================
 * 订单数据访问层 - 对应 orders 表
 * ============================================================
 *
 * 【MyBatis-Plus 说明】
 * - 继承 BaseMapper<Order>：自动获得 CRUD 方法
 * - @Mapper：标记为 MyBatis Mapper 接口
 * - @Select/@Update：注解方式编写 SQL
 *
 * 【方法说明】
 * - selectByOrderId：根据订单号查询订单
 * - selectByUserId：查询用户的所有订单（按时间降序）
 * - selectByMerchantId：查询商家的所有订单（按时间降序）
 * - selectByUserIdAndStatus：查询用户指定状态的订单
 * - selectByMerchantIdAndStatus：查询商家指定状态的订单
 * - updateStatus：更新订单状态
 * - updatePayStatus：更新支付状态（同时设置支付时间）
 * - updateShipInfo：更新发货信息（快递公司、快递单号）
 * - updateDeliverStatus：更新签收状态（同时设置签收时间）
 * - cancelOrder：取消订单（同时设置关闭时间和备注）
 * - countByUserIdAndStatus：统计用户指定状态的订单数量
 * - countByMerchantIdAndStatus：统计商家指定状态的订单数量
 * - countByStatus：统计指定状态的订单总数
 * - selectByStatus：查询指定状态的所有订单
 * - searchByKeyword：关键词搜索订单（收货人/电话/订单号）
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT * FROM orders WHERE order_id = #{orderId}")
    Order selectByOrderId(@Param("orderId") String orderId);

    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Order> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM orders WHERE merchant_id = #{merchantId} ORDER BY create_time DESC")
    List<Order> selectByMerchantId(@Param("merchantId") Long merchantId);

    @Select("SELECT * FROM orders WHERE user_id = #{userId} AND status = #{status} ORDER BY create_time DESC")
    List<Order> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Select("SELECT * FROM orders WHERE merchant_id = #{merchantId} AND status = #{status} ORDER BY create_time DESC")
    List<Order> selectByMerchantIdAndStatus(@Param("merchantId") Long merchantId, @Param("status") String status);

    @Update("UPDATE orders SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE orders SET status = #{status}, pay_time = NOW(), payment_status = 'PAID' WHERE id = #{id}")
    int updatePayStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE orders SET status = #{status}, ship_time = NOW(), express_company = #{expressCompany}, express_no = #{expressNo} WHERE id = #{id}")
    int updateShipInfo(@Param("id") Long id, @Param("status") String status,
            @Param("expressCompany") String expressCompany, @Param("expressNo") String expressNo);

    @Update("UPDATE orders SET status = #{status}, deliver_time = NOW() WHERE id = #{id}")
    int updateDeliverStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE orders SET status = 'CANCELLED', close_time = NOW(), remark = #{remark} WHERE id = #{id}")
    int cancelOrder(@Param("id") Long id, @Param("remark") String remark);

    @Select("SELECT COUNT(*) FROM orders WHERE user_id = #{userId} AND status = #{status}")
    int countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Select("SELECT COUNT(*) FROM orders WHERE merchant_id = #{merchantId} AND status = #{status}")
    int countByMerchantIdAndStatus(@Param("merchantId") Long merchantId, @Param("status") String status);

    @Select("SELECT COUNT(*) FROM orders WHERE status = #{status}")
    int countByStatus(@Param("status") String status);

    @Select("SELECT * FROM orders WHERE status = #{status} ORDER BY create_time DESC")
    List<Order> selectByStatus(@Param("status") String status);

    @Select("SELECT * FROM orders WHERE contact_name LIKE CONCAT('%', #{keyword}, '%') OR contact_phone LIKE CONCAT('%', #{keyword}, '%') OR order_id LIKE CONCAT('%', #{keyword}, '%') ORDER BY create_time DESC")
    List<Order> searchByKeyword(@Param("keyword") String keyword);
}