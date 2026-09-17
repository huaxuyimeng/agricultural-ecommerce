package com.example.vuehouduan.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.vuehouduan.common.Result;
import com.example.vuehouduan.entity.User;
import com.example.vuehouduan.mapper.UserMapper;
import com.example.vuehouduan.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * ============================================================
 * 用户管理控制器 - 用户CRUD操作
 * ============================================================
 *
 * 【前后端数据传递方式总结】
 *
 * 1. @RequestParam - URL查询参数（GET请求）
 *    前端：axios.get('/api/users?page=1&pageSize=10&role=USER')
 *    后端：@RequestParam Integer page 接收
 *
 * 2. @PathVariable - URL路径参数
 *    前端：axios.get('/api/users/5')
 *    后端：@PathVariable Long id 接收 → id=5
 *
 * 3. @RequestBody - 请求体JSON（POST/PUT请求）
 *    前端：axios.post('/api/users', { username:'xxx', name:'xxx' })
 *    后端：@RequestBody User user 接收 → Spring自动将JSON转为User对象
 *
 * 4. request.getAttribute() - 从JWT拦截器获取用户信息
 *    前端：axios请求头自动携带 Authorization: Bearer xxx
 *    后端：JwtInterceptor解析token → request.setAttribute("userId", 1)
 *         Controller中 request.getAttribute("userId") 获取
 *
 * 【分页查询流程】
 * 前端传 page(页码) + pageSize(每页条数)
 * 后端用 MyBatis-Plus 的 Page 对象分页
 * 返回 { list: [...], total: 100 } → 前端计算总页数 = total/pageSize
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取用户列表（支持分页、角色筛选、状态筛选、关键词搜索）
     *
     * 【前端请求示例】
     * GET /api/users?page=1&pageSize=10&role=USER&status=1&keyword=张三
     *
     * 【后端响应示例】
     * { code:200, data:{ list:[{id:1,username:"zhangsan",...},...], total:25 } }
     *
     * 【MyBatis-Plus QueryWrapper 原理】
     * QueryWrapper 是 MyBatis-Plus 提供的条件构造器，用于动态构建SQL WHERE条件
     * wrapper.eq("role", "USER") → WHERE role = 'USER'
     * wrapper.like("username", "张三") → WHERE username LIKE '%张三%'
     * 最终生成SQL：SELECT * FROM users WHERE role='USER' AND username LIKE '%张三%' ORDER BY create_time DESC LIMIT 0,10
     */
    @ApiOperation("获取用户列表")
    @GetMapping
    public Result<?> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        Page<User> pageObj = new Page<>(page, pageSize);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (role != null && !role.isEmpty()) {
            wrapper.eq("role", role);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("username", keyword).or().like("name", keyword).or().like("phone", keyword));
        }
        wrapper.orderByDesc("create_time");
        Page<User> result = userMapper.selectPage(pageObj, wrapper);
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        return Result.success(map);
    }

    /**
     * 获取用户详情
     *
     * 【前端请求示例】
     * GET /api/users/5
     *
     * 【@PathVariable 原理】
     * URL中的 {id} 占位符会被实际值替换
     * /api/users/5 → id = 5
     */
    @ApiOperation("获取用户详情")
    @GetMapping("/{id}")
    public Result<?> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(user);
    }

    /**
     * 创建用户（管理员功能）
     *
     * 【前端请求示例】
     * POST /api/users
     * Body: { "username":"newuser", "password":"123456", "name":"新用户", "role":"USER" }
     *
     * 【@RequestBody 原理】
     * Spring使用 Jackson（JSON库）将请求体JSON自动反序列化为User对象
     * JSON的字段名与User类的属性名一一对应
     */
    @ApiOperation("创建用户")
    @PostMapping
    public Result<?> createUser(@RequestBody User user) {
        try {
            User created = userService.createUser(user);
            return Result.success(created);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 获取当前登录用户信息
     *
     * 【数据来源】
     * userId 不是前端传的，而是 JwtInterceptor 从 token 中解析出来
     * 存入 request.setAttribute("userId", userId)
     * 这里通过 request.getAttribute("userId") 获取
     *
     * 【为什么这样设计？】
     * 安全性：前端无法伪造userId，因为token是加密的
     * 便捷性：前端不需要传userId参数，后端自动识别当前用户
     */
    @ApiOperation("获取当前登录用户")
    @GetMapping("/me")
    public Result<?> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        Map<String, Object> result = userService.getUserInfo(userId);
        return Result.success(result.get("user"));
    }

    /**
     * 更新用户信息
     *
     * 【权限控制】
     * 普通用户只能修改自己的信息
     * 管理员可以修改任意用户的信息
     * 通过比较 currentUserId 和路径中的 id，以及检查 role 来实现
     */
    @ApiOperation("更新用户信息")
    @PutMapping("/{id}")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody User user, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId == null) {
            return Result.error(401, "未登录");
        }
        if (!currentUserId.equals(id)) {
            String role = (String) request.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                return Result.error(403, "无权操作");
            }
        }
        try {
            User updated = userService.updateUser(id, user);
            return Result.success(updated);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success("删除成功");
    }

    @ApiOperation("批量删除用户")
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@RequestBody List<Long> ids) {
        userService.removeByIds(ids);
        return Result.success("批量删除成功");
    }

    @ApiOperation("账户充值")
    @PutMapping("/{id}/charge")
    public Result<?> chargeAccount(@PathVariable Long id, @RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId == null) {
            return Result.error(401, "未登录");
        }
        if (!currentUserId.equals(id)) {
            return Result.error(403, "无权操作");
        }
        double amount = 0;
        if (params != null && params.containsKey("amount")) {
            amount = Double.parseDouble(params.get("amount").toString());
        }
        if (amount <= 0) {
            return Result.error(400, "充值金额必须大于0");
        }
        try {
            User user = userService.chargeAccount(id, amount);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}