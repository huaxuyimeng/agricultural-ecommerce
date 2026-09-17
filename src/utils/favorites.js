/**
 * 收藏管理工具
 * 文件路径: src/utils/favorites.js
 * 功能描述: 处理用户收藏的添加、删除、查询等操作
 * 关联文件:
 * - src/views/front/Favorites.vue: 收藏页面
 * - src/views/front/ProductDetail.vue: 商品详情页面
 * - src/data/products.js: 商品数据
 * - src/data/news.js: 新闻数据
 */

const STORAGE_KEY_PREFIX = 'xm-favorites'

// 获取当前用户
function getCurrentUser() {
    try {
        return JSON.parse(localStorage.getItem('xm-user') || '{}')
    } catch (e) {
        return {}
    }
}

// 获取用户作用域
function getUserScope() {
    const user = getCurrentUser()
    if (!user || !user.id) return 'guest'
    return `${user.role || 'USER'}_${user.id}`
}

// 获取存储键
function getStorageKeyByScope(scope) {
    const finalScope = scope || getUserScope()
    return `${STORAGE_KEY_PREFIX}:${finalScope}`
}

// 读取收藏数据
function readFavorites(scope) {
    try {
        return JSON.parse(localStorage.getItem(getStorageKeyByScope(scope)) || '[]')
    } catch (e) {
        return []
    }
}

// 保存收藏数据
function saveFavorites(favorites, scope) {
    try {
        localStorage.setItem(getStorageKeyByScope(scope), JSON.stringify(favorites))
    } catch (e) { }
    dispatchFavoritesChanged()
}

// 触发收藏变更事件
function dispatchFavoritesChanged() {
    try {
        window.dispatchEvent(new Event('xm-favorites-changed'))
    } catch (e) {
        const evt = document.createEvent('Event')
        evt.initEvent('xm-favorites-changed', true, true)
        window.dispatchEvent(evt)
    }
}

// 获取用户收藏列表
function getFavorites(scope) {
    return readFavorites(scope)
}

// 获取用户收藏数量
function getFavoritesCount(scope) {
    return readFavorites(scope).length
}

// 检查是否已收藏
function isFavorited(targetId, type = 'product', scope) {
    const favorites = readFavorites(scope)
    return favorites.some(item => item.target_id === targetId && item.type === type)
}

// 添加收藏
function addFavorite(targetId, type = 'product', targetName, scope) {
    const user = getCurrentUser()
    if (!user || !user.id) {
        return { success: false, message: '请先登录' }
    }

    const favorites = readFavorites(scope)

    // 检查是否已收藏
    if (isFavorited(targetId, type, scope)) {
        return { success: false, message: '已经收藏过了' }
    }

    // 创建收藏项
    const newFavorite = {
        id: Date.now(),
        user_id: user.id,
        type,
        target_id: targetId,
        target_name: targetName,
        create_time: new Date().toISOString()
    }

    // 添加到收藏列表
    favorites.push(newFavorite)
    saveFavorites(favorites, scope)

    return { success: true, favorite: newFavorite }
}

// 移除收藏
function removeFavorite(targetId, type = 'product', scope) {
    const favorites = readFavorites(scope)
    const filteredFavorites = favorites.filter(item => !(item.target_id === targetId && item.type === type))

    if (filteredFavorites.length === favorites.length) {
        return { success: false, message: '未找到收藏记录' }
    }

    saveFavorites(filteredFavorites, scope)
    return { success: true }
}

// 按类型获取收藏
function getFavoritesByType(type = 'product', scope) {
    const favorites = readFavorites(scope)
    return favorites.filter(item => item.type === type)
}

// 清空收藏
function clearFavorites(scope) {
    saveFavorites([], scope)
    return { success: true }
}

function toggleFavorite(targetId, type = 'product', targetName, scope) {
    if (isFavorited(targetId, type, scope)) {
        return removeFavorite(targetId, type, scope)
    } else {
        return addFavorite(targetId, type, targetName, scope)
    }
}

// 初始化默认收藏数据（仅当用户首次使用时）
function initDefaultFavorites() {
    const user = getCurrentUser()
    if (!user || !user.id) return

    const favorites = readFavorites()
    if (favorites.length > 0) return // 已有数据，不需要初始化

    // 根据用户ID初始化默认收藏
    const defaultFavorites = []

    if (user.id === 2) {
        defaultFavorites.push(
            {
                id: Date.now() + 1,
                user_id: 2,
                type: 'product',
                target_id: 1,
                target_name: '有机黄瓜',
                create_time: new Date().toISOString()
            },
            {
                id: Date.now() + 2,
                user_id: 2,
                type: 'product',
                target_id: 2,
                target_name: '新鲜西红柿',
                create_time: new Date().toISOString()
            },
            {
                id: Date.now() + 3,
                user_id: 2,
                type: 'product',
                target_id: 5,
                target_name: '农家土鸡蛋',
                create_time: new Date().toISOString()
            },
            {
                id: Date.now() + 4,
                user_id: 2,
                type: 'news',
                target_id: 1,
                target_name: '乡村振兴政策解读',
                create_time: new Date().toISOString()
            }
        )
    } else if (user.id === 3) {
        defaultFavorites.push(
            {
                id: Date.now() + 1,
                user_id: 3,
                type: 'product',
                target_id: 7,
                target_name: '有机大米',
                create_time: new Date().toISOString()
            },
            {
                id: Date.now() + 2,
                user_id: 3,
                type: 'product',
                target_id: 8,
                target_name: '有机小米',
                create_time: new Date().toISOString()
            },
            {
                id: Date.now() + 3,
                user_id: 3,
                type: 'news',
                target_id: 4,
                target_name: '农产品电商发展趋势',
                create_time: new Date().toISOString()
            }
        )
    }

    if (defaultFavorites.length > 0) {
        saveFavorites(defaultFavorites)
    }
}

export {
    getFavorites,
    getFavoritesCount,
    isFavorited,
    addFavorite,
    removeFavorite,
    getFavoritesByType,
    clearFavorites,
    initDefaultFavorites,
    toggleFavorite
}