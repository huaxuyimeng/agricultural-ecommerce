import { getLoginHistory } from '@/api'

export async function getLoginHistoryData(params = {}) {
    try {
        const res = await getLoginHistory(params)
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

export function getLoginStatusText(status) {
    const statusMap = {
        'SUCCESS': '登录成功',
        'FAILED': '登录失败',
        'LOGOUT': '退出登录'
    }
    return statusMap[status] || status
}

export function getLoginStatusType(status) {
    const typeMap = {
        'SUCCESS': 'success',
        'FAILED': 'danger',
        'LOGOUT': 'info'
    }
    return typeMap[status] || 'info'
}

export default {
    getLoginHistoryData,
    getLoginStatusText,
    getLoginStatusType
}
