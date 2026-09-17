/**
 * 路由配置文件
 * 文件路径: src/router/index.js
 * 功能描述: 定义应用的路由结构和权限控制
 * 关联文件:
 * - src/main.js: 应用入口文件
 * - src/views/Login.vue: 登录页面
 * - src/views/Register.vue: 注册页面
 * - src/views/Front.vue: 前台布局
 * - src/views/Manager.vue: 后台管理布局
 * - src/views/404.vue: 404页面
 * - src/views/manager/403.vue: 403页面
 */
import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

// 解决导航栏或者底部导航tabBar中的vue-router在3.0版本以上频繁点击菜单报错的问题。
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => err)
}

const routes = [
  {
    path: '/',
    name: 'Manager',
    component: () => import('../views/Manager.vue'),
    redirect: '/home',  // 管理员默认重定向到后台首页
    children: [
      { path: '403', name: 'NoAuth', meta: { name: '无权限' }, component: () => import('../views/manager/403.vue') },
      { path: 'home', name: 'Home', meta: { name: '管理员首页' }, component: () => import('../views/manager/Home.vue') },
      { path: 'admin', name: 'Admin', meta: { name: '管理员信息' }, component: () => import('../views/manager/Admin.vue') },
      { path: 'adminPerson', name: 'AdminPerson', meta: { name: '个人信息' }, component: () => import('../views/manager/AdminPerson.vue') },
      { path: 'notice', name: 'Notice', meta: { name: '公告管理' }, component: () => import('../views/manager/Notice.vue') },
      { path: 'publish-notice', name: 'PublishNotice', meta: { name: '发布公告' }, component: () => import('../views/manager/PublishNotice.vue') },
      { path: 'user', name: 'User', meta: { name: '用户信息' }, component: () => import('../views/manager/User.vue') },
      { path: 'pending-products', name: 'PendingProducts', meta: { name: '待审核商品' }, component: () => import('../views/manager/PendingProducts.vue') },
      { path: 'review/:id', name: 'ProductReview', meta: { name: '商品审核' }, component: () => import('../views/manager/ProductReview.vue') },
      {
        path: 'products',
        name: 'Products',
        meta: { name: '商品管理' },
        component: () => import('../views/manager/Products.vue')  // 这里已修复
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        meta: { name: '商品详情' },
        component: () => import('../views/manager/ProductDetail.vue')  // 添加了ProductDetail路由
      },
      { path: 'messages', name: 'Messages', meta: { name: '消息中心' }, component: () => import('../views/manager/Messages.vue') },
      { path: 'user-center', name: 'UserCenter', meta: { name: '用户信息中心' }, component: () => import('../views/manager/UserCenter.vue') },
      { path: 'log-manage', name: 'LogManage', meta: { name: '日志管理' }, component: () => import('../views/manager/LogManage.vue') },
      { path: 'statistics', name: 'Statistics', meta: { name: '数据统计' }, component: () => import('../views/manager/Statistics.vue') },
      { path: 'reports', name: 'Reports', meta: { name: '报表中心' }, component: () => import('../views/manager/Reports.vue') },
      { path: 'settings', name: 'Settings', meta: { name: '系统设置' }, component: () => import('../views/manager/Settings.vue') },
      { path: 'system/log', name: 'SystemLog', meta: { name: '系统日志' }, component: () => import('../views/manager/Log.vue') },
      { path: 'login-history', name: 'LoginHistory', meta: { name: '登录历史' }, component: () => import('../views/manager/LoginHistory.vue') },
      { path: 'data-backup', name: 'DataBackup', meta: { name: '数据备份' }, component: () => import('../views/manager/DataBackup.vue') },
      { path: 'merchant', name: 'Merchant', meta: { name: '商家管理' }, component: () => import('../views/manager/Merchant.vue') },
      { path: 'server-status', name: 'ServerStatus', meta: { name: '服务器状态' }, component: () => import('../views/manager/ServerStatus.vue') },
      { path: 'performance', name: 'Performance', meta: { name: '性能监控' }, component: () => import('../views/manager/Performance.vue') },
      { path: 'coupon-manage', name: 'CouponManage', meta: { name: '优惠券管理' }, component: () => import('../views/manager/CouponManage.vue') },
      { path: 'coupon-history', name: 'CouponHistory', meta: { name: '使用历史' }, component: () => import('../views/manager/CouponHistory.vue') },
    ]
  },
  {
    path: '/front',
    name: 'Front',
    redirect: '/front/home',
    component: () => import('../views/Front.vue'),
    children: [
      { path: 'home', name: 'FrontHome', meta: { name: '系统首页' }, component: () => import('../views/front/Home.vue') },
      { path: 'product/:id', name: 'FrontProductDetail', meta: { name: '商品详情' }, component: () => import('../views/front/ProductDetail.vue') },
      { path: 'news', name: 'FrontNews', meta: { name: '资讯列表' }, component: () => import('../views/front/News.vue') },
      { path: 'news/:id', name: 'FrontNewsDetail', meta: { name: '资讯详情' }, component: () => import('../views/front/NewsDetail.vue') },
      { path: 'my-orders', name: 'FrontMyOrders', meta: { name: '我的订单' }, component: () => import('../views/front/MyOrders.vue') },
      { path: 'cart', name: 'FrontCart', meta: { name: '购物车' }, component: () => import('../views/front/Cart.vue') },
      { path: 'favorites', name: 'FrontFavorites', meta: { name: '我的收藏' }, component: () => import('../views/front/Favorites.vue') },
      { path: 'order/:orderId', name: 'FrontOrderDetail', meta: { name: '订单详情' }, component: () => import('../views/front/OrderDetail.vue') },
      { path: 'products', name: 'FrontProducts', meta: { name: '商品列表' }, component: () => import('../views/front/Products.vue') },
      { path: 'coupons', name: 'FrontCoupons', meta: { name: '优惠券' }, component: () => import('../views/front/Coupons.vue') },
      { path: 'my-coupons', name: 'FrontMyCoupons', meta: { name: '我的优惠券' }, component: () => import('../views/front/MyCoupons.vue') },
      { path: 'person', name: 'FrontPerson', meta: { name: '个人信息' }, component: () => import('../views/front/UserPerson.vue') },
      { path: 'user-person', name: 'UserPerson', meta: { name: '用户中心' }, component: () => import('../views/front/UserPerson.vue') },
      { path: 'merchant/publish', name: 'MerchantPublish', meta: { name: '发布商品' }, component: () => import('../views/front/merchant/PublishProduct.vue') },
      { path: 'merchant/products', name: 'MerchantProducts', meta: { name: '我的商品' }, component: () => import('../views/front/merchant/MyProducts.vue') },
      { path: 'merchant/orders', name: 'MerchantOrders', meta: { name: '订单管理' }, component: () => import('../views/front/merchant/OrderManage.vue') },
    ]
  },
  { path: '/login', name: 'Login', meta: { name: '登录' }, component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', meta: { name: '注册' }, component: () => import('../views/Register.vue') },
  { path: '*', name: 'NotFound', meta: { name: '无法访问' }, component: () => import('../views/404.vue') },
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// 公开路径（不需要登录）
const PUBLIC_PATHS = ['/login', '/register']

// 管理员专用路径
const ADMIN_ONLY_PATHS = [
  '/home', '/admin', '/adminPerson', '/notice',
  '/user', '/pending-products', '/review/:id', '/products', '/product/:id',
  '/publish-notice', '/messages', '/user-center', '/log-manage', '/statistics', '/reports', '/settings', '/data-backup', '/merchant',
  '/system/log', '/login-history'
]

// 前台农户专用路径
const MERCHANT_ONLY_FRONT_PATHS = ['/front/merchant/publish', '/front/merchant/products']

// 根据角色获取跳转路径的函数
function getLandingPathByRole(role) {
  switch (role) {
    case 'ADMIN':
      return '/home'  // 管理员跳转到后台首页
    case 'USER':
    case 'MERCHANT':
      return '/front/home'  // 普通用户和农户跳转到前台首页
    default:
      return '/login'
  }
}

// 获取用户信息的辅助函数（同时检查 sessionStorage 和 localStorage）
function getUserInfo() {
  try {
    // 优先从 sessionStorage 读取
    let userStr = sessionStorage.getItem('xm-user')
    let token = sessionStorage.getItem('xm-token')

    // 如果 sessionStorage 没有，尝试从 localStorage 读取
    if (!userStr || !token) {
      userStr = localStorage.getItem('xm-user')
      token = localStorage.getItem('xm-token')
    }

    if (userStr && token) {
      const user = JSON.parse(userStr)
      if (user && user.role) {
        return { user, token, hasLogin: true }
      }
    }
  } catch (error) {
    console.error('[路由守卫] 解析用户信息失败:', error)
  }
  return { user: null, token: null, hasLogin: false }
}

// 路由守卫：控制不同角色的访问权限
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta && to.meta.name) {
    document.title = `${to.meta.name} - 农产品直供平台`
  }

  // 如果已经在登录页，直接放行，避免重定向循环
  if (to.path === '/login') {
    next()
    return
  }

  const { user, token, hasLogin } = getUserInfo()
  const isPublicPath = PUBLIC_PATHS.includes(to.path)

  // 1. 已登录用户访问登录/注册页，跳转到对应首页
  if (hasLogin && isPublicPath) {
    const landingPath = getLandingPathByRole(user.role)
    if (to.path !== landingPath) {
      next(landingPath)
    } else {
      next()
    }
    return
  }

  // 2. 未登录用户访问非公开页面，跳转到登录页
  if (!hasLogin && !isPublicPath) {
    next('/login')
    return
  }

  // 3. 访问根路径 '/'，根据登录状态跳转
  if (to.path === '/') {
    if (hasLogin) {
      const landingPath = getLandingPathByRole(user.role)
      next(landingPath)
    } else {
      next('/login')
    }
    return
  }

  // 4. 权限控制：管理员路径只有管理员可以访问
  if (to.matched.some(route => {
    return ADMIN_ONLY_PATHS.some(adminPath =>
      route.path === adminPath || route.path.startsWith(adminPath.replace(/:id/, ''))
    )
  })) {
    if (user.role !== 'ADMIN') {
      next('/403')
      return
    }
  }

  // 5. 权限控制：前台农户专用路径只有农户可以访问
  if (to.matched.some(route => {
    return MERCHANT_ONLY_FRONT_PATHS.some(merchantPath =>
      route.path === merchantPath || route.path.startsWith(merchantPath)
    )
  })) {
    if (user.role !== 'MERCHANT') {
      next('/front/home')
      return
    }
  }

  // 6. 其他情况放行
  next()
})

// 捕获路由重定向错误，避免控制台报错
router.onError((error) => {
  const pattern = /Redirected when going from/
  if (!pattern.test(error.message)) {
    console.error('路由错误:', error)
  }
})

export default router