package com.example.vuehouduan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.vuehouduan.entity.Order;
import java.util.Map;

public interface OrderService extends IService<Order> {
    Order createOrder(Long userId, Map<String, Object> orderData);

    Map<String, Object> getUserOrders(Long userId, Integer page, Integer pageSize, String status);

    Order getOrderById(Long id, Long userId, String role);

    Order getOrderByOrderId(String orderId, Long userId, String role);

    void cancelOrder(Long id, Long userId, String reason);

    void payOrder(Long id, Long userId, String paymentMethod);

    void confirmReceive(Long id, Long userId);

    Map<String, Object> getMerchantOrders(Long merchantId, Integer page, Integer pageSize, String status);

    void shipOrder(Long id, Long merchantId, String expressCompany, String expressNo);

    Map<String, Object> getAllOrders(Integer page, Integer pageSize, String status, String keyword);

    Map<String, Object> getOrderStats(Long userId);

    void updateOrderAddress(Long id, Long userId, Map<String, String> params);

    void changeCoupon(Long id, Long userId, Long newCouponId);
}
