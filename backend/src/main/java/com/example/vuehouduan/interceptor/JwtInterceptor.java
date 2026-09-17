package com.example.vuehouduan.interceptor;

import cn.hutool.core.util.StrUtil;
import com.example.vuehouduan.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 * JWT 认证拦截器 - 请求到达Controller前的"门卫"
 * ============================================================
 *
 * 【拦截器在请求链路中的位置】
 * 浏览器 → Tomcat → Filter(CorsFilter) → Interceptor(JwtInterceptor) → Controller → Service → Mapper
 *                                               ↑
 *                                          这里做身份验证
 *
 * 【工作流程】
 * 1. 检查请求方法是否为 OPTIONS（预检请求），是则直接放行
 * 2. 从请求头 Authorization 中获取 token（格式：Bearer xxx）
 * 3. 如果没有token，返回 401 JSON：{ code:401, message:"未登录或token已过期" }
 * 4. 解析token，提取 userId、username、role
 * 5. 存入 request.setAttribute()，供后续Controller使用
 * 6. 放行请求
 *
 * 【前端如何携带token】
 * // 在 axios 请求拦截器中自动添加
 * axios.interceptors.request.use(config => {
 *   const token = localStorage.getItem('token');
 *   if (token) {
 *     config.headers['Authorization'] = 'Bearer ' + token;
 *   }
 *   return config;
 * });
 *
 * 【Controller中如何获取用户信息】
 * Long userId = (Long) request.getAttribute("userId");
 * String username = (String) request.getAttribute("username");
 * String role = (String) request.getAttribute("role");
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 请求到达Controller之前执行
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param handler  处理器
     * @return true=放行, false=拦截（返回401 JSON）
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler) throws Exception {
        // OPTIONS 预检请求直接放行（CORS需要）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 1. 从请求头获取 token
        String token = request.getHeader("Authorization");
        if (StrUtil.isBlank(token)) {
            token = request.getParameter("token");
        }
        // 去掉 "Bearer " 前缀（前端axios自动加的前缀）
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 2. 没有token → 检查是否是公开的GET请求
        if (StrUtil.isBlank(token)) {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            if ("GET".equalsIgnoreCase(method) && uri.matches(".*/api/products(/(search|hot|new|recommended|categories|stats))?")) {
                return true;
            }
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            Map<String, Object> map = new HashMap<>();
            map.put("code", 401);
            map.put("message", "未登录或token已过期");
            response.getWriter().write(new ObjectMapper().writeValueAsString(map));
            return false;
        }

        // 3. 解析token，提取用户信息
        try {
            Map<String, Object> claims = jwtUtil.parseToken(token);
            if (claims == null) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_OK);
                Map<String, Object> map = new HashMap<>();
                map.put("code", 401);
                map.put("message", "token无效");
                response.getWriter().write(new ObjectMapper().writeValueAsString(map));
                return false;
            }

            // 4. 处理 userId 类型兼容（可能是Integer或Long）
            Object userIdObj = claims.get("userId");
            Long userId = null;
            if (userIdObj instanceof Long) {
                userId = (Long) userIdObj;
            } else if (userIdObj instanceof Integer) {
                userId = ((Integer) userIdObj).longValue();
            } else if (userIdObj != null) {
                userId = Long.parseLong(userIdObj.toString());
            }

            // 5. 将用户信息存入 request，供后续Controller使用
            request.setAttribute("userId", userId);
            request.setAttribute("username", claims.get("username"));
            request.setAttribute("role", claims.get("role"));
            return true;
        } catch (Exception e) {
            // token解析失败 → 返回401
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            Map<String, Object> map = new HashMap<>();
            map.put("code", 401);
            map.put("message", "token解析失败");
            response.getWriter().write(new ObjectMapper().writeValueAsString(map));
            return false;
        }
    }
}