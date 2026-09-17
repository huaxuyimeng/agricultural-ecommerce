/**
 * 共享工具函数
 * 文件路径: src/utils/common.js
 * 功能描述: 提供通用的工具函数，供各组件共享使用
 */

/**
 * 获取当前登录用户
 */
export function getCurrentUser() {
  try {
    return JSON.parse(localStorage.getItem('xm-user') || '{}')
  } catch {
    return {}
  }
}

/**
 * 检查是否已登录
 */
export function isLoggedIn() {
  const user = getCurrentUser()
  return !!(user && user.id)
}

/**
 * 获取完整图片地址
 */
export function getFullImageUrl(path, defaultImg = null, itemId = null) {
  // 处理空值情况
  if (!path || path === 'null' || path === 'undefined') {
    // 返回多样化的本地商品图片
    const index = itemId ? (Number(itemId) % 11) + 1 : Math.floor(Math.random() * 11) + 1
    return `/imgs/foods/${index}.png`
  }

  // 已经是完整的http链接或data URL
  if (path.startsWith('http') || path.startsWith('data:')) return path

  // 处理upload目录（如 20260508/xxx.jpg 或 upload/20260508/xxx.jpg）
  if (path.includes('upload') || /^\d{8}\//.test(path)) {
    const baseUrl = process.env.VUE_APP_BASEURL?.replace('/api', '') || 'http://localhost:9090'
    // 提取相对路径部分
    const relativePath = path.replace(/^.*upload\//, '').replace(/^upload\//, '')
    return `${baseUrl}/upload/${relativePath}`
  }

  // 处理imgs目录
  if (path.startsWith('imgs') || path.startsWith('/imgs')) {
    return path.startsWith('/') ? path : '/' + path
  }

  // 如果是相对路径，添加基础URL
  if (!path.startsWith('/') && !path.startsWith('http')) {
    const baseUrl = process.env.VUE_APP_BASEURL?.replace('/api', '') || 'http://localhost:9090'
    return `${baseUrl}/${path}`
  }

  return path
}

/**
 * 格式化金额
 */
export function formatPrice(price, decimals = 2) {
  return (parseFloat(price) || 0).toFixed(decimals)
}

/**
 * 格式化日期时间
 */
export function formatDateTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

/**
 * 格式化相对时间
 */
export function formatRelativeTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const mins = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  if (mins < 1) return '刚刚'
  if (mins < 60) return `${mins}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString()
}

/**
 * 订单状态映射
 */
export const ORDER_STATUS = {
  pending: { text: '待支付', type: 'warning', icon: 'el-icon-time' },
  processing: { text: '待发货', type: 'info', icon: 'el-icon-s-goods' },
  shipping: { text: '配送中', type: 'primary', icon: 'el-icon-s-promotion' },
  completed: { text: '已完成', type: 'success', icon: 'el-icon-check' },
  cancelled: { text: '已取消', type: 'info', icon: 'el-icon-close' }
}

export function getOrderStatusInfo(status) {
  return ORDER_STATUS[status] || { text: '未知', type: 'info', icon: 'el-icon-question' }
}

/**
 * 支付方式映射
 */
export const PAYMENT_METHODS = {
  alipay: { text: '支付宝', icon: 'el-icon-s-goods' },
  wechat: { text: '微信支付', icon: 'el-icon-s-goods' },
  card: { text: '银行卡', icon: 'el-icon-s-order' },
  cash: { text: '货到付款', icon: 'el-icon-s-goods' }
}

export function getPaymentMethodText(method) {
  return PAYMENT_METHODS[method]?.text || '未知方式'
}

/**
 * 商品分类映射
 */
export const PRODUCT_CATEGORIES = {
  agricultural: { text: '农产品', icon: 'el-icon-s-marketing', color: '#67C23A' },
  fruit: { text: '水果', icon: 'el-icon-s-goods', color: '#409EFF' },
  livestock: { text: '畜禽产品', icon: 'el-icon-s-custom', color: '#F56C6C' },
  grain: { text: '粮油', icon: 'el-icon-s-order', color: '#E6A23C' },
  processed: { text: '加工产品', icon: 'el-icon-s-goods', color: '#909399' },
  other: { text: '其他', icon: 'el-icon-goods', color: '#9C27B0' },
  // 兼容旧分类值
  vegetable: { text: '蔬菜', icon: 'el-icon-s-marketing', color: '#67C23A' },
  meat: { text: '肉类', icon: 'el-icon-s-custom', color: '#F56C6C' }
}

export function getCategoryInfo(category) {
  return PRODUCT_CATEGORIES[category] || { text: '其他', icon: 'el-icon-goods', color: '#909399' }
}

/**
 * 发送事件通知（跨组件通信）
 */
export function notifyCartChanged() {
  try {
    const evt = new Event('xm-cart-changed')
    window.dispatchEvent(evt)
  } catch (e) {
    const evt = document.createEvent('Event')
    evt.initEvent('xm-cart-changed', true, true)
    window.dispatchEvent(evt)
  }
}

export function notifyFavoritesChanged() {
  try {
    const evt = new Event('xm-favorites-changed')
    window.dispatchEvent(evt)
  } catch (e) {
    const evt = document.createEvent('Event')
    evt.initEvent('xm-favorites-changed', true, true)
    window.dispatchEvent(evt)
  }
}

export function notifyProductsChanged() {
  try {
    const evt = new Event('xm-products-updated')
    window.dispatchEvent(evt)
  } catch (e) {
    const evt = document.createEvent('Event')
    evt.initEvent('xm-products-updated', true, true)
    window.dispatchEvent(evt)
  }
}

/**
 * 防抖函数
 */
export function debounce(fn, delay = 300) {
  let timer = null
  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => fn.apply(this, args), delay)
  }
}

/**
 * 复制到剪贴板
 */
export async function copyToClipboard(text) {
  try {
    await navigator.clipboard.writeText(text)
    return true
  } catch {
    // 降级处理
    const textarea = document.createElement('textarea')
    textarea.value = text
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    const success = document.execCommand('copy')
    document.body.removeChild(textarea)
    return success
  }
}

/**
 * 验证手机号
 */
export function validatePhone(phone) {
  return /^1[3-9]\d{9}$/.test(phone)
}

/**
 * 生成唯一ID
 */
export function generateId() {
  return Date.now().toString(36) + Math.random().toString(36).substr(2, 9)
}

/**
 * 深拷贝
 */
export function deepClone(obj) {
  return JSON.parse(JSON.stringify(obj))
}
