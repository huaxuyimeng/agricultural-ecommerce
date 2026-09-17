package com.example.vuehouduan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vuehouduan.entity.*;
import com.example.vuehouduan.mapper.*;
import com.example.vuehouduan.service.AfterSalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * ============================================================
 * 售后服务实现 - 退款、退货退款、换货
 * ============================================================
 *
 * 【前后端售后流程】
 *
 * 1. 用户申请售后：
 * 前端 POST /api/after-sales
 * Body: { orderId, productId, type("refund_only"/"return_refund"/"exchange"),
 * quantity, reason, description }
 * 后端 → 验证订单归属 → 验证商品属于该订单 → 根据类型检查订单状态
 * → 计算退款金额 → 创建售后记录
 * → 如果是仅退款(refund_only)：自动完成退款，恢复库存
 * 返回 → 售后记录对象
 *
 * 2. 商家审核售后：
 * 前端 PUT /api/after-sales/{id}/approve 或 /reject
 * 后端 → 验证商家权限 → 更新状态
 *
 * 3. 商家完成售后：
 * 前端 PUT /api/after-sales/{id}/complete
 * 后端 → 退货退款：退款到用户账户 + 恢复库存
 * → 换货：旧商品库存加回
 *
 * 4. 用户填写退货物流：
 * 前端 PUT /api/after-sales/{id}/express
 * Body: { expressCompany, expressNo }
 *
 * 【售后类型说明】
 * - refund_only(仅退款): 未发货时申请，自动退款
 * - return_refund(退货退款): 已收货后申请，需商家确认收到退货后退款
 * - exchange(换货): 已收货后申请，商家重新发货
 *
 * 【售后状态流转】
 * pending(待审核) → approved(已同意) / rejected(已拒绝)
 * approved → completed(已完成，退款到账)
 * pending → cancelled(已取消)
 */
@Service
public class AfterSalesServiceImpl extends ServiceImpl<AfterSalesMapper, AfterSales> implements AfterSalesService {

    @Autowired
    private AfterSalesMapper afterSalesMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderProductMapper orderProductMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserCouponMapper userCouponMapper;
    @Autowired
    private CouponMapper couponMapper;

