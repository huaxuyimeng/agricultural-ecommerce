package com.example.vuehouduan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================
 * 农产品电商平台 - Spring Boot 后端启动类
 * ============================================================
 *
 * 【前后端交互流程概述】
 * 前端(Vue.js) --HTTP请求--> 后端(Spring Boot) --SQL--> 数据库(MySQL)
 * ^ |
 * |________________ JSON响应 ______________________________|
 *
 * 【请求处理链路】
 * 浏览器发起请求 → Nginx/CORS过滤 → JwtInterceptor鉴权 → Controller接收
 * → Service处理业务 → Mapper执行SQL → 数据库 → 逐层返回JSON给前端
 *
 * 【关键注解说明】
 * 
 * @SpringBootApplication = @Configuration + @EnableAutoConfiguration
 *                        + @ComponentScan
 *                        标记这是Spring Boot主类，自动扫描同包及子包下的所有组件
 *                        @MapperScan("com.example.vuehouduan.mapper")
 *                        告诉MyBatis-Plus去哪里找Mapper接口，替代在每个Mapper上加@Mapper
 *
 *                        【端口配置】
 *                        在 application.yml 中配置 server.port=9090
 *                        前端通过 http://localhost:9090 访问后端API
 */
@SpringBootApplication
@MapperScan("com.example.vuehouduan.mapper")
public class VuehouduanApplication {

    /**
     * 程序入口
     * SpringApplication.run() 会：
     * 1. 启动内嵌Tomcat服务器（默认端口9090）
     * 2. 初始化Spring容器，扫描并创建所有Bean
     * 3. 注册所有Controller的URL映射
     * 4. 加载MyBatis-Plus配置，连接MySQL数据库
     */
    public static void main(String[] args) {
        SpringApplication.run(VuehouduanApplication.class, args);
    }
}