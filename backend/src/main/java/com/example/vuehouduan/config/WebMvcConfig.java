package com.example.vuehouduan.config;

import com.example.vuehouduan.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * ============================================================
 * Web MVC 配置 - 拦截器注册 + 静态资源映射
 * ============================================================
 *
 * 【拦截器 vs 过滤器】
 * 过滤器(Filter)：Servlet层面，在请求进入Spring之前处理（如CorsFilter）
 * 拦截器(Interceptor)：Spring层面，在请求到达Controller之前处理（如JwtInterceptor）
 * 执行顺序：Filter → Interceptor → Controller
 *
 * 【JWT鉴权流程】
 * 前端每次请求都在 Header 中携带 token：
 * axios.get('/api/orders', { headers: { Authorization: 'Bearer xxx' } })
 * ↓
 * JwtInterceptor.preHandle() 拦截请求
 * ↓
 * 从 Header 中取出 token，解析出 userId、username、role
 * ↓
 * 存入 request.setAttribute()，Controller 中通过 request.getAttribute("userId") 获取
 * ↓
 * 放行 → Controller 处理业务
 *
 * 【白名单路径（不需要登录就能访问）】
 * /api/auth/login - 登录接口
 * /api/auth/register - 注册接口
 * /api/products - 商品列表（游客可浏览）
 * /api/news - 新闻列表（游客可浏览）
 * /api/upload - 文件上传
 * /imgs/** - 静态图片资源
 * /doc.html - Swagger API文档
 *
 * 【静态资源映射】
 * 上传的文件保存在项目目录下的 upload/ 文件夹
 * 通过 URL /upload/20240501/xxx.jpg 即可访问
 * 原理：将磁盘路径映射为HTTP URL路径
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

        @Autowired
        private @NonNull JwtInterceptor jwtInterceptor;

        /**
         * 注册JWT拦截器
         * addPathPatterns("/**") → 拦截所有请求
         * excludePathPatterns(...) → 排除不需要登录的路径（白名单）
         */
        @Override
        public void addInterceptors(@NonNull InterceptorRegistry registry) {
                registry.addInterceptor(jwtInterceptor)
                                .addPathPatterns("/**")
                                .excludePathPatterns(
                                                "/api/auth/login",
                                                "/api/auth/register",
                                                "/api/news",
                                                "/api/news/*",
                                                "/api/products/*",
                                                "/api/upload",
                                                "/api/upload/*",
                                                "/api/upload/avatar",
                                                "/api/coupon/list",
                                                "/imgs/**",
                                                "/upload/**",
                                                "/doc.html",
                                                "/swagger-ui.html",
                                                "/swagger-ui/**",
                                                "/swagger-resources/**",
                                                "/v3/api-docs/**",
                                                "/webjars/**",
                                                "/favicon.ico",
                                                "/error");
        }

        /**
         * 静态资源映射
         * 将磁盘上的文件目录映射为HTTP可访问的URL路径
         *
         * 例如：磁盘文件 D:/project/upload/20240501/abc.jpg
         * 可通过 http://localhost:9090/upload/20240501/abc.jpg 访问
         */
        @Override
        public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
                String userDir = System.getProperty("user.dir");

                String uploadPath = userDir + File.separator + "upload" + File.separator;
                registry.addResourceHandler("/upload/**")
                                .addResourceLocations("file:" + uploadPath);

                String publicImgPath = userDir + File.separator + "public" + File.separator + "imgs" + File.separator;
                registry.addResourceHandler("/imgs/**")
                                .addResourceLocations("file:" + publicImgPath);
        }
}