/**
 * 购物车管理工具
 * 文件路径: src/utils/cart.js
 * 功能描述: 处理购物车的添加、修改、删除等操作
 * 关联文件:
 * - src/views/front/Cart.vue: 购物车页面
 * - src/views/front/ProductDetail.vue: 商品详情页面
 * - src/views/front/Home.vue: 首页组件
 * - src/data/products.js: 商品数据
 * - src/data/orders.js: 订单数据
 */
import products from '@/data/products'
import { createOrder } from '@/data/orders'

const STORAGE_KEY_PREFIX = 'xm-cart'

function getCurrentUser() {
    try {
        return JSON.parse(localStorage.getItem('xm-user') || '{}')
    } catch (e) {
        return {}
    }
}

function getUserScope() {
    const user = getCurrentUser()
    if (!user || !user.id) return 'guest'
    return `${user.role || 'USER'}_${user.id}`
}

function getStorageKeyByScope(scope) {
    const finalScope = scope || getUserScope()
    return `${STORAGE_KEY_PREFIX}:${finalScope}`
}

function readCart(scope) {
    try {
        return JSON.parse(localStorage.getItem(getStorageKeyByScope(scope)) || '[]')
    } catch (e) {
        return []
    }
}

function saveCart(cart, scope) {
    try {
        localStorage.setItem(getStorageKeyByScope(scope), JSON.stringify(cart))
    } catch (e) { }
    dispatchCartChanged()
}

function dispatchCartChanged() {
    try {
        window.dispatchEvent(new Event('xm-cart-changed'))
    } catch (e) {
        const evt = document.createEvent('Event')
        evt.initEvent('xm-cart-changed', true, true)
        window.dispatchEvent(evt)
    }
}

// 根据商品ID获取商品信息
function getProductById(productId) {
    return products.find(p => Number(p.id) === Number(productId))
}

// 初始化默认购物车数据
function initDefaultCart() {
    const user = getCurrentUser()
    if (!user || !user.id) return

    const cart = readCart()
    if (cart.length > 0) return

    // 为不同用户初始化默认购物车数据
    const defaultCart = []

    if (user.id === 2) {
        defaultCart.push(
            {
                "id": 1,
                "user_id": 2,
                "product_id": 1,
                "quantity": 2,
                "price": 3.50,
                "total_price": 7.00,
                "sku_id": null,
                "sku_attributes": "",
                "is_selected": true,
                "create_time": "2023-12-18 10:00:00",
                "update_time": "2023-12-18 10:00:00"
            },
            {
                "id": 2,
                "user_id": 2,
                "product_id": 2,
                "quantity": 3,
                "price": 4.50,
                "total_price": 13.50,
                "sku_id": null,
                "sku_attributes": "",
                "is_selected": true,
                "create_time": "2023-12-18 10:05:00",
                "update_time": "2023-12-18 10:05:00"
            },
            {
                "id": 3,
                "user_id": 2,
                "product_id": 3,
                "quantity": 1,
                "price": 8.00,
                "total_price": 8.00,
                "sku_id": null,
                "sku_attributes": "",
                "is_selected": false,
                "create_time": "2023-12-18 10:10:00",
                "update_time": "2023-12-18 10:10:00"
            }
        )
    } else if (user.id === 3) {
        defaultCart.push(
            {
                "id": 4,
                "user_id": 3,
                "product_id": 5,
                "quantity": 2,
                "price": 25.00,
                "total_price": 50.00,
                "sku_id": null,
                "sku_attributes": "",
                "is_selected": true,
                "create_time": "2023-12-18 11:00:00",
                "update_time": "2023-12-18 11:00:00"
            },
            {
                "id": 5,
                "user_id": 3,
                "product_id": 7,
                "quantity": 1,
                "price": 50.00,
                "total_price": 50.00,
                "sku_id": null,
                "sku_attributes": "",
                "is_selected": true,
                "create_time": "2023-12-18 11:05:00",
                "update_time": "2023-12-18 11:05:00"
            }
        )
    }

    if (defaultCart.length > 0) {
        saveCart(defaultCart)
    }
}

// 获取购物车列表
function getCart(scope) {
    const cart = readCart(scope)
    return cart.map(item => {
        const product = getProductById(item.product_id)
        return {
            ...item,
            product: product
        }
    })
}

// 获取购物车数量
function getCartCount(scope) {
    return readCart(scope).reduce((sum, item) => sum + (parseInt(item.quantity, 10) || 0), 0)
}

// 获取购物车总价
function getCartTotal(scope) {
    return readCart(scope)
        .filter(item => item.is_selected)
        .reduce((sum, item) => sum + (parseFloat(item.total_price) || 0), 0)
}

// 获取购物车中选中的商品
function getSelectedItems(scope) {
    return readCart(scope).filter(item => item.is_selected)
}

// 检查商品是否在购物车中
function isInCart(productId, scope) {
    return readCart(scope).some(item => Number(item.product_id) === Number(productId))
}

