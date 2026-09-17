package com.example.vuehouduan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vuehouduan.entity.*;
import com.example.vuehouduan.mapper.*;
import com.example.vuehouduan.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ============================================================
 * 订单服务实现 - 订单全生命周期管理
 * ============================================================
 *
 * 【订单状态流转（核心业务流程）】
 *
 * 用户下单 → PENDING（待支付）
 * ↓ 用户支付
 * PAID（已支付，等待商家发货）
 * ↓ 商家发货，填写快递单号
 * SHIPPED（已发货，等待用户收货）
 * ↓ 用户确认收货
 * DELIVERED（已签收）/ COMPLETED（已完成）
 *
 * 任意非终态 → CANCELLED（已取消，恢复库存）
 *
 * 【前后端订单创建完整流程】
 *
 * 1. 前端购物车页面 → 用户勾选商品 → 点击"结算"
 * 2. 前端弹出结算对话框 → 用户填写收货地址、选择支付方式
 * 3. 前端 POST /api/orders
 * Body: {
 * products: [{ productId: 1, count: 2 }, { productId: 3, count: 1 }],
 * shippingAddress: "广东省广州市...",
 * receiverName: "张三",
 * shippingPhone: "13800138000",
 * paymentMethod: "alipay", // alipay/wechat/cash
 * remark: "请尽快发货"
 * }
 * 4. 后端 OrderController.createOrder() → orderService.createOrder()
 * → 生成订单号(ORD+时间戳+随机数)
 * → 遍历商品列表：计算金额、扣减库存、增加销量
 * → 插入 order 表和 order_product 表
 * → 清除购物车中已购买的商品
 * → 返回完整订单对象
 * 5. 前端收到订单 → 跳转支付页面或订单详情
 *
 * 【前后端支付流程】
 *
 * 1. 前端 POST /api/orders/{id}/pay
 * Body: { paymentMethod: "alipay" }
 * 2. 后端 → 验证订单归属 → 检查状态为PENDING → 扣减账户余额
 * → 更新状态为PAID → 记录支付时间
 * 3. 前端收到成功响应 → 跳转订单详情，显示"等待发货"
 *
 * 【前后端发货流程（商家端）】
 *
 * 1. 商家在订单管理页面 → 点击"发货"按钮
 * 2. 前端弹出悬浮窗 → 选择快递公司 → 输入快递单号
 * 3. 前端 PUT /api/orders/{id}/ship
 * Body: { expressCompany: "顺丰速运", expressNo: "SF1234567890" }
 * 4. 后端 → 验证商家权限 → 检查状态为PAID → 更新状态为SHIPPED
 * → 记录快递公司和单号 → 记录发货时间
 * 5. 前端刷新 → 订单状态变为"已发货"，显示快递信息
 *
 * 【前后端确认收货流程（用户端）】
 *
 * 1. 用户点击"确认收货"
 * 2. 前端 PUT /api/orders/{id}/confirm
 * 3. 后端 → 验证用户权限 → 检查状态为SHIPPED → 更新状态为DELIVERED
 * 4. 前端刷新 → 订单状态变为"已签收"
 *
 * 【数据表关系】
 * order 表：订单主表（订单号、用户ID、商家ID、金额、状态、快递信息）
 * order_product 表：订单商品明细（订单号、商品ID、数量、单价）
 * product 表：商品表（下单时扣库存、加销量；取消时恢复）
 * cart 表：购物车（下单成功后清除已购商品）
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderProductMapper orderProductMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private UserCouponMapper userCouponMapper;
    @Autowired
    private CouponMapper couponMapper;

    /**
     * 创建订单（核心业务方法）
     *
     * 【前后端数据流】
     * 前端 POST /api/orders
     * Body: { products:[{productId,count}], shippingAddress, receiverName,
     * shippingPhone, paymentMethod, remark }
     * → 后端解析JSON为Map → 生成订单号 → 遍历商品计算金额
     * → 扣减库存、增加销量 → 插入order和order_product表 → 清除购物车
     * 返回 → 完整订单对象（含订单号、金额、状态等）
     *
     * 【事务管理 @Transactional】
     * 确保以下操作原子性：
     * - 订单记录插入
     * - 商品库存扣减
     * - 购物车清理
     * 任何一步失败都会回滚
     *
     * 【支付方式处理】
     * - alipay/wechat: 状态为PENDING，需要后续支付
     * - cash(货到付款): 状态直接设为PAID，跳过支付步骤
     *
     * @param userId    下单用户ID
     * @param orderData 前端传来的订单数据Map
     * @return 创建成功的订单对象
     */
    @Override
    @Transactional
    public Order createOrder(Long userId, Map<String, Object> orderData) {
        Order order = new Order();
        order.setOrderId(generateOrderId());
        order.setUserId(userId);

        Long merchantId = null;
        if (orderData.containsKey("merchantId") && orderData.get("merchantId") != null) {
            try {
                merchantId = Long.valueOf(orderData.get("merchantId").toString());
            } catch (Exception e) {
            }
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> products = (List<Map<String, Object>>) orderData.get("products");
        if (products == null || products.isEmpty()) {
            throw new RuntimeException("订单商品不能为空");
        }
        List<OrderProduct> orderProducts = new ArrayList<>();

        for (Map<String, Object> p : products) {
            if (p.get("productId") == null) {
                throw new RuntimeException("商品ID不能为空");
            }
            Long productId = Long.valueOf(p.get("productId").toString());
            Integer count = 1;
            if (p.get("count") != null) {
                count = Integer.valueOf(p.get("count").toString());
            }
            Product product = productMapper.selectById(productId);
            if (product == null) {
                throw new RuntimeException("商品不存在: " + productId);
            }
            // 如果没有指定 merchantId，从商品中获取
            if (merchantId == null) {
                merchantId = product.getMerchantId();
            }
            BigDecimal subtotal = product.getPrice().multiply(new BigDecimal(count));
            totalAmount = totalAmount.add(subtotal);

            OrderProduct op = new OrderProduct();
            op.setOrderId(order.getOrderId());
            op.setProductId(productId);
            op.setProductName(product.getName());
            op.setProductImage(product.getImage());
            op.setQuantity(count);
            op.setPrice(product.getPrice());
            op.setTotalPrice(subtotal);
            orderProducts.add(op);

            // 扣减库存，增加销量
            if (product.getStock() != null) {
                product.setStock(product.getStock() - count);
            }
            if (product.getSales() != null) {
                product.setSales(product.getSales() + count);
            } else {
                product.setSales(count);
            }
            productMapper.updateById(product);
        }

        // 如果仍然没有 merchantId，设置默认值 1
        if (merchantId == null) {
            merchantId = 1L; // 默认商家ID
        }

        order.setMerchantId(merchantId);
        String shippingAddress = orderData.get("shippingAddress") != null ? orderData.get("shippingAddress").toString()
                : "";
        order.setShippingAddress(shippingAddress);
        order.setContactName(orderData.get("receiverName") != null ? orderData.get("receiverName").toString()
                : orderData.get("shippingName") != null ? orderData.get("shippingName").toString() : "");
        order.setContactPhone(orderData.get("shippingPhone") != null ? orderData.get("shippingPhone").toString()
                : orderData.get("phone") != null ? orderData.get("phone").toString() : "");
        if (orderData.containsKey("remark")) {
            order.setRemark(orderData.get("remark").toString());
        }
        // 处理支付方式
        String paymentMethod = orderData.get("paymentMethod") != null
                ? orderData.get("paymentMethod").toString().toLowerCase()
                : "alipay";
        order.setPaymentMethod(paymentMethod);

        // 如果是货到付款，直接标记为已支付
        String orderStatus = "PENDING";
        if ("cash".equals(paymentMethod) || "货到付款".equals(orderData.get("paymentMethod"))) {
            orderStatus = "PAID"; // 货到付款视为已支付，等待发货
        }
        order.setStatus(orderStatus);
        order.setPaymentStatus("UNPAID".equals(orderStatus) ? "UNPAID" : "PAID");
        order.setCreateTime(LocalDateTime.now());

        order.setTotalAmount(totalAmount);

        // 处理优惠券
        BigDecimal couponAmount = BigDecimal.ZERO;
        Long couponId = null;
        String couponCode = null;
        if (orderData.containsKey("couponId") && orderData.get("couponId") != null) {
            try {
                couponId = Long.valueOf(orderData.get("couponId").toString());
                couponCode = orderData.get("couponCode") != null ? orderData.get("couponCode").toString() : null;

                // 验证优惠券
                UserCoupon userCoupon = userCouponMapper.selectById(couponId);
                if (userCoupon == null) {
                    throw new RuntimeException("优惠券不存在");
                }
                if (!userCoupon.getUserId().equals(userId)) {
                    throw new RuntimeException("优惠券不属于当前用户");
                }
                if (userCoupon.getStatus() != 1) {
                    throw new RuntimeException("优惠券已使用或已过期");
                }
                LocalDateTime now = LocalDateTime.now();
                if (now.isBefore(userCoupon.getValidStart()) || now.isAfter(userCoupon.getValidEnd())) {
                    throw new RuntimeException("优惠券已过期");
                }

                // 获取优惠券详情，检查使用条件
                Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
                if (coupon != null && coupon.getMinAmount() != null
                        && coupon.getMinAmount().compareTo(BigDecimal.ZERO) > 0) {
                    if (totalAmount.compareTo(coupon.getMinAmount()) < 0) {
                        throw new RuntimeException("订单金额不满足优惠券使用条件，满" + coupon.getMinAmount() + "元可用");
                    }
                }

                // 计算优惠金额
                if (coupon != null) {
                    if (coupon.getType() == 1) {
                        // 现金券
                        couponAmount = coupon.getAmount();
                    } else if (coupon.getType() == 2) {
                        // 折扣券
                        couponAmount = totalAmount.multiply(BigDecimal.ONE.subtract(coupon.getAmount()));
                    }
                    // 优惠金额不能超过订单金额
                    if (couponAmount.compareTo(totalAmount) > 0) {
                        couponAmount = totalAmount;
                    }
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                // 优惠券信息无效，忽略
            }
        }

        BigDecimal actualAmount = totalAmount.subtract(couponAmount);
        if (actualAmount.compareTo(BigDecimal.ZERO) < 0) {
            actualAmount = BigDecimal.ZERO;
        }

        order.setActualAmount(actualAmount);
        order.setCouponId(couponId);
        order.setCouponAmount(couponAmount);
        order.setCouponCode(couponCode);
        orderMapper.insert(order);

        for (OrderProduct op : orderProducts) {
            orderProductMapper.insert(op);
        }

        // 清除购物车中已结算的商品
        QueryWrapper<Cart> cartWrapper = new QueryWrapper<>();
        cartWrapper.eq("user_id", userId);
        List<Cart> cartItems = cartMapper.selectList(cartWrapper);
        for (Map<String, Object> p : products) {
            Long productId = Long.valueOf(p.get("productId").toString());
            Integer orderQuantity = 1;
            if (p.get("count") != null) {
                orderQuantity = Integer.valueOf(p.get("count").toString());
            }
            for (Cart cart : cartItems) {
                if (cart.getProductId().equals(productId)) {
                    Integer cartQuantity = cart.getQuantity();
                    if (cartQuantity <= orderQuantity) {
                        // 购物车数量小于等于购买数量，移除该商品
                        cartMapper.deleteById(cart.getId());
                    } else {
                        // 购物车数量大于购买数量，减少数量
                        cart.setQuantity(cartQuantity - orderQuantity);
                        cart.setTotalPrice(cart.getPrice().multiply(new BigDecimal(cart.getQuantity())));
                        cart.setUpdateTime(LocalDateTime.now());
                        cartMapper.updateById(cart);
                    }
                }
            }
        }

        return order;
    }

    /**
     * 获取用户订单列表（分页）
     *
     * 【前后端数据流】
     * 前端 GET /api/orders?page=1&pageSize=10&status=PAID
     * 后端 → 根据userId查询 → 按状态筛选 → 分页返回
     * 返回 → { list: [...], total: 100 }
     *
     * @param userId   用户ID
     * @param page     页码
     * @param pageSize 每页数量
     * @param status   订单状态筛选（可选）
     * @return 分页结果Map
     */
    @Override
    public Map<String, Object> getUserOrders(Long userId, Integer page, Integer pageSize, String status) {
        Page<Order> pageObj = new Page<>(page, pageSize);
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        Page<Order> result = orderMapper.selectPage(pageObj, wrapper);
        fillOrderDetails(result.getRecords());
        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        return map;
    }

    /**
     * 根据数据库ID获取订单详情（含权限校验）
     *
     * 【前后端数据流】
     * 前端 GET /api/orders/{id}
     * 后端 → 查询订单 → 权限校验（买家/商家/管理员） → 查询商品明细 → 填充商品图片
     * 返回 → 完整订单对象（含products列表）
     *
     * 【权限校验逻辑】
     * - isOwner: 买家查看自己的订单（userId匹配）
     * - isMerchant: 商家查看自己店铺的订单（merchantId匹配）
     * - isAdmin: 管理员可查看所有订单
     *
     * @param id     订单数据库主键ID
     * @param userId 当前用户ID
     * @param role   当前用户角色
     * @return 订单对象
     */
    @Override
    public Order getOrderById(Long id, Long userId, String role) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        // 权限检查：买家查看自己的订单，或商家查看自己店铺的订单，或管理员
        boolean isOwner = userId != null && order.getUserId().equals(userId);
        boolean isMerchant = "MERCHANT".equals(role) && userId != null && order.getMerchantId().equals(userId);
        boolean isAdmin = "ADMIN".equals(role);
        if (!isOwner && !isMerchant && !isAdmin) {
            throw new RuntimeException("无权查看此订单");
        }
        QueryWrapper<OrderProduct> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", order.getOrderId());
        List<OrderProduct> products = orderProductMapper.selectList(wrapper);
        // Fill product images
        for (OrderProduct op : products) {
            Product product = productMapper.selectById(op.getProductId());
            if (product != null) {
                op.setProductImage(product.getImage());
            }
        }
        order.setProducts(products);
        return order;
    }

    /**
     * 根据订单号获取订单详情（含权限校验）
     *
     * 【前后端数据流】
     * 前端 GET /api/orders/detail/{orderId} （orderId是ORD开头的业务订单号）
     * 后端 → 通过order_id字段查询 → 权限校验 → 填充商品明细
     *
     * @param orderId 业务订单号（如 ORD202405011200001234）
     * @param userId  当前用户ID
     * @param role    当前用户角色
     * @return 订单对象
     */
    @Override
    public Order getOrderByOrderId(String orderId, Long userId, String role) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        Order order = orderMapper.selectOne(wrapper);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        // 权限检查：买家查看自己的订单，或商家查看自己店铺的订单，或管理员
        boolean isOwner = userId != null && order.getUserId().equals(userId);
        boolean isMerchant = "MERCHANT".equals(role) && userId != null && order.getMerchantId().equals(userId);
        boolean isAdmin = "ADMIN".equals(role);
        if (!isOwner && !isMerchant && !isAdmin) {
            throw new RuntimeException("无权查看此订单");
        }
        QueryWrapper<OrderProduct> opWrapper = new QueryWrapper<>();
        opWrapper.eq("order_id", order.getOrderId());
        List<OrderProduct> products = orderProductMapper.selectList(opWrapper);
        for (OrderProduct op : products) {
            Product product = productMapper.selectById(op.getProductId());
            if (product != null) {
                op.setProductImage(product.getImage());
            }
        }
        order.setProducts(products);
        return order;
    }

    /**
     * 取消订单
     *
     * 【前后端数据流】
     * 前端 PUT /api/orders/{id}/cancel
     * Body: { reason: "不想要了" }
     * 后端 → 验证权限 → 检查状态(PENDING/PAID可取消) → 恢复库存和销量
     * → 如果已支付则退款到账户余额 → 更新状态为CANCELLED
     *
     * 【事务管理 @Transactional】
     * 确保订单状态更新、库存恢复、余额退款原子性
     *
     * @param id     订单ID
     * @param userId 用户ID
     * @param reason 取消原因
     */
    @Override
    @Transactional
    public void cancelOrder(Long id, Long userId, String reason) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (!"PENDING".equals(order.getStatus()) && !"PAID".equals(order.getStatus())) {
            throw new RuntimeException("当前状态不允许取消");
        }

        if ("PAID".equals(order.getStatus())) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                BigDecimal refundAmount = order.getActualAmount() != null ? order.getActualAmount()
                        : order.getTotalAmount();
                if (refundAmount != null) {
                    BigDecimal balance = user.getAccount() != null ? user.getAccount() : BigDecimal.ZERO;
                    user.setAccount(balance.add(refundAmount));
                    userMapper.updateById(user);
                }
            }
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

        order.setStatus("CANCELLED");
        order.setCloseTime(LocalDateTime.now());
        if (StringUtils.hasText(reason)) {
            order.setRemark(reason);
        }
        orderMapper.updateById(order);

        QueryWrapper<OrderProduct> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", order.getOrderId());
        List<OrderProduct> products = orderProductMapper.selectList(wrapper);
        for (OrderProduct op : products) {
            Product product = productMapper.selectById(op.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + op.getQuantity());
                product.setSales(product.getSales() - op.getQuantity());
                productMapper.updateById(product);
            }
        }
    }

    /**
     * 支付订单（余额支付）
     *
     * 【前后端数据流】
     * 前端 POST /api/orders/{id}/pay
     * Body: { paymentMethod: "alipay" }
     * 后端 → 验证权限 → 检查状态为PENDING → 检查余额是否充足
     * → 扣减账户余额 → 更新状态为PAID → 记录支付时间
     *
     * 【事务管理 @Transactional】
     * 确保余额扣减和订单状态更新原子性
     *
     * @param id            订单ID
     * @param userId        用户ID
     * @param paymentMethod 支付方式
     */
    @Override
    @Transactional
    public void payOrder(Long id, Long userId, String paymentMethod) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("当前状态不允许支付");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 使用创建订单时已计算的实付金额（已扣减优惠券）
        BigDecimal payAmount = order.getActualAmount() != null ? order.getActualAmount() : order.getTotalAmount();
        if (payAmount == null) {
            payAmount = BigDecimal.ZERO;
        }
        if (payAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("订单金额无效");
        }

        BigDecimal balance = user.getAccount() != null ? user.getAccount() : BigDecimal.ZERO;
        if (balance.compareTo(payAmount) < 0) {
            throw new RuntimeException("账户余额不足，当前余额：" + balance + "，需支付：" + payAmount);
        }

        user.setAccount(balance.subtract(payAmount));
        userMapper.updateById(user);

        order.setStatus("PAID");
        order.setPaymentStatus("PAID");
        order.setPaymentMethod(paymentMethod);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 如果订单使用了优惠券，标记优惠券为已使用
        if (order.getCouponId() != null) {
            UserCoupon userCoupon = userCouponMapper.selectById(order.getCouponId());
            if (userCoupon != null && userCoupon.getStatus() == 1) {
                userCoupon.setStatus(2);
                userCoupon.setUseTime(LocalDateTime.now());
                userCoupon.setOrderNo(order.getOrderId());
                userCoupon.setDiscountAmount(order.getCouponAmount());
                userCoupon.setUpdateTime(LocalDateTime.now());
                userCouponMapper.updateById(userCoupon);
            }
        }
    }

    /**
     * 确认收货（用户端操作）
     *
     * 【前后端数据流】
     * 前端 PUT /api/orders/{id}/confirm
     * 后端 → 验证用户权限 → 检查状态为SHIPPED → 更新状态为DELIVERED
     *
     * @param id     订单ID
     * @param userId 用户ID
     */
    @Override
    public void confirmReceive(Long id, Long userId) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (!"SHIPPED".equals(order.getStatus())) {
            throw new RuntimeException("当前状态不允许确认收货");
        }
        order.setStatus("DELIVERED");
        order.setDeliverTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    /**
     * 获取商家订单列表（商家端查看自己店铺的订单）
     *
     * 【前后端数据流】
     * 前端商家管理页面 → GET /api/orders/merchant?page=1&pageSize=10&status=PAID
     * 后端 → 根据merchantId查询 → 按状态筛选 → 分页返回
     *
     * @param merchantId 商家ID
     * @param page       页码
     * @param pageSize   每页数量
     * @param status     订单状态筛选
     * @return 分页结果Map
     */
    @Override
    public Map<String, Object> getMerchantOrders(Long merchantId, Integer page, Integer pageSize, String status) {
        Page<Order> pageObj = new Page<>(page, pageSize);
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("merchant_id", merchantId);
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        Page<Order> result = orderMapper.selectPage(pageObj, wrapper);
        fillOrderDetails(result.getRecords());
        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        return map;
    }

    /**
     * 商家发货
     *
     * 【前后端数据流】
     * 前端商家订单管理 → 点击"发货" → 弹出悬浮窗选择快递公司、输入快递单号
     * 前端 PUT /api/orders/{id}/ship
     * Body: { expressCompany: "顺丰速运", expressNo: "SF1234567890" }
     * 后端 → 验证商家权限 → 检查状态为PAID → 更新状态为SHIPPED
     * → 记录快递公司和单号 → 记录发货时间
     *
     * 【快递单号格式】
     * 前端在快递单号前加上公司编码前缀，如：
     * - 顺丰速运: SF1234567890
     * - 中国邮政: EMS1234567890
     * - 中通快递: ZTO1234567890
     *
     * @param id             订单ID
     * @param merchantId     商家ID
     * @param expressCompany 快递公司名称
     * @param expressNo      快递单号
     */
    @Override
    public void shipOrder(Long id, Long merchantId, String expressCompany, String expressNo) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (!"PAID".equals(order.getStatus())) {
            throw new RuntimeException("当前状态不允许发货");
        }
        order.setStatus("SHIPPED");
        order.setShipTime(LocalDateTime.now());
        // 设置快递信息
        order.setExpressCompany(expressCompany);
        order.setExpressNo(expressNo);
        orderMapper.updateById(order);
    }

    /**
     * 获取所有订单（管理员端）
     *
     * 【前后端数据流】
     * 前端管理员页面 → GET /api/admin/orders?page=1&pageSize=20&status=&keyword=
     * 后端 → 支持按状态筛选、按订单号/收件人/电话搜索 → 分页返回
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @param status   状态筛选
     * @param keyword  搜索关键词
     * @return 分页结果Map
     */
    @Override
    public Map<String, Object> getAllOrders(Integer page, Integer pageSize, String status, String keyword) {
        Page<Order> pageObj = new Page<>(page, pageSize);
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("order_id", keyword)
                    .or().like("contact_name", keyword)
                    .or().like("contact_phone", keyword));
        }
        wrapper.orderByDesc("create_time");
        Page<Order> result = orderMapper.selectPage(pageObj, wrapper);
        fillOrderDetails(result.getRecords());
        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        return map;
    }

    /**
     * 获取订单统计信息
     *
     * 【前后端数据流】
     * 前端仪表盘 → GET /api/orders/stats
     * 后端 → 统计各状态订单数量 → 返回 { total, byStatus: { pending, paid, shipped, ... } }
     *
     * @param userId 用户ID（null则统计全部）
     * @return 统计数据Map
     */
    @Override
    public Map<String, Object> getOrderStats(Long userId) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        long total = orderMapper.selectCount(wrapper);

        Map<String, Object> byStatus = new HashMap<>();
        for (String s : Arrays.asList("PENDING", "PAID", "SHIPPED", "DELIVERED", "CANCELLED")) {
            QueryWrapper<Order> w = new QueryWrapper<Order>();
            if (userId != null) {
                w.eq("user_id", userId);
            }
            w.eq("status", s);
            byStatus.put(s.toLowerCase(), orderMapper.selectCount(w));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("byStatus", byStatus);
        return result;
    }

    /**
     * 生成订单号
     * 格式: ORD + yyyyMMddHHmmss + 4位随机数
     * 例如: ORD202405011200001234
     */
    private String generateOrderId() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = (int) (Math.random() * 10000);
        return "ORD" + now + String.format("%04d", random);
    }

    /**
     * 填充订单详情（用户名、商家名、商品列表、商品图片）
     * 用于订单列表展示，批量填充关联数据
     */
    private void fillOrderDetails(List<Order> orders) {
        for (Order o : orders) {
            User user = userMapper.selectById(o.getUserId());
            if (user != null)
                o.setUserName(user.getName());
            Merchant merchant = merchantMapper.selectById(o.getMerchantId());
            if (merchant != null)
                o.setMerchantName(merchant.getShopName());

            QueryWrapper<OrderProduct> wrapper = new QueryWrapper<>();
            wrapper.eq("order_id", o.getOrderId());
            List<OrderProduct> products = orderProductMapper.selectList(wrapper);
            // Fill product images
            for (OrderProduct op : products) {
                Product product = productMapper.selectById(op.getProductId());
                if (product != null) {
                    op.setProductImage(product.getImage());
                }
            }
            o.setProducts(products);
        }
    }

    /**
     * 修改订单收货地址
     *
     * 【前后端数据流】
     * 前端 PUT /api/orders/{id}/address
     * Body: { receiver: "张三", phone: "13800138000", address: "广东省..." }
     *
     * @param id     订单ID
     * @param userId 用户ID
     * @param params 地址参数Map
     */
    @Override
    public void updateOrderAddress(Long id, Long userId, Map<String, String> params) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (params.containsKey("receiver")) {
            order.setContactName(params.get("receiver"));
        }
        if (params.containsKey("phone")) {
            order.setContactPhone(params.get("phone"));
        }
        if (params.containsKey("address")) {
            order.setShippingAddress(params.get("address"));
        }
        orderMapper.updateById(order);
    }

    /**
     * 更换订单优惠券（仅限待支付状态）
     *
     * @param id          订单ID
     * @param userId      用户ID
     * @param newCouponId 新优惠券ID（为null表示取消优惠券）
     */
    @Override
    @Transactional
    public void changeCoupon(Long id, Long userId, Long newCouponId) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("只有待支付状态的订单可以更换优惠券");
        }

        // 如果订单已使用优惠券，先恢复旧优惠券
        if (order.getCouponId() != null) {
            UserCoupon oldCoupon = userCouponMapper.selectById(order.getCouponId());
            if (oldCoupon != null && oldCoupon.getStatus() == 2) {
                oldCoupon.setStatus(1);
                oldCoupon.setUseTime(null);
                oldCoupon.setOrderNo(null);
                oldCoupon.setDiscountAmount(null);
                oldCoupon.setUpdateTime(LocalDateTime.now());
                userCouponMapper.updateById(oldCoupon);
            }
        }

        BigDecimal couponAmount = BigDecimal.ZERO;
        Long couponId = null;
        String couponCode = null;

        if (newCouponId != null) {
            UserCoupon userCoupon = userCouponMapper.selectById(newCouponId);
            if (userCoupon == null) {
                throw new RuntimeException("优惠券不存在");
            }
            if (!userCoupon.getUserId().equals(userId)) {
                throw new RuntimeException("优惠券不属于当前用户");
            }
            if (userCoupon.getStatus() != 1) {
                throw new RuntimeException("优惠券已使用或已过期");
            }
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(userCoupon.getValidStart()) || now.isAfter(userCoupon.getValidEnd())) {
                throw new RuntimeException("优惠券已过期");
            }

            Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
            if (coupon != null && coupon.getMinAmount() != null
                    && coupon.getMinAmount().compareTo(BigDecimal.ZERO) > 0) {
                if (order.getTotalAmount().compareTo(coupon.getMinAmount()) < 0) {
                    throw new RuntimeException("订单金额不满足优惠券使用条件，满" + coupon.getMinAmount() + "元可用");
                }
            }

            if (coupon != null) {
                if (coupon.getType() == 1) {
                    couponAmount = coupon.getAmount();
                } else if (coupon.getType() == 2) {
                    couponAmount = order.getTotalAmount().multiply(BigDecimal.ONE.subtract(coupon.getAmount()));
                }
                if (couponAmount.compareTo(order.getTotalAmount()) > 0) {
                    couponAmount = order.getTotalAmount();
                }
            }

            couponId = newCouponId;
            couponCode = userCoupon.getCouponCode();
        }

        BigDecimal actualAmount = order.getTotalAmount().subtract(couponAmount);
        if (actualAmount.compareTo(BigDecimal.ZERO) < 0) {
            actualAmount = BigDecimal.ZERO;
        }

        order.setActualAmount(actualAmount);
        order.setCouponId(couponId);
        order.setCouponAmount(couponAmount);
        order.setCouponCode(couponCode);
        orderMapper.updateById(order);
    }
}