    /**
     * 创建售后申请
     *
     * 【前后端数据流】
     * 前端 POST /api/after-sales
     * Body: { orderId, productId, type, quantity, reason, description }
     * 后端 → 验证订单归属 → 验证商品属于该订单 → 根据类型检查状态
     * → 计算退款金额 → 创建售后记录
     * → 仅退款自动完成：退款到账户 + 恢复库存 + 订单状态改为REFUNDED
     *
     * 【事务管理 @Transactional】
     * 确保售后记录创建、退款、库存恢复原子性
     */
    @Override
    @Transactional
    public AfterSales createAfterSales(Long userId, Map<String, Object> data) {
        Long orderId = Long.valueOf(data.get("orderId").toString());
        Long productId = Long.valueOf(data.get("productId").toString());
        String type = data.get("type").toString();
        Integer quantity = data.containsKey("quantity") ? Integer.valueOf(data.get("quantity").toString()) : 1;
        String reason = data.containsKey("reason") ? data.get("reason").toString() : "";
        String description = data.containsKey("description") ? data.get("description").toString() : "";

        // 验证订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }

        // 验证商品是否属于该订单
        QueryWrapper<OrderProduct> opWrapper = new QueryWrapper<>();
        opWrapper.eq("order_id", order.getOrderId()).eq("product_id", productId);
        OrderProduct orderProduct = orderProductMapper.selectOne(opWrapper);
        if (orderProduct == null) {
            throw new RuntimeException("该商品不属于此订单");
        }
        if (orderProduct.getQuantity() < quantity) {
            throw new RuntimeException("售后数量不能超过购买数量");
        }

        // 根据售后类型检查订单状态
        if ("refund_only".equals(type)) {
            // 仅退款：只能未发货状态申请
            if (!"PENDING".equals(order.getStatus()) && !"PAID".equals(order.getStatus())) {
                throw new RuntimeException("仅退款只能在待支付或待发货状态下申请");
            }
        } else if ("return_refund".equals(type)) {
            // 退货退款：已收货状态才能申请
            if (!"DELIVERED".equals(order.getStatus()) && !"COMPLETED".equals(order.getStatus())) {
                throw new RuntimeException("退货退款只能在已完成状态下申请");
            }
        } else if ("exchange".equals(type)) {
            // 换货：已收货状态才能申请
            if (!"DELIVERED".equals(order.getStatus()) && !"COMPLETED".equals(order.getStatus())) {
                throw new RuntimeException("换货只能在已完成状态下申请");
            }
        }

        // 计算退款金额（考虑优惠券折扣）
        BigDecimal productAmount = orderProduct.getPrice().multiply(new BigDecimal(quantity));
        BigDecimal refundAmount = productAmount;
        if (order.getActualAmount() != null && order.getTotalAmount() != null
                && order.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountRate = order.getActualAmount().divide(order.getTotalAmount(), 4,
                    BigDecimal.ROUND_HALF_UP);
            refundAmount = productAmount.multiply(discountRate).setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        AfterSales afterSales = new AfterSales();
        afterSales.setOrderId(orderId);
        afterSales.setOrderNo(order.getOrderId());
        afterSales.setUserId(userId);
        afterSales.setMerchantId(order.getMerchantId());
        afterSales.setProductId(productId);
        afterSales.setProductName(orderProduct.getProductName());
        afterSales.setProductImage(orderProduct.getProductImage());
        afterSales.setQuantity(quantity);
        afterSales.setRefundAmount(refundAmount);
        afterSales.setType(type);
        afterSales.setReason(reason);
        afterSales.setDescription(description);
        afterSales.setStatus("pending");
        afterSales.setRefundStatus("none");
        afterSales.setCreateTime(LocalDateTime.now());

        afterSalesMapper.insert(afterSales);

        if ("refund_only".equals(type)) {
            order.setStatus("REFUNDED");
            order.setCloseTime(LocalDateTime.now());
            orderMapper.updateById(order);

            Product product = productMapper.selectById(productId);
            if (product != null) {
                product.setStock(product.getStock() + quantity);
                product.setSales(product.getSales() - quantity);
                productMapper.updateById(product);
            }

            afterSales.setStatus("completed");
            afterSales.setRefundStatus("success");
            afterSales.setHandleTime(LocalDateTime.now());
            afterSalesMapper.updateById(afterSales);

            User user = userMapper.selectById(userId);
            if (user != null) {
                BigDecimal balance = user.getAccount() != null ? user.getAccount() : BigDecimal.ZERO;
                user.setAccount(balance.add(refundAmount));
                userMapper.updateById(user);
            }

            // 如果订单使用了优惠券，恢复优惠券为可用状态
            if (order.getCouponId() != null) {
                UserCoupon userCoupon = userCouponMapper.selectById(order.getCouponId());
                if (userCoupon != null && userCoupon.getStatus() == 2) {
                    userCoupon.setStatus(1);
                    userCoupon.setUseTime(null);
                    userCoupon.setOrderNo(null);
                    userCoupon.setDiscountAmount(null);
                    userCoupon.setUpdateTime(LocalDateTime.now());
                    userCouponMapper.updateById(userCoupon);
                }
            }
        }

        return afterSales;
    }

    /**
     * 获取用户售后申请列表（分页）
     *
     * 【前后端数据流】
     * 前端 GET /api/after-sales?page=1&pageSize=10&status=pending
     * 后端 → 根据userId查询 → 按状态筛选 → 分页返回
     * 返回 → { list: [...], total: 100 }
     */
    @Override
    public Map<String, Object> getUserAfterSales(Long userId, Integer page, Integer pageSize, String status) {
        Page<AfterSales> pageObj = new Page<>(page, pageSize);
        QueryWrapper<AfterSales> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        Page<AfterSales> result = afterSalesMapper.selectPage(pageObj, wrapper);

        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        return map;
    }

    /**
     * 根据ID获取售后申请详情
     *
     * @param id     售后申请ID
     * @param userId 用户ID（用于权限校验）
     * @return 售后申请对象
     */
    @Override
    public AfterSales getAfterSalesById(Long id, Long userId) {
        AfterSales afterSales = afterSalesMapper.selectById(id);
        if (afterSales == null) {
            throw new RuntimeException("售后申请不存在");
        }
        if (userId != null && !afterSales.getUserId().equals(userId)) {
            throw new RuntimeException("无权查看此售后申请");
        }
        return afterSales;
    }

    /**
     * 商家审核通过售后申请
     *
     * 【前后端数据流】
     * 前端 PUT /api/after-sales/{id}/approve
     * Body: { remark: "同意退款" }
     * 后端 → 验证商家权限 → 更新状态为approved → 如果是仅退款则直接退款
     *
     * 【事务管理 @Transactional】
     * 确保状态更新和退款操作原子性
     */
    @Override
    @Transactional
    public void approveAfterSales(Long id, Long merchantId, String remark) {
        AfterSales afterSales = afterSalesMapper.selectById(id);
        if (afterSales == null) {
            throw new RuntimeException("售后申请不存在");
        }
        if (!afterSales.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("无权操作此售后申请");
        }
        if (!"pending".equals(afterSales.getStatus())) {
            throw new RuntimeException("当前状态不允许审核");
        }

        afterSales.setStatus("approved");
        afterSales.setHandleTime(LocalDateTime.now());
        if (StringUtils.hasText(remark)) {
            afterSales.setRemark(remark);
        }

        // 如果是仅退款（未发货），直接退款
        if ("refund_only".equals(afterSales.getType())) {
            processRefund(afterSales);
        }

        afterSalesMapper.updateById(afterSales);
    }

    /**
     * 商家拒绝售后申请
     *
     * 【前后端数据流】
     * 前端 PUT /api/after-sales/{id}/reject
     * Body: { remark: "不符合退款条件" }
     * 后端 → 验证商家权限 → 更新状态为rejected
     */
    @Override
    @Transactional
    public void rejectAfterSales(Long id, Long merchantId, String remark) {
        AfterSales afterSales = afterSalesMapper.selectById(id);
        if (afterSales == null) {
            throw new RuntimeException("售后申请不存在");
        }
        if (!afterSales.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("无权操作此售后申请");
        }
        if (!"pending".equals(afterSales.getStatus())) {
            throw new RuntimeException("当前状态不允许审核");
        }

        afterSales.setStatus("rejected");
        afterSales.setHandleTime(LocalDateTime.now());
        if (StringUtils.hasText(remark)) {
            afterSales.setRemark(remark);
        }
        afterSalesMapper.updateById(afterSales);
    }

    /**
     * 商家完成售后处理
     *
     * 【前后端数据流】
     * 前端 PUT /api/after-sales/{id}/complete
     * 后端 → 验证商家权限 → 根据售后类型处理：
     * - 退货退款：退款到用户账户 + 恢复库存
     * - 换货：旧商品库存加回
     * → 更新状态为completed
     *
     * 【事务管理 @Transactional】
     * 确保退款、库存更新、状态更新原子性
     */
    @Override
    @Transactional
    public void completeAfterSales(Long id, Long merchantId) {
        AfterSales afterSales = afterSalesMapper.selectById(id);
        if (afterSales == null) {
            throw new RuntimeException("售后申请不存在");
        }
        if (!afterSales.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("无权操作此售后申请");
        }

        // 退货退款：确认收到退货后退款
        if ("return_refund".equals(afterSales.getType()) && "approved".equals(afterSales.getStatus())) {
            processRefund(afterSales);
            // 退货退款：库存加回去
            Product product = productMapper.selectById(afterSales.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + afterSales.getQuantity());
                product.setSales(product.getSales() - afterSales.getQuantity());
                productMapper.updateById(product);
            }
        }

        // 换货：创建新订单发货（简化处理，直接标记完成）
        if ("exchange".equals(afterSales.getType())) {
            // 换货逻辑：旧商品库存加回，新商品库存扣减
            Product oldProduct = productMapper.selectById(afterSales.getProductId());
            if (oldProduct != null) {
                oldProduct.setStock(oldProduct.getStock() + afterSales.getQuantity());
                oldProduct.setSales(oldProduct.getSales() - afterSales.getQuantity());
                productMapper.updateById(oldProduct);
            }
        }

        afterSales.setStatus("completed");
        afterSalesMapper.updateById(afterSales);
    }