// 添加商品到购物车
function addToCart(product, quantity = 1, scope) {
    if (!product || quantity < 1) return { success: false, message: '参数错误' }

    const user = getCurrentUser()
    if (!user || !user.id) return { success: false, message: '请先登录' }

    const cart = readCart(scope)
    const existingIndex = cart.findIndex(item => Number(item.product_id) === Number(product.id))
    const availableStock = product.stock == null ? Infinity : Number(product.stock)

    if (existingIndex > -1) {
        const existing = cart[existingIndex]
        const currentQuantity = Number(existing.quantity || 0)
        if (currentQuantity + quantity > availableStock) {
            return { success: false, message: '已达最大购买数量' }
        }

        const newQuantity = currentQuantity + quantity
        cart[existingIndex] = {
            ...existing,
            quantity: newQuantity,
            total_price: (product.price * newQuantity).toFixed(2),
            update_time: new Date().toISOString()
        }
    } else {
        const newItem = {
            id: Date.now(),
            user_id: user.id,
            product_id: product.id,
            quantity: quantity,
            price: product.price,
            total_price: (product.price * quantity).toFixed(2),
            sku_id: null,
            sku_attributes: '',
            is_selected: true,
            create_time: new Date().toISOString(),
            update_time: new Date().toISOString()
        }
        cart.push(newItem)
    }

    saveCart(cart, scope)
    return { success: true, cart: getCart(scope) }
}

// 更新购物车商品数量
function updateCartItemQuantity(itemId, quantity, scope) {
    const cart = readCart(scope)
    const index = cart.findIndex(item => Number(item.id) === Number(itemId))
    if (index === -1) return { success: false, message: '商品不存在' }
    if (quantity < 1) return { success: false, message: '数量必须大于0' }

    const item = cart[index]
    const product = getProductById(item.product_id)
    if (!product) return { success: false, message: '商品信息不存在' }

    const availableStock = product.stock == null ? Infinity : Number(product.stock)
    if (quantity > availableStock) {
        return { success: false, message: '已达最大购买数量' }
    }

    cart[index] = {
        ...item,
        quantity: quantity,
        total_price: (product.price * quantity).toFixed(2),
        update_time: new Date().toISOString()
    }

    saveCart(cart, scope)
    return { success: true, cart: getCart(scope) }
}

// 更新购物车商品选中状态
function updateCartItemSelected(itemId, isSelected, scope) {
    const cart = readCart(scope)
    const index = cart.findIndex(item => Number(item.id) === Number(itemId))
    if (index === -1) return { success: false, message: '商品不存在' }

    cart[index] = {
        ...cart[index],
        is_selected: isSelected,
        update_time: new Date().toISOString()
    }

    saveCart(cart, scope)
    return { success: true, cart: getCart(scope) }
}

// 全选/取消全选
function toggleAllSelected(isSelected, scope) {
    const cart = readCart(scope)
    const updatedCart = cart.map(item => ({
        ...item,
        is_selected: isSelected,
        update_time: new Date().toISOString()
    }))

    saveCart(updatedCart, scope)
    return { success: true, cart: getCart(scope) }
}

// 删除购物车商品
function removeCartItem(itemId, scope) {
    const cart = readCart(scope).filter(item => Number(item.id) !== Number(itemId))
    saveCart(cart, scope)
    return { success: true, cart: getCart(scope) }
}

// 清空购物车
function clearCart(scope) {
    saveCart([], scope)
    return { success: true, cart: [] }
}

// 从购物车创建订单
function checkoutCart(orderInfo = {}, scope) {
    const user = getCurrentUser()
    if (!user || !user.id) {
        return { success: false, message: '请先登录' }
    }

    const selectedItems = getSelectedItems(scope)
    if (selectedItems.length === 0) {
        return { success: false, message: '请选择要结算的商品' }
    }

    // 构建订单商品列表
    const orderProducts = selectedItems.map(item => ({
        productId: item.product_id,
        count: item.quantity
    }))

    // 创建正式订单
    const newOrder = createOrder(user.id, orderProducts, {
        userName: user.username || user.name || '用户',
        merchantId: orderInfo.merchantId || 4,
        merchantName: orderInfo.merchantName || '绿源生态农场',
        status: 'pending',
        shippingAddress: orderInfo.shippingAddress || '',
        shippingPhone: orderInfo.shippingPhone || '',
        shippingName: orderInfo.shippingName || '',
        remark: orderInfo.remark || ''
    })

    // 移除已结算的商品
    const cart = readCart(scope).filter(item => !item.is_selected)
    saveCart(cart, scope)

    return { success: true, order: newOrder }
}

// 获取带有商品详情的订单列表
function getOrdersWithDetails(scope) {
    const cart = readCart(scope)
    return cart.map(item => {
        const product = getProductById(item.product_id)
        return {
            ...item,
            product: product,
            status: 'cart',
            orderId: item.orderId || `cart-${Date.now()}-${Math.floor(Math.random() * 1000)}`
        }
    })
}

// 为订单添加商品详情
function enrichOrderWithProductDetails(order) {
    if (!order || !order.products || !Array.isArray(order.products)) {
        return order
    }

    const productsWithDetails = order.products.map(item => {
        const product = getProductById(item.productId)
        return {
            ...item,
            product: product || {}
        }
    })

    return {
        ...order,
        products: productsWithDetails
    }
}

// 兼容旧的readOrders函数
function readOrders(scope) {
    return readCart(scope)
}

// 兼容旧的saveOrders函数
function saveOrders(orders, scope) {
    return saveCart(orders, scope)
}

// 兼容旧的getCartOrders函数
function getCartOrders(scope) {
    return getCart(scope)
}

export {
    readCart,
    saveCart,
    getCart,
    getCartCount,
    getCartTotal,
    getSelectedItems,
    isInCart,
    addToCart,
    updateCartItemQuantity,
    updateCartItemSelected,
    toggleAllSelected,
    removeCartItem,
    clearCart,
    checkoutCart,
    initDefaultCart,
    getProductById,
    getOrdersWithDetails,
    enrichOrderWithProductDetails,
    readOrders,
    saveOrders,
    getCartOrders
}