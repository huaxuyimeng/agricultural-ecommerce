/**
 * 认证配置文件
 * 文件路径: src/config/auth.js
 * 功能描述: 处理用户认证、权限管理和用户数据操作
 * 关联文件:
 * - src/data/user.js: 用户数据管理
 * - src/router/index.js: 路由权限控制
 */
// 注意：不再导入 userDataManager，避免函数冲突

// 角色选项
export const ROLE_OPTIONS = [
  { value: 'ADMIN', label: '平台管理员' },
  { value: 'MERCHANT', label: '农户/商家' },
  { value: 'USER', label: '普通用户' }
]

// 从localStorage获取用户数据，如果没有则使用初始数据
function getUsersFromStorage() {
  try {
    const storedUsers = localStorage.getItem('xm-all-users')
    if (storedUsers) {
      return JSON.parse(storedUsers)
    }
  } catch (error) {
    console.error('读取用户数据失败:', error)
  }
  return null
}

// 保存用户数据到localStorage
function saveUsersToStorage(users) {
  try {
    localStorage.setItem('xm-all-users', JSON.stringify(users))
  } catch (error) {
    console.error('保存用户数据失败:', error)
  }
}

// 初始用户数据
const INITIAL_USERS = []

// 获取用户数据（优先从localStorage，没有则使用初始数据）
function getAllUsers() {
  const storedUsers = getUsersFromStorage()
  if (storedUsers && storedUsers.length > 0) {
    return storedUsers
  }
  // 初始化存储
  saveUsersToStorage(INITIAL_USERS)
  return INITIAL_USERS
}

// 导出用户数据
export const MOCK_USERS = getAllUsers()

// 根据角色跳转的路径映射
export const LANDING_PATH_BY_ROLE = {
  USER: '/front/home',
  MERCHANT: '/front/home',
  ADMIN: '/home'
}

// 用户统计
export const userStats = {
  get total() { return getAllUsers().length },
  get admins() { return getAllUsers().filter(u => u.role === 'ADMIN').length },
  get merchants() { return getAllUsers().filter(u => u.role === 'MERCHANT').length },
  get users() { return getAllUsers().filter(u => u.role === 'USER').length }
}

// 前端用户菜单
export const FRONT_USER_MENUS = [
  { text: '首页', path: '/front/home', icon: 'el-icon-s-home' },
  { text: '全部商品', path: '#all-products', icon: 'el-icon-s-goods' },
  { text: '农产品', path: '#agricultural', icon: 'el-icon-s-marketing' },
  { text: '加工产品', path: '#processed', icon: 'el-icon-s-goods' },
  { text: '优惠券', path: '/front/coupons', icon: 'el-icon-present' },
  { text: '我的订单', path: '#my-orders', icon: 'el-icon-s-order' },
  { text: '最新咨询', path: '#latest-news', icon: 'el-icon-s-promotion' }
]

// 前端农户菜单
export const FRONT_MERCHANT_MENUS = [
  { text: '首页', path: '/front/home', icon: 'el-icon-s-home' },
  { text: '全部商品', path: '#all-products', icon: 'el-icon-s-goods' },
  { text: '发布商品', path: '/front/merchant/publish', icon: 'el-icon-edit' },
  { text: '我的商品', path: '/front/merchant/products', icon: 'el-icon-s-goods' },
  { text: '订单管理', path: '/front/merchant/orders', icon: 'el-icon-s-order' },
  { text: '最新咨询', path: '#latest-news', icon: 'el-icon-s-promotion' }
]

// 用户管理函数
export const userManager = {
  // 获取所有用户
  getAllUsers,

  // 根据ID获取用户
  getUserById(id) {
    return getAllUsers().find(user => user.id === id)
  },

  // 根据用户名获取用户
  getUserByUsername(username) {
    return getAllUsers().find(user => user.username === username)
  },

  // 验证用户登录
  validateUserLogin(username, password, role) {
    return getAllUsers().find(user =>
      user.username === username &&
      user.password === password &&
      user.role === role
    )
  },

  // 添加用户
  addUser(userData) {
    const users = getAllUsers()
    const newId = users.length > 0 ? Math.max(...users.map(u => u.id)) + 1 : 1
    const newUser = {
      id: newId,
      username: userData.username,
      name: userData.name || userData.username,
      password: userData.password || '123456',
      role: userData.role || 'USER',
      avatar: userData.avatar || '/imgs/user/admin.png',
      email: userData.email || '',
      phone: userData.phone || '',
      account: userData.account || 0.00,
      token: `${(userData.role || 'USER').toLowerCase()}_token_${newId}`,
      shopName: userData.role === 'MERCHANT' ? (userData.shopName || `${userData.name}的农场`) : null
    }

    users.push(newUser)
    saveUsersToStorage(users)
    return newUser
  },

  // 更新用户
  updateUser(id, userData) {
    const users = getAllUsers()
    const index = users.findIndex(user => user.id === id)
    if (index !== -1) {
      users[index] = { ...users[index], ...userData }
      saveUsersToStorage(users)
      return users[index]
    }
    return null
  },

  // 删除用户
  deleteUser(id) {
    const users = getAllUsers()
    const index = users.findIndex(user => user.id === id)
    if (index !== -1) {
      const deletedUser = users.splice(index, 1)[0]
      saveUsersToStorage(users)
      return deletedUser
    }
    return null
  },

  // 搜索用户
  searchUsers(keyword) {
    const lowerKeyword = keyword.toLowerCase()
    return getAllUsers().filter(user =>
      user.username.toLowerCase().includes(lowerKeyword) ||
      user.name.toLowerCase().includes(lowerKeyword) ||
      user.email.toLowerCase().includes(lowerKeyword) ||
      user.phone.includes(keyword) ||
      (user.role && user.role.toLowerCase().includes(lowerKeyword))
    )
  },

  // 重置密码
  resetPassword(id, newPassword = '123456') {
    return this.updateUser(id, { password: newPassword })
  },

  // 获取用户统计
  getUserStats() {
    return userStats
  },

  // 重置为初始数据
  resetToInitial() {
    saveUsersToStorage(INITIAL_USERS)
    return INITIAL_USERS
  }
}