import {
    getUserFavorites as apiGetUserFavorites,
    addFavorite as apiAddFavorite,
    removeFavorite as apiRemoveFavorite
} from '@/api'

function getCurrentUser() {
    try {
        return JSON.parse(localStorage.getItem('xm-user') || '{}')
    } catch {
        return {}
    }
}

export async function getFavorites() {
    const user = getCurrentUser()
    if (!user || !user.id) return []
    try {
        const res = await apiGetUserFavorites()
        return res.data || []
    } catch {
        return []
    }
}

export async function getProductFavorites() {
    return getFavorites()
}

export async function checkFavorite(type, targetId) {
    const user = getCurrentUser()
    if (!user || !user.id) return false
    try {
        const favorites = await getFavorites()
        return favorites.some(f => {
            const favTargetId = f.targetId || f.target_id
            return Number(favTargetId) === Number(targetId)
        })
    } catch {
        return false
    }
}

export async function addFavorite(type, targetId, targetName) {
    const user = getCurrentUser()
    if (!user || !user.id) throw new Error('请先登录')
    const res = await apiAddFavorite({ productId: targetId })
    return res.data
}

export async function removeFavorite(type, targetId) {
    const user = getCurrentUser()
    if (!user || !user.id) throw new Error('请先登录')
    await apiRemoveFavorite(targetId)
    return true
}

export default {
    getFavorites,
    getProductFavorites,
    checkFavorite,
    addFavorite,
    removeFavorite
}
