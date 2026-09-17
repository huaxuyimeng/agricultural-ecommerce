package com.example.vuehouduan.common;

import lombok.Data;

/**
 * ============================================================
 * 统一响应结果类 - 前后端数据交互的标准格式
 * ============================================================
 *
 * 【为什么需要统一响应格式？】
 * 前端需要知道每次请求是成功还是失败，以及携带什么数据。
 * 如果每个接口返回格式不同，前端就要写很多种解析逻辑，非常混乱。
 * 统一格式后，前端只需要判断 code 是否为 200 即可。
 *
 * 【前端接收到的JSON格式】
 * {
 *   "code": 200,        // 状态码：200=成功, 400=参数错误, 401=未登录, 500=服务器错误
 *   "message": "操作成功", // 提示信息，前端可以直接弹窗显示
 *   "data": { ... }     // 实际数据，可以是对象、数组、null
 * }
 *
 * 【泛型 <T> 的作用】
 * Result<User>  → data 是 User 对象
 * Result<List<Product>> → data 是 Product 列表
 * Result<?>     → data 类型不确定（登录返回token+user混合数据）
 *
 * 【前端 axios 拦截器中的处理】
 * response => {
 *   const res = response.data;  // 拿到这个 Result 对象
 *   if (res.code !== 200) {
 *     // 显示错误消息 res.message
 *     return Promise.reject(new Error(res.message));
 *   }
 *   return res;  // 返回给业务代码
 * }
 */
@Data
public class Result<T> {
    /** HTTP状态码：200成功, 400参数错误, 401未登录, 403无权限, 500服务器错误 */
    private Integer code;
    /** 提示信息，前端可直接展示给用户 */
    private String message;
    /** 响应数据，泛型支持任意类型 */
    private T data;

    // ==================== 成功响应的工厂方法 ====================

    /** 成功无数据：{ code:200, message:"操作成功", data:null } */
    public static <T> Result<T> success() {
        return success(null);
    }

    /** 成功带数据：{ code:200, message:"操作成功", data:传入的对象 } */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /** 成功带自定义消息和数据 */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    // ==================== 失败响应的工厂方法 ====================

    /** 失败默认500：{ code:500, message:"错误信息", data:null } */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    /** 失败自定义code：如 401未登录, 403无权限 */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /** 失败默认消息 */
    public static <T> Result<T> error() {
        return error(500, "操作失败");
    }
}