import {
    getProductPage as apiGetProductPage,
    getProductById as apiGetProductById,
    createProduct as apiCreateProduct,
    updateProduct as apiUpdateProduct,
    deleteProduct as apiDeleteProduct,
    approveProduct as apiApproveProduct,
    getHotProducts as apiGetHotProducts,
    getNewProducts as apiGetNewProducts,
    getRecommendProducts as apiGetRecommendProducts,
    searchProducts as apiSearchProducts
} from '@/api'

const CATEGORY_MAP = {
    'agricultural': '农产品',
    'livestock': '畜禽产品',
    'processed': '加工产品'
}

export async function getProductPageData(params = {}) {
    try {
        const res = await apiGetProductPage(params)
        return {
            list: res.data.records || res.data.list || [],
            total: res.data.total || 0,
            page: params.page || params.pageNum || 1,
            pageSize: params.pageSize || 10
        }
    } catch {
        return { list: [], total: 0, page: 1, pageSize: 10 }
    }
}

export async function getProductById(id) {
    try {
        const res = await apiGetProductById(id)
        return res.data
    } catch {
        return null
    }
}

export async function createProduct(data) {
    return await apiCreateProduct(data)
}

export async function updateProduct(id, data) {
    return await apiUpdateProduct(id, data)
}

export async function deleteProduct(id) {
    return await apiDeleteProduct(id)
}

export async function approveProduct(id, status) {
    return await apiApproveProduct(id, status)
}

export async function getHotProducts(limit = 10) {
    try {
        const res = await apiGetHotProducts(limit)
        return res.data || []
    } catch {
        return []
    }
}

export async function getNewProducts(limit = 10) {
    try {
        const res = await apiGetNewProducts(limit)
        return res.data || []
    } catch {
        return []
    }
}

export async function getRecommendProducts(limit = 10) {
    try {
        const res = await apiGetRecommendProducts(limit)
        return res.data || []
    } catch {
        return []
    }
}

export async function searchProducts(keyword) {
    try {
        const res = await apiSearchProducts(keyword)
        return res.data || []
    } catch {
        return []
    }
}

export function getCategoryText(category) {
    return CATEGORY_MAP[category] || category
}

export function getCategoryOptions() {
    return [
        { value: 'agricultural', label: '农产品' },
        { value: 'livestock', label: '畜禽产品' },
        { value: 'processed', label: '加工产品' }
    ]
}

export function getStatusText(status) {
    const statusMap = {
        'pending': '待审核',
        'approved': '已通过',
        'rejected': '已驳回'
    }
    return statusMap[status] || status
}

export function getStatusTagType(status) {
    const typeMap = {
        'pending': 'warning',
        'approved': 'success',
        'rejected': 'danger'
    }
    return typeMap[status] || 'info'
}

export { CATEGORY_MAP }

export default {
    getProductPageData,
    getProductById,
    createProduct,
    updateProduct,
    deleteProduct,
    approveProduct,
    getHotProducts,
    getNewProducts,
    getRecommendProducts,
    searchProducts,
    getCategoryText,
    getCategoryOptions,
    getStatusText,
    getStatusTagType,
    CATEGORY_MAP
}
