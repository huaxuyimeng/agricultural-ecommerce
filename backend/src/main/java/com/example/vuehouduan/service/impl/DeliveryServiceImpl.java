package com.example.vuehouduan.service.impl;

import com.example.vuehouduan.entity.Delivery;
import com.example.vuehouduan.entity.DeliveryTracking;
import com.example.vuehouduan.entity.Order;
import com.example.vuehouduan.mapper.DeliveryMapper;
import com.example.vuehouduan.mapper.DeliveryTrackingMapper;
import com.example.vuehouduan.mapper.OrderMapper;
import com.example.vuehouduan.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    @Autowired
    private DeliveryMapper deliveryMapper;

    @Autowired
    private DeliveryTrackingMapper deliveryTrackingMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public Map<String, Object> getLogisticsByOrderId(String orderId) {
        Map<String, Object> result = new HashMap<>();

        // 尝试用订单号查询
        Delivery delivery = deliveryMapper.selectByOrderId(orderId);
        
        // 如果没找到，尝试用数字ID查询orders表获取order_id
        if (delivery == null) {
            try {
                Long numericId = Long.parseLong(orderId);
                Order orderById = orderMapper.selectById(numericId);
                if (orderById != null) {
                    delivery = deliveryMapper.selectByOrderId(orderById.getOrderId());
                    orderId = orderById.getOrderId(); // 更新orderId为实际的订单号
                }
            } catch (NumberFormatException e) {
                // 不是数字ID，忽略
            }
        }
        
        if (delivery == null) {
            result.put("success", false);
            result.put("message", "暂无物流信息");
            return result;
        }

        Order order = orderMapper.selectByOrderId(orderId);

        List<DeliveryTracking> trackingList = deliveryTrackingMapper.selectByDeliveryId(delivery.getId());
        List<Map<String, String>> tracking = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");

        for (int i = 0; i < trackingList.size(); i++) {
            DeliveryTracking t = trackingList.get(i);
            Map<String, String> item = new HashMap<>();
            item.put("time", t.getCreateTime() != null ? t.getCreateTime().format(formatter) : "");
            item.put("status", t.getDescription());
            item.put("location", t.getLocation());
            item.put("completed", String.valueOf(i < trackingList.size() - 1));
            tracking.add(item);
        }

        result.put("success", true);
        result.put("company", delivery.getExpressCompany());
        result.put("trackingNo", delivery.getExpressNumber());
        result.put("status", delivery.getStatus());
        result.put("currentLocation", delivery.getCurrentLocation());
        result.put("shipTime", delivery.getShipTime() != null ? delivery.getShipTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
        result.put("signedTime", delivery.getSignedTime() != null ? delivery.getSignedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");

        if (order != null) {
            result.put("receiver", order.getContactName());
            result.put("receiverAddress", order.getShippingAddress());
        }

        result.put("tracking", tracking);
        return result;
    }
}