    /**
     * 用户取消售后申请
     *
     * 【前后端数据流】
     * 前端 PUT /api/after-sales/{id}/cancel
     * 后端 → 验证用户权限 → 检查状态为pending → 更新状态为cancelled
     */
    @Override
    @Transactional
    public void cancelAfterSales(Long id, Long userId) {
        AfterSales afterSales = afterSalesMapper.selectById(id);
        if (afterSales == null) {
            throw new RuntimeException("售后申请不存在");
        }
        if (!afterSales.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此售后申请");
        }
        if (!"pending".equals(afterSales.getStatus())) {
            throw new RuntimeException("当前状态不允许取消");
        }

        afterSales.setStatus("cancelled");
        afterSalesMapper.updateById(afterSales);
    }

    /**
     * 用户填写退货物流信息
     *
     * 【前后端数据流】
     * 前端 PUT /api/after-sales/{id}/express
     * Body: { expressCompany: "中通快递", expressNo: "ZTO1234567890" }
     * 后端 → 验证用户权限 → 检查状态为approved → 更新物流信息
     */
    @Override
    public void updateExpress(Long id, Long userId, String expressCompany, String expressNo) {
        AfterSales afterSales = afterSalesMapper.selectById(id);
        if (afterSales == null) {
            throw new RuntimeException("售后申请不存在");
        }
        if (!afterSales.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此售后申请");
        }
        if (!"approved".equals(afterSales.getStatus())) {
            throw new RuntimeException("只有已同意的售后申请才能填写物流信息");
        }

        afterSales.setExpressCompany(expressCompany);
        afterSales.setExpressNo(expressNo);
        afterSalesMapper.updateById(afterSales);
    }

