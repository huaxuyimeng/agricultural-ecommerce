package com.example.vuehouduan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.vuehouduan.common.RegisterRequest;
import com.example.vuehouduan.entity.User;
import java.util.Map;

public interface UserService extends IService<User> {
    Map<String, Object> login(String username, String password, String role);
    User register(RegisterRequest request);
    User createUser(User user);
    Map<String, Object> getUserInfo(Long userId);
    User updateUser(Long userId, User user);
    void resetPassword(Long userId, String newPassword);
    User chargeAccount(Long userId, double amount);
    void approveUser(Long userId);
    void rejectUser(Long userId);
    void deleteUser(Long userId);
}
