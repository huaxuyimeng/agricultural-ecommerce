package com.example.vuehouduan.controller;

import com.example.vuehouduan.common.Result;
import com.example.vuehouduan.entity.Order;
import com.example.vuehouduan.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * ============================================================
 * 订单控制器 - 订单全生命周期管理
 * ============================================================
 *
 * 【订单状态流转（核心业务流程）】
 *
 * 用户下单 → PENDING（待支付）
 * ↓ 支付
 * PAID（已支付，等待商家发货）
 * ↓ 商家发货
 * SHIPPED（已发货，等待用户收货）
 * ↓ 用户确认收货
 * DELIVERED/COMPLETED（已完成）
 *
 * 任意非终态 → CANCELLED（已取消）
 *
 * 【前后端订单创建流程】
 * 1. 前端购物车页面 → 用户点击"结算"
 * 2. 前端 POST /api/orders
 * Body: { products:[{productId:1,count:2}], shippingAddress:"xxx",
 * paymentMethod:"alipay" }
 * 3. 后端 OrderController.createOrder()
 * → orderService.createOrder() 创建订单
 * → 扣减库存、增加销量
 * → 清除购物车中已购买的商品
 * → 返回订单对象
 * 4. 前端收到订单 → 跳转支付页面或订单详情
 *
 * 【角色权限说明】
 * USER(买家)：创建订单、支付、确认收货、查看自己的订单
 * MERCHANT(卖家)：查看自己商品的订单、发货
 * ADMIN(管理员)：查看所有订单
 */
@Api(tags = "订单管理")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单（用户下单）
     *
     * 【前端请求示例】
     * POST /api/orders
     * Body: {
     * "products": [{"productId": 1, "count": 2}, {"productId": 3, "count": 1}],
     * "shippingAddress": "北京市朝阳区xxx",
     * "receiverName": "张三",
     * "shippingPhone": "13800138000",
     * "paymentMethod": "alipay",
     * "remark": "请尽快发货"
     * }
     *
     * 【后端处理流程】
     * 1. 从token获取userId（谁下的单）
     * 2. 遍历products，查询商品信息，计算总价
     * 3. 扣减库存，增加销量
     * 4. 生成订单号（时间戳+随机数）
     * 5. 保存订单和订单商品关联
     * 6. 清除购物车中已购买的商品
     *
     * 【@Transactional 事务保证】
     * 如果任何一步失败（如库存不足），所有操作回滚，保证数据一致性
     */
    @ApiOperation("创建订单")
    @PostMapping
    public Result<?> createOrder(@RequestBody Map<String, Object> orderData, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            Order order = orderService.createOrder(userId, orderData);
            return Result.success(order);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 获取订单列表（根据角色返回不同数据）
     *
     * ADMIN → 查看所有订单
     * USER → 只查看自己的订单
     * MERCHANT → 查看自己商品的订单（通过 MerchantOrderController）
     */
    @ApiOperation("获取订单列表")
    @GetMapping
    public Result<?> getOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        Map<String, Object> result;
        if ("ADMIN".equals(role)) {
            result = orderService.getAllOrders(page, pageSize, status, null);
        } else {
            result = orderService.getUserOrders(userId, page, pageSize, status);
        }
        return Result.success(result);
    }

    /**
     * 获取订单详情
     * 支持两种ID格式：数字ID（数据库主键）和字符串orderId（订单号如20240501120000123）
     */
    @ApiOperation("获取订单详情")
    @GetMapping("/{id}")
    public Result<?> getOrderById(@PathVariable String id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        try {
            Order order;
            try {
                Long numericId = Long.parseLong(id);
                order = orderService.getOrderById(numericId, userId, role);
            } catch (NumberFormatException e) {
                order = orderService.getOrderByOrderId(id, userId, role);
            }
            return Result.success(order);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 取消订单
     * 只有 PENDING（待支付）状态的订单可以取消
     */
    @ApiOperation("取消订单")
    @PutMapping("/{id}/cancel")
    public Result<?> cancelOrder(@PathVariable Long id, @RequestBody(required = false) Map<String, String> params,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String reason = params != null ? params.get("reason") : null;
        try {
            orderService.cancelOrder(id, userId, reason);
            return Result.success("取消成功");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 支付订单
     * 将订单状态从 PENDING 改为 PAID
     */
    @ApiOperation("支付订单")
    @PutMapping("/{id}/pay")
    public Result<?> payOrder(@PathVariable Long id, @RequestBody Map<String, String> params,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String paymentMethod = params.get("paymentMethod") != null ? params.get("paymentMethod").toString() : "balance";
        try {
            orderService.payOrder(id, userId, paymentMethod);
            return Result.success(orderService.getById(id));
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 确认收货（买家操作）
     * 将订单状态从 SHIPPED 改为 DELIVERED
     */
    @ApiOperation("确认收货")
    @PutMapping("/{id}/confirm")
    public Result<?> confirmReceive(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            orderService.confirmReceive(id, userId);
            return Result.success("确认收货成功");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 获取订单统计（各状态数量）
     * 前端用于显示"待付款(3) 待发货(2) 待收货(1)"等标签
     */
    @ApiOperation("获取订单统计")
    @GetMapping("/stats")
    public Result<?> getOrderStats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(orderService.getOrderStats(userId));
    }

    @ApiOperation("更新订单地址")
    @PutMapping("/{id}/address")
    public Result<?> updateOrderAddress(@PathVariable Long id, @RequestBody Map<String, String> params,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            orderService.updateOrderAddress(id, userId, params);
            return Result.success("地址更新成功");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 更换订单优惠券
     * 仅限待支付状态的订单
     */
    @ApiOperation("更换优惠券")
    @PutMapping("/{id}/coupon")
    public Result<?> changeCoupon(@PathVariable Long id, @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long newCouponId = null;
        if (params != null && params.containsKey("couponId")) {
            Object val = params.get("couponId");
            if (val != null) {
                newCouponId = Long.valueOf(val.toString());
            }
        }
        try {
            orderService.changeCoupon(id, userId, newCouponId);
            return Result.success(orderService.getById(id));
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}