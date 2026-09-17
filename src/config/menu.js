export const MENU_CONFIG = [
  {
    index: '/home',
    title: '系统首页',
    icon: 'el-icon-s-home',
    type: 'item'
  },
  {
    index: 'info',
    title: '信息管理',
    icon: 'el-icon-document',
    type: 'submenu',
    children: [
      { index: '/notice', title: '公告管理', icon: 'el-icon-bell', roles: ['ADMIN'] },
      { index: '/publish-notice', title: '发布公告', icon: 'el-icon-edit', roles: ['ADMIN'] },
      { index: '/products', title: '商品信息', icon: 'el-icon-goods', roles: ['MERCHANT'] },
      { index: '/pending-products', title: '商品审核', icon: 'el-icon-view', roles: ['ADMIN'] },
      { index: '/products', title: '商品管理', icon: 'el-icon-s-management', roles: ['ADMIN'] }
    ]
  },
  {
    index: 'user',
    title: '用户管理',
    icon: 'el-icon-user',
    type: 'submenu',
    roles: ['ADMIN'],
    children: [
      { index: '/admin', title: '管理员信息', icon: 'el-icon-s-custom' },
      { index: '/user', title: '用户信息', icon: 'el-icon-user-solid' },
      { index: '/merchant', title: '商家管理', icon: 'el-icon-shop' }
    ]
  },
  {
    index: 'system',
    title: '系统设置',
    icon: 'el-icon-setting',
    type: 'submenu',
    roles: ['ADMIN'],
    children: [
      { index: '/user-center', title: '用户信息中心', icon: 'el-icon-user-solid' },
      { index: '/settings', title: '系统配置', icon: 'el-icon-s-tools' },
      { index: '/log-manage', title: '操作日志', icon: 'el-icon-notebook-2' },
      { index: '/data-backup', title: '数据备份', icon: 'el-icon-download' }
    ]
  },
  {
    index: 'stats',
    title: '统计分析',
    icon: 'el-icon-data-analysis',
    type: 'submenu',
    children: [
      { index: '/statistics', title: '数据统计', icon: 'el-icon-s-data' },
      { index: '/reports', title: '报表分析', icon: 'el-icon-s-data' }
    ]
  },
  {
    index: 'monitor',
    title: '系统监控',
    icon: 'el-icon-monitor',
    type: 'submenu',
    roles: ['ADMIN'],
    children: [
      { index: '/server-status', title: '服务器状态', icon: 'el-icon-monitor' },
      { index: '/performance', title: '性能监控', icon: 'el-icon-data-line' }
    ]
  }
]

export const USER_MENU_ITEMS = [
  { key: 'profile', icon: 'el-icon-user', label: '个人中心', divided: false },
  { key: 'settings', icon: 'el-icon-setting', label: '账户设置', divided: false },
  { key: 'help', icon: 'el-icon-question', label: '使用帮助', divided: true },
  { key: 'feedback', icon: 'el-icon-chat-dot-round', label: '问题反馈', divided: false },
  { key: 'about', icon: 'el-icon-info', label: '关于系统', divided: true },
  { key: 'logout', icon: 'el-icon-switch-button', label: '退出登录', divided: false }
]

export const QUICK_ACTIONS = [
  { key: 'refresh', icon: 'el-icon-refresh', title: '刷新页面', desc: '刷新当前页面', type: 'primary' },
  { key: 'back', icon: 'el-icon-back', title: '返回上一页', desc: '返回上一个页面', type: 'warning' },
  { key: 'dashboard', icon: 'el-icon-s-home', title: '返回首页', desc: '返回系统首页', type: 'success' },
  { key: 'fullscreen', icon: 'el-icon-full-screen', title: '全屏模式', desc: '切换全屏显示', type: 'info' }
]

export const SEARCH_OPTIONS = [
  { value: '系统首页', icon: 'el-icon-s-home', path: '/home' },
  { value: '用户管理', icon: 'el-icon-user', path: '/user' },
  { value: '商品管理', icon: 'el-icon-goods', path: '/products' },
  { value: '公告管理', icon: 'el-icon-bell', path: '/notice' },
  { value: '发布公告', icon: 'el-icon-edit', path: '/publish-notice' },
  { value: '用户信息中心', icon: 'el-icon-user-solid', path: '/user-center' },
  { value: '数据统计', icon: 'el-icon-s-data', path: '/statistics' }
]

export const DEFAULT_TAB_LIST = [
  { name: 'home', title: '系统首页', path: '/home', closable: false }
]

export const KEEP_ALIVE_COMPONENTS = ['Home', 'Products', 'User', 'Admin']
