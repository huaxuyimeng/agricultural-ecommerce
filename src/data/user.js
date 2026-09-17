import request from '@/utils/request'
import {
    getUserPage as apiGetUserPage,
    getUserById as apiGetUserById,
    updateUser as apiUpdateUser,
    deleteUser as apiDeleteUser,
    createUser as apiCreateUser,
    changePassword as apiChangePassword,
    getMerchants as apiGetMerchants,
    getLoginHistory as apiGetLoginHistory
} from '@/api'

export function getCurrentUser() {
    try {
        const userStr = sessionStorage.getItem('xm-user') || localStorage.getItem('xm-user')
        return userStr ? JSON.parse(userStr) : null
    } catch {
        return null
    }
}

export function setCurrentUser(user) {
    localStorage.setItem('xm-user', JSON.stringify(user))
}

export function getCurrentUserToken() {
    return localStorage.getItem('xm-token')
}

export function setCurrentUserToken(token) {
    localStorage.setItem('xm-token', token)
}

export function clearCurrentUser() {
    localStorage.removeItem('xm-user')
    localStorage.removeItem('xm-token')
}

export async function getAllUsers(params = {}) {
    try {
        const res = await getUserPage(params)
        return {
            list: res.data?.records || res.data?.list || [],
            total: res.data?.total || 0,
            page: params.pageNum || 1,
            pageSize: params.pageSize || 10
        }
    } catch {
        return { list: [], total: 0, page: 1, pageSize: 10 }
    }
}

export function getAllUsersSync() {
    const user = getCurrentUser()
    return user ? [user] : []
}

export async function getUserStats() {
    try {
        const allRes = await getAllUsers({ pageSize: 1000 })
        const users = allRes.list || []
        return {
            total: allRes.total,
            admins: users.filter(u => u.role === 'ADMIN').length,
            merchants: users.filter(u => u.role === 'MERCHANT').length,
            users: users.filter(u => u.role === 'USER').length,
            active: users.filter(u => u.status === 1).length,
            disabled: users.filter(u => u.status === 0).length
        }
    } catch {
        const user = getCurrentUser()
        return {
            total: user ? 1 : 0,
            admins: user?.role === 'ADMIN' ? 1 : 0,
            merchants: user?.role === 'MERCHANT' ? 1 : 0,
            users: user?.role === 'USER' ? 1 : 0,
            active: user?.status === 1 ? 1 : 0,
            disabled: user?.status === 0 ? 1 : 0
        }
    }
}

export async function getUserById(id) {
    try {
        const res = await apiGetUserById(id)
        return res.data
    } catch {
        return null
    }
}

export async function addUser(userData) {
    const res = await apiCreateUser(userData)
    return res.data
}

export async function updateUser(id, userData) {
    const res = await apiUpdateUser(id, userData)
    const currentUser = getCurrentUser()
    if (currentUser && currentUser.id === id) {
        setCurrentUser({ ...currentUser, ...res.data })
    }
    return res.data
}

export async function deleteUser(id) {
    await apiDeleteUser(id)
    return true
}

export async function deleteUsers(ids) {
    await Promise.all(ids.map(id => apiDeleteUser(id)))
    return true
}

export async function changePassword(data) {
    const res = await apiChangePassword(data)
    return res.data
}

export async function resetPassword(id, newPassword = '123456') {
    return updateUser(id, { password: newPassword })
}

export async function getMerchants() {
    try {
        const res = await apiGetMerchants()
        return res.data || []
    } catch {
        return []
    }
}

export async function searchUsers(keyword) {
    const result = await getAllUsers({ username: keyword })
    return result.list
}

export async function getUsersByRole(role) {
    const result = await getAllUsers({ role })
    return result.list
}

export async function getUsersByStatus(status) {
    const result = await getAllUsers({ status })
    return result.list
}

export async function isUsernameExist(username, excludeId = null) {
    try {
        const result = await getAllUsers({ username })
        return result.list.some(u => u.username === username && u.id !== excludeId)
    } catch {
        return false
    }
}

export function isEmailExist(email, excludeId = null) {
    return false
}

export function isPhoneExist(phone, excludeId = null) {
    return false
}

export function getRoleText(role) {
    const roleMap = {
        'ADMIN': '管理员',
        'MERCHANT': '农户/商家',
        'USER': '普通用户'
    }
    return roleMap[role] || role
}

export function getStatusText(status) {
    return status === 1 ? '正常' : '禁用'
}

export function getStatusTagType(status) {
    return status === 1 ? 'success' : 'danger'
}

export function getRoleTagType(role) {
    const typeMap = {
        'ADMIN': 'danger',
        'MERCHANT': 'warning',
        'USER': 'success'
    }
    return typeMap[role] || 'info'
}

export async function approveUser(id) {
    return updateUser(id, { approvalStatus: 'APPROVED' })
}

export async function rejectUser(id) {
    return updateUser(id, { approvalStatus: 'REJECTED' })
}

export function getApprovalText(status) {
    const statusMap = {
        'PENDING': '待审核',
        'APPROVED': '已通过',
        'REJECTED': '已驳回'
    }
    return statusMap[status] || '未知状态'
}

export function getApprovalTagType(status) {
    const typeMap = {
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger'
    }
    return typeMap[status] || 'info'
}

export function resetToInitial() {
    clearCurrentUser()
}

export async function getLoginHistory(params = {}) {
    try {
        const res = await apiGetLoginHistory(params)
        return {
            list: res.data?.records || res.data?.list || [],
            total: res.data?.total || 0,
            page: params.pageNum || 1,
            pageSize: params.pageSize || 10
        }
    } catch {
        return { list: [], total: 0, page: 1, pageSize: 10 }
    }
}

export async function addLoginHistory(userId, data = {}) {
    try {
        await request({
            url: '/login-history',
            method: 'post',
            data: { userId, ...data }
        })
        return true
    } catch {
        return false
    }
}

export default {
    getCurrentUser,
    setCurrentUser,
    getCurrentUserToken,
    setCurrentUserToken,
    clearCurrentUser,
    getAllUsers,
    getAllUsersSync,
    getUserStats,
    getUserById,
    addUser,
    updateUser,
    deleteUser,
    deleteUsers,
    changePassword,
    resetPassword,
    getMerchants,
    searchUsers,
    getUsersByRole,
    getUsersByStatus,
    isUsernameExist,
    isEmailExist,
    isPhoneExist,
    getRoleText,
    getStatusText,
    getStatusTagType,
    getRoleTagType,
    approveUser,
    rejectUser,
    getApprovalText,
    getApprovalTagType,
    resetToInitial,
    getLoginHistory,
    addLoginHistory
}
