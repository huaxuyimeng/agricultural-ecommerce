package com.example.vuehouduan.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 * JWT（JSON Web Token）工具类 - 用户身份认证的核心
 * ============================================================
 *
 * 【什么是JWT？】
 * JWT是一种无状态的认证方式。用户登录成功后，服务器生成一个加密的token字符串
 * 返回给前端。之后前端每次请求都携带这个token，服务器通过解析token来识别用户身份。
 *
 * 【为什么用JWT而不是Session？】
 * Session：服务器需要存储用户状态 → 占用内存 → 分布式部署需要共享Session → 复杂
 * JWT：   用户信息编码在token中 → 服务器无需存储 → 无状态 → 适合分布式/微服务
 *
 * 【JWT结构（三段式，用 . 分隔）】
 * Header.Payload.Signature
 * Header:   {"alg":"HS512","typ":"JWT"}  → Base64编码
 * Payload:  {"userId":1,"username":"admin","role":"ADMIN","exp":过期时间} → Base64编码
 * Signature: 用密钥对前两段签名 → 防止篡改
 *
 * 【前后端JWT交互流程】
 * 1. 前端 POST /api/auth/login { username, password }
 * 2. 后端验证密码 → 调用 createToken() 生成JWT → 返回 { token: "xxx", user: {...} }
 * 3. 前端 localStorage.setItem('token', token) 保存
 * 4. 后续请求 axios 拦截器自动在 Header 加 Authorization: Bearer xxx
 * 5. 后端 JwtInterceptor 拦截 → parseToken() 解析 → 获取 userId/username/role
 * 6. token过期 → 前端收到401 → 跳转登录页
 *
 * 【配置来源】
 * jwt.secret 和 jwt.expiration 从 application.yml 中读取：
 * jwt:
 *   secret: vuehouduan_secret_key_2024_very_long_string_for_security
 *   expiration: 86400000  # 24小时 = 24*60*60*1000
 */
@Component
public class JwtUtil {

    /** 签名密钥，从配置文件注入，用于加密和验证token */
    @Value("${jwt.secret}")
    private String secret;

    /** token过期时间（毫秒），从配置文件注入，默认24小时 */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 生成JWT Token
     *
     * @param userId   用户ID，存入token的Payload中
     * @param username 用户名
     * @param role     角色（USER/MERCHANT/ADMIN），用于权限判断
     * @return JWT字符串，格式：xxx.yyy.zzz
     *
     * 生成的token示例（解码后的Payload）：
     * {
     *   "userId": 1,
     *   "username": "admin",
     *   "role": "ADMIN",
     *   "iat": 1715400000,     // 签发时间
     *   "exp": 1715486400      // 过期时间 = 签发时间 + 24小时
     * }
     */
    public String createToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 解析JWT Token，提取其中的用户信息
     *
     * @param token JWT字符串
     * @return Claims对象，包含 userId、username、role 等信息
     * @throws Exception token无效或过期时抛出异常
     *
     * 前端收到401后应清除本地token并跳转登录页
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 判断token是否过期
     *
     * @param token JWT字符串
     * @return true=已过期, false=有效
     *
     * 前端可以在请求前先检查token是否过期，避免无效请求
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}