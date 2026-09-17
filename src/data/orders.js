import {
    getOrderPage as apiGetOrderPage,
    getOrderById as apiGetOrderById,
    createOrder as apiCreateOrder,
    cancelOrder as apiCancelOrder,
    payOrder as apiPayOrder,
    confirmReceive as apiConfirmReceive,
    getOrderStats as apiGetOrderStats
} from '@/api'

const ORDER_STATUS_MAP = {
    'PENDING': '待付款',
    'PAID': '已付款',
    'SHIPPED': '配送中',
    'DELIVERED': '已完成',
    'CANCELLED': '已取消'
}

export async function getOrderPageData(params = {}) {
    try {
        const res = await apiGetOrderPage(params)
        return {
            list: res.data?.records || res.data?.list || [],
            total: res.data?.total || 0,
            page: params.page || 1,
            pageSize: params.pageSize || 10
        }
    } catch {
        return { list: [], total: 0, page: 1, pageSize: 10 }
    }
}

export async function getOrderById(id) {
    try {
        const res = await apiGetOrderById(id)
        return res.data
    } catch {
        return null
    }
}

export async function getUserOrders() {
    try {
        const res = await apiGetOrderPage({ page: 1, pageSize: 100 })
        return res.data?.records || res.data?.list || []
    } catch {
        return []
    }
}

export async function createOrder(data) {
    const res = await apiCreateOrder(data)
    return res.data
}

export async function cancelOrder(id, reason) {
    await apiCancelOrder(id, reason)
    return true
}

export async function payOrder(id, paymentMethod) {
    const res = await apiPayOrder(id, paymentMethod)
    return res.data
}

export async function confirmReceive(id) {
    await apiConfirmReceive(id)
    return true
}

export async function getOrderStats() {
    try {
        const res = await apiGetOrderStats()
        return res.data || {}
    } catch {
        return {}
    }
}

export function getOrderStatusText(status) {
    return ORDER_STATUS_MAP[status] || status
}

export function getOrderStatusType(status) {
    const typeMap = {
        'PENDING': 'warning',
        'PAID': 'primary',
        'SHIPPED': 'info',
        'DELIVERED': 'success',
        'CANCELLED': 'info'
    }
    return typeMap[status] || 'info'
}

export function calculateOrderTotal(products) {
    if (!products || !Array.isArray(products)) return 0
    return products.reduce((sum, item) => {
        return sum + ((item.price || 0) * (item.count || item.quantity || 0))
    }, 0)
}

export const orderStatusList = [
    { value: 'PENDING', label: '待付款' },
    { value: 'PAID', label: '已付款' },
    { value: 'SHIPPED', label: '配送中' },
    { value: 'DELIVERED', label: '已完成' },
    { value: 'CANCELLED', label: '已取消' }
]

export const paymentMethods = [
    { value: 'alipay', label: '支付宝' },
    { value: 'wechat', label: '微信支付' },
    { value: 'bank', label: '银行卡' }
]

export { ORDER_STATUS_MAP }

export default {
    getOrderPageData,
    getOrderById,
    getUserOrders,
    createOrder,
    cancelOrder,
    payOrder,
    confirmReceive,
    getOrderStats,
    getOrderStatusText,
    getOrderStatusType,
    calculateOrderTotal,
    orderStatusList,
    paymentMethods,
    ORDER_STATUS_MAP
}
