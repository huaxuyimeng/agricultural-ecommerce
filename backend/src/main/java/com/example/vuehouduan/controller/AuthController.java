package com.example.vuehouduan.controller;

import com.example.vuehouduan.common.LoginRequest;
import com.example.vuehouduan.common.RegisterRequest;
import com.example.vuehouduan.common.Result;
import com.example.vuehouduan.entity.User;
import com.example.vuehouduan.service.LoginHistoryService;
import com.example.vuehouduan.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * ============================================================
 * 认证控制器 - 处理登录、注册、登出
 * ============================================================
 *
 * 【前后端登录流程详解】
 *
 * 1. 前端用户填写表单 → 点击登录按钮
 * 2. 前端 axios.post('/api/auth/login', { username, password, role })
 *    → JSON数据通过HTTP请求体（@RequestBody）发送到后端
 * 3. 后端 AuthController.login() 接收请求
 *    → 调用 userService.login() 验证用户名密码
 *    → 验证通过后调用 jwtUtil.createToken() 生成JWT
 *    → 返回 { code:200, data:{ token:"xxx", user:{...} } }
 * 4. 前端收到响应 → localStorage.setItem('token', token)
 *    → 跳转到首页
 *
 * 【前后端注册流程详解】
 *
 * 1. 前端填写注册表单 → axios.post('/api/auth/register', { username, password, name, role, ... })
 * 2. 后端 AuthController.register() 接收请求
 *    → 调用 userService.register() 创建用户
 *    → 如果是MERCHANT角色，同时创建商家记录
 *    → 返回 { code:200, data: user对象 }
 * 3. 前端收到成功响应 → 提示注册成功 → 跳转登录页
 *
 * 【@RestController 原理】
 * @RestController = @Controller + @ResponseBody
 * 方法的返回值会自动序列化为JSON字符串写入HTTP响应体
 *
 * 【@RequestMapping("/api/auth") 原理】
 * 类上的 @RequestMapping 定义了这个Controller所有接口的URL前缀
 * 方法上的 @PostMapping("/login") 定义子路径
 * 完整URL = 前缀 + 子路径 = /api/auth/login
 */
@Api(tags = "认证管理")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private LoginHistoryService loginHistoryService;

    /**
     * 用户登录
     *
     * 【前端请求示例】
     * POST /api/auth/login
     * Content-Type: application/json
     * Body: { "username": "admin", "password": "123456", "role": "ADMIN" }
     *
     * 【后端响应示例】
     * { code:200, message:"操作成功", data:{ token:"eyJhbG...", user:{ id:1, username:"admin", ... } } }
     *
     * 【参数说明】
     * @RequestBody LoginRequest request → Spring自动将请求体JSON反序列化为LoginRequest对象
     *   LoginRequest包含：username(用户名), password(密码), role(角色)
     * @param httpRequest → 获取客户端IP和User-Agent，用于记录登录日志
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            String ip = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            Map<String, Object> result = userService.login(request.getUsername(), request.getPassword(), request.getRole());
            User user = (User) result.get("user");
            loginHistoryService.recordLogin(user.getId(), user.getUsername(), ip, userAgent, "SUCCESS", null);
            return Result.success(result);
        } catch (RuntimeException e) {
            String ip = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            loginHistoryService.recordLogin(null, request.getUsername(), ip, userAgent, "FAILED", e.getMessage());
            return Result.error(401, e.getMessage());
        }
    }

    /**
     * 用户注册
     *
     * 【前端请求示例】
     * POST /api/auth/register
     * Body: { "username":"newuser", "password":"123456", "name":"新用户", "role":"USER", "phone":"13800138000" }
     *
     * 【后端响应示例】
     * { code:200, message:"操作成功", data:{ id:5, username:"newuser", name:"新用户", role:"USER", ... } }
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 用户登出
     * 前端清除本地token即可，后端JWT无状态不需要额外处理
     */
    @ApiOperation("用户登出")
    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        return Result.success("登出成功");
    }

    @ApiOperation("刷新Token")
    @PostMapping("/refresh")
    public Result<?> refresh(@RequestBody Map<String, String> params) {
        return Result.success();
    }

    /**
     * 获取客户端真实IP地址
     * 考虑了反向代理的情况（Nginx等会在X-Forwarded-For头中传递真实IP）
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}