    /**
     * 获取商家收到的售后申请列表（商家端）
     *
     * 【前后端数据流】
     * 前端商家管理页面 → GET /api/after-sales/merchant?page=1&pageSize=10&status=pending
     * 后端 → 根据merchantId查询 → 按状态筛选 → 分页返回
     */
    @Override
    public Map<String, Object> getMerchantAfterSales(Long merchantId, Integer page, Integer pageSize, String status) {
        Page<AfterSales> pageObj = new Page<>(page, pageSize);
        QueryWrapper<AfterSales> wrapper = new QueryWrapper<>();
        wrapper.eq("merchant_id", merchantId);
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        Page<AfterSales> result = afterSalesMapper.selectPage(pageObj, wrapper);

        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        return map;
    }

    /**
     * 处理退款：将金额退回到用户账户，并恢复优惠券
     */
    private void processRefund(AfterSales afterSales) {
        User user = userMapper.selectById(afterSales.getUserId());
        if (user != null) {
            BigDecimal balance = user.getAccount() != null ? user.getAccount() : BigDecimal.ZERO;
            user.setAccount(balance.add(afterSales.getRefundAmount()));
            userMapper.updateById(user);
        }
        afterSales.setRefundStatus("success");

        // 如果订单使用了优惠券，恢复优惠券为可用状态
        Order order = orderMapper.selectById(afterSales.getOrderId());
        if (order != null && order.getCouponId() != null) {
            UserCoupon userCoupon = userCouponMapper.selectById(order.getCouponId());
            if (userCoupon != null && userCoupon.getStatus() == 2) {
                userCoupon.setStatus(1);
                userCoupon.setUseTime(null);
                userCoupon.setOrderNo(null);
                userCoupon.setDiscountAmount(null);
                userCoupon.setUpdateTime(LocalDateTime.now());
                userCouponMapper.updateById(userCoupon);
            }
        }
    }
}
