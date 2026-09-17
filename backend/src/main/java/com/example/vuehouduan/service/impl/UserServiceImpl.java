package com.example.vuehouduan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vuehouduan.common.RegisterRequest;
import com.example.vuehouduan.entity.Merchant;
import com.example.vuehouduan.entity.User;
import com.example.vuehouduan.mapper.MerchantMapper;
import com.example.vuehouduan.mapper.UserMapper;
import com.example.vuehouduan.service.UserService;
import com.example.vuehouduan.util.JwtUtil;
import com.example.vuehouduan.util.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 * 用户服务实现 - 用户认证、注册、信息管理
 * ============================================================
 *
 * 【前后端用户认证流程】
 *
 * 1. 登录流程：
 * 前端 POST /api/auth/login
 * Body: { username:"xxx", password:"xxx", role:"USER" }
 * → AuthController.login() 调用本服务的 login()
 * → 验证用户名密码 → 生成JWT Token → 返回 { token, user }
 * → 前端将 token 存入 localStorage
 * → 后续所有请求在 Header 中携带: Authorization: Bearer <token>
 * → JwtInterceptor 拦截器验证 token 有效性
 *
 * 2. 注册流程：
 * 前端 POST /api/auth/register
 * Body: { username, password, name, role, email, phone, ... }
 * → 检查用户名是否已存在
 * → 创建 User 记录
 * → 如果是商家(MERCHANT)，同时创建 Merchant 记录（待审核状态）
 * → 返回用户对象
 *
 * 3. Token 机制：
 * - JWT(JSON Web Token) 包含用户ID、用户名、角色
 * - 无状态认证，服务端不需要存储 session
 * - 每次请求携带 token，拦截器解析后注入到 request 属性中
 * - Controller 通过 @RequestAttribute 获取当前用户信息
 *
 * 【数据表关系】
 * user 表：存储所有用户（普通用户、商家、管理员）
 * merchant 表：存储商家额外信息（店铺名、营业执照等），通过 user.id 关联
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     *
     * 【前后端数据流】
     * 前端 → POST /api/auth/login { username, password, role }
     * 后端 → 查询用户 → 验证密码 → 生成JWT → 更新最后登录时间
     * 返回 → { token: "eyJhbG...", user: { id, username, role, ... } }
     *
     * 【前端收到响应后】
     * localStorage.setItem('token', data.token)
     * localStorage.setItem('user', JSON.stringify(data.user))
     * 后续请求自动在 axios 拦截器中添加 Authorization header
     *
     * @param username 用户名
     * @param password 明文密码
     * @param role     角色（USER/MERCHANT/ADMIN），null表示不限制
     * @return 包含 token 和 user 对象的 Map
     */
    @Override
    public Map<String, Object> login(String username, String password, String role) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        if (role != null && !role.isEmpty()) {
            wrapper.eq("role", role);
        }
        User user = baseMapper.selectOne(wrapper);
        if (user == null) {
            throw new RuntimeException("用户名不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("该账号已被禁用");
        }

        String storedPassword = user.getPassword();

        if (storedPassword == null || storedPassword.isEmpty()) {
            throw new RuntimeException("用户密码未设置");
        }

        // 兼容历史明文密码：自动升级为 BCrypt 加密
        if (!storedPassword.startsWith("$2a$") && !storedPassword.startsWith("$2b$") && !storedPassword.startsWith("$2y$")) {
            if (!storedPassword.equals(password)) {
                throw new RuntimeException("密码错误");
            }
            // 验证通过，自动升级为 BCrypt 哈希
            user.setPassword(passwordEncoder.encode(password));
            baseMapper.updateById(user);
        } else if (!passwordEncoder.matches(password, storedPassword)) {
            throw new RuntimeException("密码错误");
        }

        String token = jwtUtil.createToken(user.getId(), user.getUsername(), user.getRole());
        user.setToken(token);
        user.setLastLoginTime(LocalDateTime.now());
        baseMapper.updateById(user);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    /**
     * 用户注册
     *
     * 【前后端数据流】
     * 前端 → POST /api/auth/register
     * Body: {
     * username, password, name, role("USER"或"MERCHANT"),
     * email, phone, studentId, gender,
     * shopName, shopAddress, shopDescription, businessLicense // 商家额外字段
     * }
     * 后端 → 检查用户名唯一性 → 创建User → 如果是商家则创建Merchant(状态PENDING)
     * 返回 → 用户对象
     *
     * 【事务管理 @Transactional】
     * 确保 User 和 Merchant 两张表的插入要么同时成功，要么同时回滚
     *
     * @param request 注册请求对象，包含用户基本信息和商家信息
     * @return 创建成功的用户对象
     */
    @Override
    @Transactional
    public User register(RegisterRequest request) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", request.getUsername());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        // 使用 BCrypt 加密存储密码
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole(request.getRole() != null ? request.getRole() : "USER");
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStudentId(request.getStudentId());
        user.setGender(request.getGender());
        user.setAccount(BigDecimal.ZERO);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(user);

        if ("MERCHANT".equals(request.getRole())) {
            Merchant merchant = new Merchant();
            merchant.setId(user.getId());
            merchant.setShopName(request.getShopName());
            merchant.setShopAddress(request.getShopAddress());
            merchant.setShopDescription(request.getShopDescription());
            merchant.setBusinessLicense(request.getBusinessLicense());
            merchant.setApprovalStatus("PENDING");
            merchantMapper.insert(merchant);
        }
        return user;
    }

    /**
     * 管理员创建用户
     *
     * 【前后端数据流】
     * 前端管理员页面 → POST /api/admin/users
     * Body: { username, password, name, role, email, phone, ... }
     * 后端 → 检查用户名唯一性 → 设置默认值 → 插入数据库
     * 返回 → 创建的用户对象
     *
     * @param user 用户对象（由前端JSON自动反序列化）
     * @return 创建成功的用户对象
     */
    @Override
    @Transactional
    public User createUser(User user) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        // 使用 BCrypt 加密存储密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getRole() == null) {
            user.setRole("USER");
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(user);
        return user;
    }

    /**
     * 获取用户个人信息
     *
     * 【前后端数据流】
     * 前端 → GET /api/user/info（Header携带token）
     * 后端 → JwtInterceptor解析token获取userId → 查询user表
     * → 如果是商家，额外查询merchant表合并信息
     * 返回 → { user: { id, username, name, avatar, shopName, ... } }
     *
     * 【安全注意】
     * - 密码字段设为null，不返回给前端
     * - 商家用户自动合并 merchant 表的店铺信息到 user 对象中
     *
     * @param userId 用户ID（从JWT token中解析）
     * @return 包含用户信息的Map
     */
    @Override
    public Map<String, Object> getUserInfo(Long userId) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 不返回密码
        user.setPassword(null);
        // 对于商家用户，复制商家信息
        if ("MERCHANT".equals(user.getRole())) {
            Merchant merchant = merchantMapper.selectById(userId);
            if (merchant != null) {
                user.setAvatar(merchant.getShopLogo());
                user.setShopName(merchant.getShopName());
                user.setShopAddress(merchant.getShopAddress());
                user.setShopDescription(merchant.getShopDescription());
                user.setShopLogo(merchant.getShopLogo());
                user.setBusinessLicense(merchant.getBusinessLicense());
                user.setApprovalStatus(merchant.getApprovalStatus());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        return result;
    }

    /**
     * 更新用户个人信息
     *
     * 【前后端数据流】
     * 前端个人中心 → PUT /api/user/update
     * Body: { name, email, phone, avatar, description, ... }
     * 后端 → 查询现有用户 → 只更新非null字段（部分更新） → 保存
     * 返回 → 更新后的用户对象
     *
     * 【设计说明】
     * 采用"非null即更新"策略，前端只需传需要修改的字段
     *
     * @param userId 当前登录用户ID
     * @param user   包含要更新字段的用户对象
     * @return 更新后的用户对象
     */
    @Override
    public User updateUser(Long userId, User user) {
        User existUser = baseMapper.selectById(userId);
        if (existUser == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getName() != null)
            existUser.setName(user.getName());
        if (user.getEmail() != null) {
            QueryWrapper<User> emailWrapper = new QueryWrapper<>();
            emailWrapper.eq("email", user.getEmail()).ne("id", userId);
            if (baseMapper.selectCount(emailWrapper) > 0) {
                throw new RuntimeException("邮箱已被其他用户使用");
            }
            existUser.setEmail(user.getEmail());
        }
        if (user.getPhone() != null) {
            QueryWrapper<User> phoneWrapper = new QueryWrapper<>();
            phoneWrapper.eq("phone", user.getPhone()).ne("id", userId);
            if (baseMapper.selectCount(phoneWrapper) > 0) {
                throw new RuntimeException("手机号已被其他用户使用");
            }
            existUser.setPhone(user.getPhone());
        }
        if (user.getAvatar() != null)
            existUser.setAvatar(user.getAvatar());
        if (user.getDescription() != null)
            existUser.setDescription(user.getDescription());
        if (user.getStudentId() != null)
            existUser.setStudentId(user.getStudentId());
        if (user.getGender() != null)
            existUser.setGender(user.getGender());
        if (user.getBirthday() != null)
            existUser.setBirthday(user.getBirthday());
        if (user.getAddress() != null)
            existUser.setAddress(user.getAddress());
        existUser.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(existUser);
        return existUser;
    }

    /**
     * 重置密码
     *
     * 【前后端数据流】
     * 前端 → PUT /api/user/reset-password
     * Body: { newPassword: "xxx" }
     * 后端 → 查询用户 → 更新密码 → 保存
     *
     * @param userId      用户ID
     * @param newPassword 新密码（明文）
     */
    @Override
    public void resetPassword(Long userId, String newPassword) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 使用 BCrypt 加密存储密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(user);
    }

    /**
     * 账户充值
     *
     * 【前后端数据流】
     * 前端 → POST /api/user/charge
     * Body: { amount: 100.00 }
     * 后端 → 查询用户 → 余额累加 → 保存
     * 返回 → 更新后的用户对象（含新余额）
     *
     * @param userId 用户ID
     * @param amount 充值金额
     * @return 更新后的用户对象
     */
    @Override
    public User chargeAccount(Long userId, double amount) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getAccount() == null) {
            user.setAccount(BigDecimal.ZERO);
        }
        user.setAccount(user.getAccount().add(BigDecimal.valueOf(amount)));
        user.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(user);
        return user;
    }

    /**
     * 审核通过商家入驻申请
     *
     * 【前后端数据流】
     * 前端管理员 → PUT /api/admin/users/{id}/approve
     * 后端 → 查询用户 → 更新merchant表审批状态为APPROVED
     *
     * @param userId 商家用户ID
     */
    @Override
    public void approveUser(Long userId) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if ("MERCHANT".equals(user.getRole())) {
            Merchant merchant = merchantMapper.selectById(userId);
            if (merchant != null) {
                merchant.setApprovalStatus("APPROVED");
                merchant.setApproveTime(LocalDateTime.now());
                merchantMapper.updateById(merchant);
            }
        }
    }

    /**
     * 拒绝商家入驻申请
     *
     * @param userId 商家用户ID
     */
    @Override
    public void rejectUser(Long userId) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if ("MERCHANT".equals(user.getRole())) {
            Merchant merchant = merchantMapper.selectById(userId);
            if (merchant != null) {
                merchant.setApprovalStatus("REJECTED");
                merchant.setApproveTime(LocalDateTime.now());
                merchantMapper.updateById(merchant);
            }
        }
    }

    /**
     * 删除用户
     *
     * @param userId 用户ID
     */
    @Override
    public void deleteUser(Long userId) {
        baseMapper.deleteById(userId);
    }
}
