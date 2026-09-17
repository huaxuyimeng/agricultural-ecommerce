import {
    getNewsPage as apiGetNewsPage,
    getNewsById as apiGetNewsById,
    createNews as apiCreateNews,
    updateNews as apiUpdateNews,
    deleteNews as apiDeleteNews,
    likeNews as apiLikeNews,
    shareNews as apiShareNews
} from '@/api'

export async function getNewsPageData(params = {}) {
    try {
        const res = await apiGetNewsPage(params)
        return {
            list: res.data.records || res.data.list || [],
            total: res.data.total || 0,
            page: params.pageNum || 1,
            pageSize: params.pageSize || 10
        }
    } catch {
        return { list: [], total: 0, page: 1, pageSize: 10 }
    }
}

export async function getNewsById(id) {
    try {
        const res = await apiGetNewsById(id)
        return res.data
    } catch {
        return null
    }
}

export async function createNews(data) {
    const res = await apiCreateNews(data)
    return res.data
}

export async function updateNews(id, data) {
    const res = await apiUpdateNews(id, data)
    return res.data
}

export async function deleteNews(id) {
    await apiDeleteNews(id)
    return true
}

export async function likeNews(id) {
    await apiLikeNews(id)
    return true
}

export async function shareNews(id) {
    await apiShareNews(id)
    return true
}

export const newsCategories = [
    { value: 'policy', label: '政策解读' },
    { value: 'technology', label: '技术培训' },
    { value: 'farming', label: '农事指南' },
    { value: 'ecommerce', label: '电商发展' },
    { value: 'finance', label: '农村金融' },
    { value: 'safety', label: '质量安全' },
    { value: 'tourism', label: '乡村旅游' },
    { value: 'marketing', label: '品牌建设' }
]

export function getCategoryText(category) {
    const found = newsCategories.find(c => c.value === category)
    return found ? found.label : category
}

export default {
    getNewsPageData,
    getNewsById,
    createNews,
    updateNews,
    deleteNews,
    likeNews,
    shareNews,
    newsCategories,
    getCategoryText
}
