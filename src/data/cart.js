import {
    getUserCart as apiGetUserCart,
    addToCart as apiAddToCart,
    updateCartItem as apiUpdateCartItem,
    removeFromCart as apiRemoveFromCart,
    clearUserCart as apiClearUserCart
} from '@/api'

export async function getCart() {
    try {
        const res = await apiGetUserCart()
        return res.data || []
    } catch {
        return []
    }
}

export async function getCartStats() {
    try {
        const items = await getCart()
        const count = items.reduce((sum, item) => sum + (item.quantity || item.count || 0), 0)
        const total = items.reduce((sum, item) => sum + ((item.price || 0) * (item.quantity || item.count || 0)), 0)
        return { count, total, items }
    } catch {
        return { count: 0, total: 0, items: [] }
    }
}

export async function addToCart(data) {
    const res = await apiAddToCart({
        productId: data.productId,
        count: data.quantity || data.count || 1
    })
    return res.data
}

export async function updateCartItem(id, quantity) {
    const res = await apiUpdateCartItem(id, quantity)
    return res.data
}

export async function removeFromCart(id) {
    await apiRemoveFromCart(id)
    return true
}

export async function clearCart() {
    await apiClearUserCart()
    return true
}

export function calculateCartTotal(items) {
    if (!items || !Array.isArray(items)) return 0
    return items.reduce((sum, item) => {
        return sum + ((item.price || 0) * (item.quantity || item.count || 0))
    }, 0)
}

export function calculateCartCount(items) {
    if (!items || !Array.isArray(items)) return 0
    return items.reduce((sum, item) => sum + (item.quantity || item.count || 0), 0)
}

export default {
    getCart,
    getCartStats,
    addToCart,
    updateCartItem,
    removeFromCart,
    clearCart,
    calculateCartTotal,
    calculateCartCount
}
