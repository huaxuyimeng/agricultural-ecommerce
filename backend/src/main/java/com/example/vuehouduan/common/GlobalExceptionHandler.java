package com.example.vuehouduan.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * ============================================================
 * 全局异常处理器 - 统一捕获所有Controller抛出的异常
 * ============================================================
 *
 * 【为什么需要全局异常处理？】
 * 如果没有这个类，Controller中抛出的异常会直接返回500错误页面给前端，
 * 前端收到的不是JSON格式，无法解析，用户体验很差。
 * 有了全局异常处理，所有异常都会被拦截，转成统一的 Result JSON 格式返回。
 *
 * 【异常处理流程】
 * Controller → Service → 某处抛出异常
 *     ↓
 * 异常向上冒泡，被 @RestControllerAdvice 拦截
 *     ↓
 * 匹配 @ExceptionHandler 注解的方法
 *     ↓
 * 返回 Result JSON 给前端（前端正常解析，显示错误消息）
 *
 * 【@RestControllerAdvice 原理】
 * 这是 Spring AOP（面向切面编程）的应用：
 * 它会在所有 @RestController 的方法外面包一层 try-catch，
 * 任何未捕获的异常都会被这里的方法处理。
 *
 * 【前端收到的JSON示例】
 * 当后端抛出 new RuntimeException("用户名不存在") 时：
 * { "code": 400, "message": "用户名不存在", "data": null }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理运行时异常（业务逻辑中主动抛出的异常）
     * 返回 code=400，前端显示具体错误信息
     *
     * 示例：throw new RuntimeException("用户名不存在")
     * → 前端收到 { code:400, message:"用户名不存在" }
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        return Result.error(400, e.getMessage());
    }

    /**
     * 处理所有未预期的异常（兜底）
     * 返回 code=500，前端显示"服务器内部错误"
     * e.printStackTrace() 会在控制台打印完整堆栈，方便排查
     *
     * 示例：空指针、数组越界等未捕获的异常
     * → 前端收到 { code:500, message:"服务器内部错误: xxx" }
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        e.printStackTrace();
        return Result.error(500, "服务器内部错误: " + e.getMessage());
    }
}