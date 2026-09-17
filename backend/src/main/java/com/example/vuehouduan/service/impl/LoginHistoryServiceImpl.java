package com.example.vuehouduan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vuehouduan.entity.LoginHistory;
import com.example.vuehouduan.mapper.LoginHistoryMapper;
import com.example.vuehouduan.service.LoginHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 * 登录历史服务实现 - 登录记录管理
 * ============================================================
 *
 * 【前后端登录记录流程】
 *
 * 1. 记录登录：
 * 用户登录成功后由 AuthController 调用
 * 后端 → 插入登录记录（用户ID、IP、UserAgent、登录时间、状态）
 *
 * 2. 查看个人登录历史：
 * 前端个人中心 → GET /api/user/login-history?page=1&pageSize=10
 * 后端 → 根据userId查询 → 按登录时间降序 → 分页返回
 *
 * 3. 查看所有登录历史（管理员）：
 * 前端管理员页面 → GET /api/admin/login-history?page=1&userId=&username=
 * 后端 → 支持按用户ID和用户名筛选 → 分页返回
 *
 * 4. 记录登出：
 * 用户登出时调用
 * 后端 → 更新登出时间 → 计算在线时长
 */
@Service
public class LoginHistoryServiceImpl extends ServiceImpl<LoginHistoryMapper, LoginHistory>
        implements LoginHistoryService {

    /**
     * 记录用户登录
     *
     * 【前后端数据流】
     * 用户登录成功后由 AuthController.login() 调用
     * 后端 → 插入登录记录（用户ID、IP、UserAgent、登录时间、状态）
     *
     * @param userId    用户ID
     * @param username  用户名
     * @param ip        登录IP地址
     * @param userAgent 浏览器UserAgent
     * @param status    登录状态（success/failed）
     * @param errorMsg  错误信息（登录失败时）
     */
    @Override
    public void recordLogin(Long userId, String username, String ip, String userAgent, String status, String errorMsg) {
        LoginHistory history = new LoginHistory();
        history.setUserId(userId);
        history.setUsername(username);
        history.setIpAddress(ip);
        history.setUserAgent(userAgent);
        history.setLoginTime(LocalDateTime.now());
        history.setLoginStatus(status);
        history.setErrorMessage(errorMsg);
        baseMapper.insert(history);
    }

    /**
     * 获取用户个人登录历史（分页）
     *
     * 【前后端数据流】
     * 前端个人中心 → GET /api/user/login-history?page=1&pageSize=10
     * 后端 → 根据userId查询 → 按登录时间降序 → 分页返回
     */
    @Override
    public Map<String, Object> getLoginHistory(Long userId, Integer page, Integer pageSize) {
        Page<LoginHistory> pageObj = new Page<>(page, pageSize);
        QueryWrapper<LoginHistory> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("login_time");
        Page<LoginHistory> result = baseMapper.selectPage(pageObj, wrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        return map;
    }

    /**
     * 获取所有用户登录历史（管理员端，分页）
     *
     * 【前后端数据流】
     * 前端管理员页面 → GET /api/admin/login-history?page=1&userId=&username=
     * 后端 → 支持按用户ID和用户名筛选 → 分页返回
     */
    @Override
    public Map<String, Object> getAllLoginHistory(Integer page, Integer pageSize, Long userId, String username) {
        Page<LoginHistory> pageObj = new Page<>(page, pageSize);
        QueryWrapper<LoginHistory> wrapper = new QueryWrapper<>();
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        if (StringUtils.hasText(username)) {
            wrapper.like("username", username);
        }
        wrapper.orderByDesc("login_time");
        Page<LoginHistory> result = baseMapper.selectPage(pageObj, wrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        return map;
    }

    /**
     * 记录用户登出
     *
     * 【前后端数据流】
     * 用户登出时由 AuthController.logout() 调用
     * 后端 → 更新登出时间 → 计算在线时长（秒）
     *
     * @param id 登录记录ID
     */
    @Override
    public void recordLogout(Long id) {
        LoginHistory history = baseMapper.selectById(id);
        if (history != null && history.getLogoutTime() == null) {
            LocalDateTime now = LocalDateTime.now();
            history.setLogoutTime(now);
            if (history.getLoginTime() != null) {
                long seconds = java.time.Duration.between(history.getLoginTime(), now).getSeconds();
                history.setDuration((int) seconds);
            }
            baseMapper.updateById(history);
        }
    }
}
