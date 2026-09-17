/**
 * API 接口模块
 * 文件路径: src/api/index.js
 * 功能描述: 封装所有与后端交互的 API 调用
 */

import request from '@/utils/request'

// ============================================
// 认证相关 API (Auth)
// ============================================

/**
 * 用户登录
 * @param {Object} data - 登录数据 { username, password, role }
 * @returns {Promise} 登录结果，包含 token 和用户信息
 */
export function login(data) {
    return request({
        url: '/auth/login',
        method: 'post',
        data
    })
}

/**
 * 用户注册
 * @param {Object} data - 注册数据
 * @returns {Promise} 注册结果
 */
export function register(data) {
    return request({
        url: '/auth/register',
        method: 'post',
        data
    })
}

/**
 * 退出登录
 * @returns {Promise}
 */
export function logout() {
    return request({
        url: '/auth/logout',
        method: 'post'
    })
}

/**
 * 获取当前登录用户信息
 * @returns {Promise} 用户信息
 */
export function getCurrentUser() {
    return request({
        url: '/users/me',
        method: 'get'
    })
}

// ============================================
// 用户管理 API (User)
// ============================================

/**
 * 获取用户分页列表
 * @param {Object} params - 查询参数 { pageNum, pageSize, username, role, status }
 * @returns {Promise} 分页用户列表
 */
export function getUserPage(params) {
    return request({
        url: '/users',
        method: 'get',
        params
    })
}

/**
 * 根据ID获取用户信息
 * @param {Number} id - 用户ID
 * @returns {Promise} 用户信息
 */
export function getUserById(id) {
    return request({
        url: `/users/${id}`,
        method: 'get'
    })
}

/**
 * 创建用户（管理员）
 * @param {Object} data - 用户数据
 * @returns {Promise}
 */
export function createUser(data) {
    return request({
        url: '/users',
        method: 'post',
        data
    })
}

/**
 * 更新用户信息
 * @param {Number} id - 用户ID
 * @param {Object} data - 更新数据
 * @returns {Promise}
 */
export function updateUser(id, data) {
    return request({
        url: `/users/${id}`,
        method: 'put',
        data
    })
}

/**
 * 删除用户
 * @param {Number} id - 用户ID
 * @returns {Promise}
 */
export function deleteUser(id) {
    return request({
        url: `/users/${id}`,
        method: 'delete'
    })
}

/**
 * 修改密码
 * @param {Object} data - { oldPassword, newPassword }
 * @returns {Promise}
 */
export function changePassword(data) {
    const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
    return request({
        url: `/users/${user.id}/password`,
        method: 'put',
        data
    })
}

/**
 * 用户充值
 * @param {Number} amount - 充值金额
 * @returns {Promise}
 */
export function chargeAccount(amount) {
    const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
    return request({
        url: `/users/${user.id}/charge`,
        method: 'put',
        data: { amount }
    })
}

/**
 * 获取商家列表
 * @returns {Promise}
 */
export function getMerchants() {
    return request({
        url: '/users/merchants',
        method: 'get'
    })
}

// ============================================
// 商品管理 API (Product)
// ============================================

/**
 * 获取商品分页列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getProductPage(params) {
    return request({
        url: '/products',
        method: 'get',
        params
    })
}

/**
 * 获取商品详情
 * @param {Number} id - 商品ID
 * @returns {Promise}
 */
export function getProductById(id) {
    return request({
        url: `/products/${id}`,
        method: 'get'
    })
}

/**
 * 创建商品
 * @param {Object} data - 商品数据
 * @returns {Promise}
 */
export function createProduct(data) {
    return request({
        url: '/products',
        method: 'post',
        data
    })
}

/**
 * 更新商品
 * @param {Number} id - 商品ID
 * @param {Object} data - 更新数据
 * @returns {Promise}
 */
export function updateProduct(id, data) {
    return request({
        url: `/products/${id}`,
        method: 'put',
        data
    })
}

/**
 * 上传文件
 * @param {FormData} formData - 包含文件的 FormData
 * @returns {Promise}
 */
