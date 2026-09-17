package com.example.vuehouduan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.vuehouduan.entity.LoginHistory;
import java.util.Map;

public interface LoginHistoryService extends IService<LoginHistory> {
    void recordLogin(Long userId, String username, String ip, String userAgent, String status, String errorMsg);
    Map<String, Object> getLoginHistory(Long userId, Integer page, Integer pageSize);
    Map<String, Object> getAllLoginHistory(Integer page, Integer pageSize, Long userId, String username);
    void recordLogout(Long id);
}
