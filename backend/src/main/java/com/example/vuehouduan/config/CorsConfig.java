package com.example.vuehouduan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * ============================================================
 * 跨域配置 - 允许前端(Vue)跨域访问后端API
 * ============================================================
 *
 * 【什么是跨域？】
 * 浏览器的"同源策略"安全机制：协议、域名、端口三者必须完全相同才能互相访问。
 * 前端运行在 http://localhost:8080（Vue开发服务器）
 * 后端运行在 http://localhost:9090（Spring Boot）
 * 端口不同 → 跨域 → 浏览器默认拦截
 *
 * 【解决方案：CORS（跨域资源共享）】
 * 后端在响应头中加入 Access-Control-Allow-Origin，告诉浏览器"我允许跨域"。
 * 浏览器收到这个头后，就会放行。
 *
 * 【前后端跨域交互流程】
 * 1. 前端 axios 发请求到 http://localhost:9090/api/xxx
 * 2. 浏览器先发 OPTIONS 预检请求（询问服务器是否允许跨域）
 * 3. CorsFilter 拦截，返回允许的响应头
 * 4. 浏览器确认允许后，再发真正的 GET/POST 请求
 * 5. 后端处理请求，返回数据
 *
 * 【配置说明】
 * addAllowedOriginPattern("*")  → 允许所有来源（开发环境用，生产环境应限制域名）
 * addAllowedHeader("*")         → 允许所有请求头（包括自定义的 Authorization）
 * addAllowedMethod("*")         → 允许所有HTTP方法（GET/POST/PUT/DELETE/OPTIONS）
 * setAllowCredentials(true)     → 允许携带Cookie（登录状态需要）
 * setMaxAge(3600L)              → 预检请求缓存1小时（减少OPTIONS请求次数）
 */
@Configuration
public class CorsConfig {

    /**
     * 允许跨域的来源（生产环境应改为前端实际域名）
     * 开发环境：localhost:8080
     * 生产环境：例如 https://your-domain.com
     */
    private static final String[] ALLOWED_ORIGINS = {
            "http://localhost:8080",
            "http://localhost:8081",
            "http://127.0.0.1:8080"
    };

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // 明确列出允许的来源（不要用 "*" + credentials 同时使用，浏览器会拦截）
        config.setAllowedOrigins(Arrays.asList(ALLOWED_ORIGINS));
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}