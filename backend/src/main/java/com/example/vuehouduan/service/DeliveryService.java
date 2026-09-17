package com.example.vuehouduan.service;

import java.util.Map;

public interface DeliveryService {
    Map<String, Object> getLogisticsByOrderId(String orderId);
}
