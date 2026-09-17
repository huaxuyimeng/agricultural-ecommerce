/**
 * 请求工具模块
 * 文件路径: src/utils/request.js
 * 功能描述: 基于axios的请求封装，处理请求拦截和响应处理
 */
import axios from 'axios'
import router from "@/router";

// 创建axios实例
const request = axios.create({
    baseURL: process.env.VUE_APP_BASEURL,
    timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(config => {
    // 如果是 FormData，不设置 Content-Type，让浏览器自动设置
    if (!(config.data instanceof FormData)) {
        config.headers['Content-Type'] = 'application/json;charset=utf-8'
    }
    try {
        const token = localStorage.getItem('xm-token')
        if (token) {
            config.headers['Authorization'] = 'Bearer ' + token
        }
    } catch (e) {
        // ignore
    }
    return config
}, error => {
    console.error('请求配置错误:', error)
    return Promise.reject(error)
})

// 响应拦截器
request.interceptors.response.use(
    response => {
        const res = response.data
        
        // 处理字符串响应
        if (typeof res === 'string') {
            try {
                return JSON.parse(res)
            } catch {
                return res
            }
        }
        
        // 处理统一响应格式 { code, message, data }
        if (res.code !== 200) {
            if (res.code === 401) {
                localStorage.removeItem('xm-user')
                localStorage.removeItem('xm-token')
                router.push('/login').catch(() => {})
                return Promise.reject(new Error(res.message || '未登录或登录已过期'))
            }
            return Promise.reject(new Error(res.message || '请求失败'))
        }
        
        return res
    },
    error => {
        console.error('响应错误:', error)
        
        const err = error || {}
        err.message = err.message || '请求失败'
        
        if (err.code === 'ECONNABORTED' || (err.message && err.message.includes('timeout'))) {
            err.message = '请求超时，请稍后重试'
        }
        
        if (!err.response) {
            err.message = '无法连接服务器，请检查后端服务是否启动'
        } else if (err.response && err.response.status === 401) {
            localStorage.removeItem('xm-user')
            localStorage.removeItem('xm-token')
            router.push('/login').catch(() => {})
        }
        
        return Promise.reject(err)
    }
)

export default request