export function uploadFile(formData) {
    return request({
        url: '/upload',
        method: 'post',
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

/**
 * 删除商品
 * @param {Number} id - 商品ID
 * @returns {Promise}
 */
export function deleteProduct(id) {
    return request({
        url: `/products/${id}`,
        method: 'delete'
    })
}

/**
 * 审核通过商品
 * @param {Number} id - 商品ID
 * @returns {Promise}
 */
export function approveProduct(id) {
    return request({
        url: `/products/${id}/approve`,
        method: 'put'
    })
}

/**
 * 获取热门商品
 * @param {Number} limit - 数量限制
 * @returns {Promise}
 */
export function getHotProducts(limit = 10) {
    return request({
        url: '/products/hot',
        method: 'get',
        params: { limit }
    })
}

/**
 * 获取新品商品
 * @param {Number} limit - 数量限制
 * @returns {Promise}
 */
export function getNewProducts(limit = 10) {
    return request({
        url: '/products/new',
        method: 'get',
        params: { limit }
    })
}

/**
 * 获取推荐商品
 * @param {Number} limit - 数量限制
 * @returns {Promise}
 */
export function getRecommendProducts(limit = 10) {
    return request({
        url: '/products/recommended',
        method: 'get',
        params: { limit }
    })
}

/**
 * 搜索商品
 * @param {String} keyword - 搜索关键词
 * @returns {Promise}
 */
export function searchProducts(keyword) {
    return request({
        url: '/products/search',
        method: 'get',
        params: { keyword }
    })
}

export function getMerchantProductPage(params) {
    return request({
        url: '/merchant/products',
        method: 'get',
        params
    })
}

export function getPendingProducts(params) {
    return request({
        url: '/products/pending',
        method: 'get',
        params
    })
}

export function adminApproveProduct(id) {
    return request({
        url: `/products/${id}/approve`,
        method: 'put'
    })
}

export function adminRejectProduct(id) {
    return request({
        url: `/products/${id}/reject`,
        method: 'put'
    })
}

// ============================================
// 订单管理 API (Order)
// ============================================

/**
 * 获取订单分页列表
 * @param {Object} params - 查询参数 { page, pageSize, status }
 * @returns {Promise}
 */
export function getOrderPage(params) {
    return request({
        url: '/orders',
        method: 'get',
        params
    })
}

/**
 * 获取订单详情
 * @param {Number} id - 订单ID
 * @returns {Promise}
 */
export function getOrderById(id) {
    return request({
        url: `/orders/${id}`,
        method: 'get'
    })
}

/**
 * 根据订单号获取订单
 * @param {String} orderNo - 订单号
 * @returns {Promise}
 */
export function getOrderByNo(orderNo) {
    return request({
        url: '/orders/detail',
        method: 'get',
        params: { orderNo }
    })
}

/**
 * 创建订单
 * @param {Object} data - 订单数据
 * @returns {Promise}
 */
export function createOrder(data) {
    return request({
        url: '/orders',
        method: 'post',
        data
    })
}

/**
 * 取消订单
 * @param {Number} id - 订单ID
 * @param {String} reason - 取消原因
 * @returns {Promise}
 */
export function cancelOrder(id, reason) {
    return request({
        url: `/orders/${id}/cancel`,
        method: 'put',
        data: reason ? { reason } : {}
    })
}

/**
 * 支付订单
 * @param {Number} id - 订单ID
 * @param {String} paymentMethod - 支付方式
 * @returns {Promise}
 */
export function payOrder(id, payData) {
    return request({
        url: `/orders/${id}/pay`,
        method: 'put',
        data: typeof payData === 'string' ? { paymentMethod: payData } : payData
    })
}

/**
 * 确认收货
 * @param {Number} id - 订单ID
 * @returns {Promise}
 */
export function confirmReceive(id) {
    return request({
        url: `/orders/${id}/confirm`,
        method: 'put'
    })
}

export function updateOrderAddress(id, data) {
    return request({
        url: `/orders/${id}/address`,
        method: 'put',
        data
    })
}

/**
 * 更换订单优惠券
 * @param {Number} id - 订单ID
 * @param {Number|null} couponId - 新优惠券ID（null表示取消优惠券）
 * @returns {Promise}
 */
export function changeOrderCoupon(id, couponId) {
    return request({
        url: `/orders/${id}/coupon`,
        method: 'put',
        data: { couponId }
    })
}

/**
 * 获取订单统计
 * @returns {Promise}
 */
export function getOrderStats() {
    return request({
        url: '/orders/stats',
        method: 'get'
    })
}

/**
 * 获取商家订单列表
 * @param {Object} params - 查询参数 { page, pageSize, status }
 * @returns {Promise}
 */
export function getMerchantOrders(params) {
    return request({
        url: '/merchant/orders',
        method: 'get',
        params
    })
}

/**
 * 商家发货
 * @param {Number} id - 订单ID
 * @param {Object} data - { expressCompany, expressNo }
 * @returns {Promise}
 */
export function merchantShipOrder(id, data) {
    return request({
        url: `/merchant/orders/${id}/ship`,
        method: 'put',
        data
    })
}

// ============================================
// 售后管理 API (After-Sales)
// ============================================

/**
 * 创建售后申请
 * @param {Object} data - { orderId, productId, type, quantity, reason, description }
 * @returns {Promise}
 */
export function createAfterSales(data) {
    return request({
        url: '/after-sales',
        method: 'post',
        data
    })
}

/**
 * 获取用户售后列表
 * @param {Object} params - { page, pageSize, status }
 * @returns {Promise}
 */
export function getUserAfterSales(params) {
    return request({
        url: '/after-sales',
        method: 'get',
        params
    })
}

/**
 * 获取售后详情
 * @param {Number} id - 售后ID
 * @returns {Promise}
 */
export function getAfterSalesById(id) {
    return request({
        url: `/after-sales/${id}`,
        method: 'get'
    })
}

/**
 * 取消售后申请
 * @param {Number} id - 售后ID
 * @returns {Promise}
 */
export function cancelAfterSales(id) {
    return request({
        url: `/after-sales/${id}/cancel`,
        method: 'put'
    })
}

/**
 * 填写退货物流信息
 * @param {Number} id - 售后ID
 * @param {Object} data - { expressCompany, expressNo }
 * @returns {Promise}
 */
export function updateAfterSalesExpress(id, data) {
    return request({
        url: `/after-sales/${id}/express`,
        method: 'put',
        data
    })
}

// ============================================
// 购物车 API (Cart)
// ============================================

/**
 * 获取用户购物车
 * @returns {Promise}
 */
export function getUserCart() {
    return request({
        url: '/cart',
        method: 'get'
    })
}

/**
 * 添加到购物车
 * @param {Object} data - { productId, count }
 * @returns {Promise}
 */
export function addToCart(data) {
    return request({
        url: '/cart',
        method: 'post',
        data: {
            productId: data.productId,
            count: data.count || data.quantity || 1
        }
    })
}

/**
 * 更新购物车商品数量
 * @param {Number} id - 购物车项ID
 * @param {Number} quantity - 数量
 * @returns {Promise}
 */
export function updateCartItem(id, quantity) {
    return request({
        url: `/cart/${id}`,
        method: 'put',
        data: { quantity }
    })
}

/**
 * 从购物车移除
 * @param {Number} id - 购物车项ID
 * @returns {Promise}
 */
export function removeFromCart(id) {
    return request({
        url: `/cart/${id}`,
        method: 'delete'
    })
}

/**
 * 清空用户购物车
 * @returns {Promise}
 */
export function clearUserCart() {
    return request({
        url: '/cart/clear',
        method: 'delete'
    })
}

// ============================================
// 收藏 API (Favorite)
// ============================================

/**
 * 获取用户收藏列表
 * @returns {Promise}
 */
export function getUserFavorites() {
    return request({
        url: '/favorites',
        method: 'get'
    })
}

/**
 * 添加收藏
 * @param {Object} data - { productId }
 * @returns {Promise}
 */
export function addFavorite(data) {
    return request({
        url: '/favorites',
        method: 'post',
        data: { productId: data.productId || data.targetId }
    })
}

/**
 * 取消收藏
 * @param {Number} id - 收藏ID或商品ID
 * @param {Boolean} byProductId - 是否通过商品ID删除
 * @returns {Promise}
 */
export function removeFavorite(id, byProductId = true) {
    return request({
        url: `/favorites/${byProductId ? 0 : id}`,
        method: 'delete',
        params: byProductId ? { productId: id } : {}
    })
}

// ============================================
// 收货地址 API (Address)
// ============================================

/**
 * 获取收货地址列表
 * @returns {Promise}
 */
export function getAddressList() {
    return request({
        url: '/addresses',
        method: 'get'
    })
}

/**
 * 添加收货地址
 * @param {Object} data - 地址数据
 * @returns {Promise}
 */
export function addAddress(data) {
    return request({
        url: '/addresses',
        method: 'post',
        data
    })
}

/**
 * 更新收货地址
 * @param {Number} id - 地址ID
 * @param {Object} data - 地址数据
 * @returns {Promise}
 */
export function updateAddress(id, data) {
    return request({
        url: `/addresses/${id}`,
        method: 'put',
        data
    })
}

/**
 * 删除收货地址
 * @param {Number} id - 地址ID
 * @returns {Promise}
 */
export function deleteAddress(id) {
    return request({
        url: `/addresses/${id}`,
        method: 'delete'
    })
}

/**
 * 设置默认收货地址
 * @param {Number} id - 地址ID
 * @returns {Promise}
 */
export function setDefaultAddress(id) {
    return request({
        url: `/addresses/${id}/default`,
        method: 'put'
    })
}

// ============================================
// 优惠券 API (UserCoupon)
// ============================================

/**
 * 获取结算时可用的优惠券
 * @param {Number} orderAmount - 订单金额
 * @returns {Promise}
 */
export function getCheckoutCoupons(orderAmount) {
    return request({
        url: '/user-coupon/checkout',
        method: 'get',
        params: { orderAmount }
    })
}

// ============================================
// 优惠券管理 API (Admin Coupon)
// ============================================

/**
 * 获取优惠券分页列表
 * @param {Object} params - { pageNum, pageSize, name, status }
 * @returns {Promise}
 */
export function getCouponPage(params) {
    return request({
        url: '/coupon/admin/page',
        method: 'get',
        params
    })
}

/**
 * 创建优惠券
 * @param {Object} data - { name, amount, minAmount, startTime, endTime, totalNum, source, description }
 * @returns {Promise}
 */
export function createCoupon(data) {
    return request({
        url: '/coupon/admin',
        method: 'post',
        data
    })
}

/**
 * 更新优惠券
 * @param {Number} id - 优惠券ID
 * @param {Object} data - 更新数据
 * @returns {Promise}
 */
export function updateCoupon(id, data) {
    return request({
        url: `/coupon/admin/${id}`,
        method: 'put',
        data
    })
}

/**
 * 删除优惠券
 * @param {Number} id - 优惠券ID
 * @returns {Promise}
 */
export function deleteCoupon(id) {
    return request({
        url: `/coupon/admin/${id}`,
        method: 'delete'
    })
}

/**
 * 获取优惠券使用历史
 * @param {Object} params - { pageNum, pageSize, couponId, username }
 * @returns {Promise}
 */
export function getCouponUsageHistory(params) {
    return request({
        url: '/user-coupon/history',
        method: 'get',
        params
    })
}

// ============================================
// 新闻管理 API (News)
// ============================================

/**
 * 获取新闻分页列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getNewsPage(params) {
    return request({
        url: '/news',
        method: 'get',
        params
    })
}

/**
 * 获取新闻详情
 * @param {Number} id - 新闻ID
 * @returns {Promise}
 */
export function getNewsById(id) {
    return request({
        url: `/news/${id}`,
        method: 'get'
    })
}

/**
 * 创建新闻
 * @param {Object} data - 新闻数据
 * @returns {Promise}
 */
export function createNews(data) {
    return request({
        url: '/news',
        method: 'post',
        data
    })
}

/**
 * 更新新闻
 * @param {Number} id - 新闻ID
 * @param {Object} data - 更新数据
 * @returns {Promise}
 */
export function updateNews(id, data) {
    return request({
        url: `/news/${id}`,
        method: 'put',
        data
    })
}

/**
 * 删除新闻
 * @param {Number} id - 新闻ID
 * @returns {Promise}
 */
export function deleteNews(id) {
    return request({
        url: `/news/${id}`,
        method: 'delete'
    })
}

/**
 * 点赞新闻
 * @param {Number} id - 新闻ID
 * @returns {Promise}
 */
export function likeNews(id) {
    return request({
        url: `/news/${id}/like`,
        method: 'post'
    })
}

/**
 * 分享新闻
 * @param {Number} id - 新闻ID
 * @returns {Promise}
 */
export function shareNews(id) {
    return request({
        url: `/news/${id}/share`,
        method: 'post'
    })
}

/**
 * 获取商品评价
 * @param {Number} productId - 商品ID
 * @param {Object} params - 分页参数
 * @returns {Promise}
 */
export function getProductReviews(productId, params) {
    return request({
        url: `/products/${productId}/reviews`,
        method: 'get',
        params
    })
}

// ============================================
// 登录历史 API (Login History)
// ============================================

/**
 * 获取登录历史（管理员）
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getLoginHistory(params) {
    return request({
        url: '/login-history',
        method: 'get',
        params
    })
}

// ============================================
// 系统设置 API (System Settings)
// ============================================

/**
 * 获取系统设置
 * @param {String} type - 设置类型 (basic/product/user/notification)，不传则获取所有
 * @returns {Promise}
 */
export function getSettings(type) {
    return request({
        url: '/admin/settings',
        method: 'get',
        params: type ? { type } : {}
    })
}

/**
 * 保存系统设置
 * @param {Object} data - { type, value }
 * @returns {Promise}
 */
export function saveSettings(data) {
    return request({
        url: '/admin/settings',
        method: 'post',
        data
    })
}

/**
 * 批量保存系统设置
 * @param {Object} settings - { basic: '...', product: '...', ... }
 * @returns {Promise}
 */
export function saveSettingsBatch(settings) {
    return request({
        url: '/admin/settings/batch',
        method: 'post',
        data: settings
    })
}

// ============================================
// 默认导出
// ============================================

export default {
    // Auth
    login,
    register,
    logout,
    getCurrentUser,
    // User
    getUserPage,
    getUserById,
    createUser,
    updateUser,
    deleteUser,
    changePassword,
    getMerchants,
    // Product
    getProductPage,
    getProductById,
    createProduct,
    updateProduct,
    deleteProduct,
    approveProduct,
    getHotProducts,
    getNewProducts,
    getRecommendProducts,
    searchProducts,
    getMerchantProductPage,
    getPendingProducts,
    adminApproveProduct,
    adminRejectProduct,
    // Order
    getOrderPage,
    getOrderById,
    getOrderByNo,
    createOrder,
    cancelOrder,
    payOrder,
    confirmReceive,
    getOrderStats,
    getMerchantOrders,
    merchantShipOrder,
    // After-Sales
    createAfterSales,
    getUserAfterSales,
    getAfterSalesById,
    cancelAfterSales,
    updateAfterSalesExpress,
    // Cart
    getUserCart,
    addToCart,
    updateCartItem,
    removeFromCart,
    clearUserCart,
    // Favorite
    getUserFavorites,
    addFavorite,
    removeFavorite,
    // Address
    getAddressList,
    addAddress,
    updateAddress,
    deleteAddress,
    setDefaultAddress,
    // News
    getNewsPage,
    getNewsById,
    createNews,
    updateNews,
    deleteNews,
    likeNews,
    shareNews,
    // Review
    getProductReviews,
    // CouponAdmin
    getCouponPage,
    createCoupon,
    updateCoupon,
    deleteCoupon,
    getCouponUsageHistory,
    // LoginHistory
    getLoginHistory,
    // SystemSettings
    getSettings,
    saveSettings,
    saveSettingsBatch
}
