/**
 * 后台管理框架布局组件
 * 文件路径: src/views/Manager.vue
 * 功能描述: 后台管理系统的整体布局框架，包含顶部导航栏（Logo、面包屑导航、功能搜索、刷新/全屏/暗黑模式切换、用户下拉菜单）、
 *           左侧可折叠菜单栏（根据角色动态展示：ADMIN可见信息管理/用户管理/系统设置/统计分析/监控运维/消息中心/日志审计，
 *           MERCHANT可见商品管理、商户可见订单管理）、右侧内容主区域（router-view），
 *           支持暗黑模式切换、全屏切换、菜单折叠/展开、页面刷新、退出登录
 * 关联文件:
 * - src/router/index.js: 后台管理路由配置
 * - src/store/index.js: 提供当前登录用户信息和角色
 * - src/views/manager/Home.vue: 后台首页（默认展示页面）
 * - src/views/manager/UserCenter.vue: 用户信息中心页面
 */
<template>
  <div class="manager-container">
    <div class="manager-header">
      <div class="header-left">
        <div class="logo-section" @click="goToHome">
          <div class="logo-wrapper">
            <img 
              v-if="!logoError" 
              src="@/assets/imgs/logo.png"
              alt="Logo" 
              class="logo"
              @error="handleLogoError"
            />
            <div v-else class="logo-fallback">
              {{ systemName.charAt(0) }}
            </div>
          </div>
          <span class="system-name" v-if="!isCollapsed">{{ systemName }}</span>
        </div>
        <div class="collapse-btn" @click="toggleSidebar">
          <i :class="isCollapsed ? 'el-icon-s-unfold' : 'el-icon-s-fold'"></i>
        </div>
      </div>

      <div class="header-center">
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item 
            v-for="(item, index) in breadcrumbList" 
            :key="index"
            :to="item.path"
          >
            <i v-if="index === 0" class="el-icon-s-home"></i>
            {{ item.name }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <div class="header-right">
        <div class="search-box" v-if="user.role === 'ADMIN'">
          <el-autocomplete
            v-model="searchKeyword"
            placeholder="搜索功能..."
            :fetch-suggestions="querySearch"
            @select="handleSearchSelect"
            size="small"
            clearable
            prefix-icon="el-icon-search"
          >
            <template #default="{ item }">
              <div class="search-item">
                <i :class="item.icon"></i>
                <span>{{ item.value }}</span>
              </div>
            </template>
          </el-autocomplete>
        </div>

        <div class="header-actions">
          <el-tooltip content="刷新" placement="bottom">
            <div class="action-icon" @click="handleRefresh">
              <i class="el-icon-refresh"></i>
            </div>
          </el-tooltip>

          <el-tooltip :content="isFullscreen ? '退出全屏' : '全屏'" placement="bottom">
            <div class="action-icon" @click="toggleFullscreen">
              <i :class="isFullscreen ? 'el-icon-close' : 'el-icon-full-screen'"></i>
            </div>
          </el-tooltip>

          <el-tooltip :content="isDarkMode ? '明亮模式' : '暗黑模式'" placement="bottom">
            <div class="action-icon" @click="toggleTheme">
              <i :class="isDarkMode ? 'el-icon-sunny' : 'el-icon-moon'"></i>
            </div>
          </el-tooltip>

          <el-dropdown trigger="click" placement="bottom-end" class="user-dropdown">
            <div class="user-trigger">
              <el-avatar :size="32" :src="userAvatar">
                {{ getUserInitial }}
              </el-avatar>
              <span class="user-name">{{ user.name || '管理员' }}</span>
              <i class="el-icon-arrow-down"></i>
            </div>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native="goToPerson">
                <i class="el-icon-user"></i> 个人中心
              </el-dropdown-item>
              <el-dropdown-item @click.native="openHelp">
                <i class="el-icon-question"></i> 使用帮助
              </el-dropdown-item>
              <el-dropdown-item divided @click.native="logout">
                <i class="el-icon-switch-button"></i> 退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
    </div>

    <div class="manager-main">
      <div class="sidebar" :class="{ collapsed: isCollapsed }">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapsed"
          router
          class="sidebar-menu"
          background-color="#001529"
          text-color="#bfcbd9"
          active-text-color="#ffffff"
          :collapse-transition="false"
          :unique-opened="true"
        >
          <el-menu-item index="/home">
            <i class="el-icon-s-home"></i>
            <span>系统首页</span>
          </el-menu-item>
          
          <el-submenu index="info">
            <template #title>
              <i class="el-icon-document"></i>
              <span>信息管理</span>
            </template>
            <el-menu-item index="/notice" v-if="user.role === 'ADMIN'">
              <i class="el-icon-bell"></i>
              <span>公告管理</span>
            </el-menu-item>
            <el-menu-item index="/publish-notice" v-if="user.role === 'ADMIN'">
              <i class="el-icon-edit"></i>
              <span>发布公告</span>
            </el-menu-item>
            <el-menu-item index="/products" v-if="user.role === 'MERCHANT'">
              <i class="el-icon-goods"></i>
              <span>商品信息</span>
            </el-menu-item>
            <el-menu-item index="/pending-products" v-if="user.role === 'ADMIN'">
              <i class="el-icon-view"></i>
              <span>商品审核</span>
            </el-menu-item>
            <el-menu-item index="/products" v-if="user.role === 'ADMIN'">
              <i class="el-icon-s-management"></i>
              <span>商品管理</span>
            </el-menu-item>
          </el-submenu>

          <el-submenu index="user" v-if="user.role === 'ADMIN'">
            <template #title>
              <i class="el-icon-user"></i>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/admin">
              <i class="el-icon-s-custom"></i>
              <span>管理员信息</span>
            </el-menu-item>
            <el-menu-item index="/user">
              <i class="el-icon-user-solid"></i>
              <span>用户信息</span>
            </el-menu-item>
            <el-menu-item index="/merchant">
              <i class="el-icon-s-shop"></i>
              <span>商家管理</span>
            </el-menu-item>
          </el-submenu>
          
          <el-submenu index="system" v-if="user.role === 'ADMIN'">
            <template #title>
              <i class="el-icon-setting"></i>
              <span>系统设置</span>
            </template>
            <el-menu-item index="/user-center">
              <i class="el-icon-user-solid"></i>
              <span>用户信息中心</span>
            </el-menu-item>
            <el-menu-item index="/settings">
              <i class="el-icon-s-tools"></i>
              <span>系统配置</span>
            </el-menu-item>
            <el-menu-item index="/log-manage">
              <i class="el-icon-notebook-2"></i>
              <span>操作日志</span>
            </el-menu-item>
            <el-menu-item index="/data-backup">
              <i class="el-icon-download"></i>
              <span>数据备份</span>
            </el-menu-item>
          </el-submenu>
          
          <el-submenu index="stats">
            <template #title>
              <i class="el-icon-data-analysis"></i>
              <span>统计分析</span>
            </template>
            <el-menu-item index="/statistics">
              <i class="el-icon-s-data"></i>
              <span>数据统计</span>
            </el-menu-item>
            <el-menu-item index="/reports">
              <i class="el-icon-s-data"></i>
              <span>报表分析</span>
            </el-menu-item>
          </el-submenu>

          <el-submenu index="monitor" v-if="user.role === 'ADMIN'">
            <template #title>
              <i class="el-icon-monitor"></i>
              <span>系统监控</span>
            </template>
            <el-menu-item index="/server-status">
              <i class="el-icon-monitor"></i>
              <span>服务器状态</span>
            </el-menu-item>
            <el-menu-item index="/performance">
              <i class="el-icon-data-line"></i>
              <span>性能监控</span>
            </el-menu-item>
          </el-submenu>

          <el-submenu index="marketing" v-if="user.role === 'ADMIN'">
            <template #title>
              <i class="el-icon-present"></i>
              <span>营销管理</span>
            </template>
            <el-menu-item index="/coupon-manage">
              <i class="el-icon-s-ticket"></i>
              <span>优惠券管理</span>
            </el-menu-item>
            <el-menu-item index="/coupon-history">
              <i class="el-icon-time"></i>
              <span>使用历史</span>
            </el-menu-item>
          </el-submenu>
        </el-menu>
      </div>

      <div class="main-content" :class="{ collapsed: isCollapsed }">
        <div class="tabs-bar" v-if="showTabs && tabList.length > 1">
          <el-tabs
            v-model="activeTab"
            type="card"
            closable
            @tab-click="handleTabClick"
            @tab-remove="removeTab"
          >
            <el-tab-pane
              v-for="tab in tabList"
              :key="tab.name"
              :label="tab.title"
              :name="tab.name"
              :closable="tab.closable !== false"
            />
          </el-tabs>
          <el-dropdown trigger="click" @command="handleTabsAction" class="tabs-more">
            <i class="el-icon-more"></i>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="refresh">刷新当前页</el-dropdown-item>
              <el-dropdown-item command="closeCurrent">关闭当前页</el-dropdown-item>
              <el-dropdown-item command="closeOther">关闭其他页</el-dropdown-item>
              <el-dropdown-item command="closeAll" divided>关闭所有页</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>

        <div class="content-area">
          <router-view v-slot="{ Component }">
            <transition name="fade-transform" mode="out-in">
              <keep-alive :include="keepAliveComponents">
                <component 
                  :is="Component" 
                  :key="$route.fullPath" 
                  @update:user="updateUser"
                />
              </keep-alive>
            </transition>
          </router-view>
        </div>
      </div>
    </div>

    <el-dialog
      title="使用帮助"
      :visible.sync="helpDialogVisible"
      width="560px"
    >
      <div class="help-content">
        <h3>常用快捷键</h3>
        <div class="shortcuts-grid">
          <div class="shortcut-item">
            <kbd>F1</kbd>
            <span>帮助</span>
          </div>
          <div class="shortcut-item">
            <kbd>F5</kbd>
            <span>刷新</span>
          </div>
          <div class="shortcut-item">
            <kbd>Ctrl+B</kbd>
            <span>侧边栏</span>
          </div>
          <div class="shortcut-item">
            <kbd>Ctrl+F</kbd>
            <span>搜索</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="helpDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { 
  SEARCH_OPTIONS, 
  DEFAULT_TAB_LIST, 
  KEEP_ALIVE_COMPONENTS 
} from '@/config/menu'

export default {
  name: "ManagerLayout",
  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      isCollapsed: false,
      isFullscreen: false,
      isDarkMode: false,
      isMobile: false,
      showTabs: true,
      helpDialogVisible: false,
      searchKeyword: '',
      activeMenu: '/home',
      activeTab: 'home',
      currentTime: '',
      onlineUsers: 5,
      systemName: '后台管理系统',
      systemVersion: '1.0.0',
      breadcrumbList: [],
      tabList: [...DEFAULT_TAB_LIST],
      keepAliveComponents: [...KEEP_ALIVE_COMPONENTS],
      searchOptions: [...SEARCH_OPTIONS],
      logoError: false,
      timeInterval: null,
      userInterval: null
    }
  },
  computed: {
    userAvatar() {
      if (!this.user.avatar) {
        return '/imgs/user/admin.png'
      }
      const avatar = this.user.avatar
      if (avatar.startsWith('data:image') || avatar.startsWith('http')) {
        return avatar
      }
      if (avatar.startsWith('/')) {
        return avatar
      }
      const baseUrl = (process.env.VUE_APP_BASEURL || '').replace('/api', '') || 'http://localhost:9090'
      return `${baseUrl}/upload/${avatar}`
    },
    getUserInitial() {
      return (this.user.name || '管理员').charAt(0).toUpperCase()
    }
  },
  created() {
    this.checkLogin()
    this.initSystem()
    this.updateBreadcrumb()
    this.updateCurrentTime()
    this.checkMobile()
  },
  mounted() {
    this.loadUserSettings()
    this.setupKeyboardShortcuts()
    window.addEventListener('resize', this.handleResize)
    this.updateActiveMenu()
    this.updateTabList()
    this.setTheme(this.isDarkMode)
  },
  beforeDestroy() {
    this.clearIntervals()
    this.cleanupEventListeners()
  },
  watch: {
    $route() {
      this.updateBreadcrumb()
      this.updateActiveMenu()
      this.updateTabList()
    },
    isDarkMode(val) {
      this.setTheme(val)
    }
  },
  methods: {
    checkLogin() {
      if (!this.user.id) {
        this.$router.push('/login')
      }
    },
    initSystem() {
      window.addEventListener('xm-user-updated', this.updateUser)
      this.startTimers()
    },
    startTimers() {
      this.timeInterval = setInterval(this.updateCurrentTime, 1000)
      this.userInterval = setInterval(() => {
        this.onlineUsers = Math.floor(Math.random() * 10) + 1
      }, 30000)
    },
    clearIntervals() {
      clearInterval(this.timeInterval)
      clearInterval(this.userInterval)
    },
    cleanupEventListeners() {
      window.removeEventListener('resize', this.handleResize)
      window.removeEventListener('xm-user-updated', this.updateUser)
      document.removeEventListener('fullscreenchange', this.handleFullscreenChange)
      document.removeEventListener('keydown', this.handleKeydown)
    },
    updateUser() {
      try {
        this.user = JSON.parse(localStorage.getItem('xm-user') || '{}')
      } catch (error) {
        this.user = {}
      }
    },
    goToPerson() {
      const routeMap = {
        'ADMIN': '/manager/adminPerson',
        'MERCHANT': '/manager/merchantPerson',
        'USER': '/manager/userPerson'
      }
      this.$router.push(routeMap[this.user.role] || '/manager/userPerson')
    },
    goToHome() {
      this.$router.push('/manager/home')
    },
    logout() {
      this.$confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        localStorage.removeItem('xm-user')
        localStorage.removeItem('xm-token')
        this.$router.replace('/login')
        this.$message.success('已退出登录')
      }).catch(() => {})
    },
    loadUserSettings() {
      try {
        const settings = JSON.parse(localStorage.getItem('user-settings') || '{}')
        this.isCollapsed = settings.sidebarCollapsed || false
        this.isDarkMode = settings.darkMode || false
        this.showTabs = settings.showTabs !== false
      } catch (error) {
        this.resetUserSettings()
      }
    },
    saveUserSettings() {
      try {
        localStorage.setItem('user-settings', JSON.stringify({
          sidebarCollapsed: this.isCollapsed,
          darkMode: this.isDarkMode,
          showTabs: this.showTabs
        }))
      } catch (error) {}
    },
    resetUserSettings() {
      this.isCollapsed = false
      this.isDarkMode = false
      this.showTabs = true
    },
    toggleTheme() {
      this.isDarkMode = !this.isDarkMode
      this.saveUserSettings()
      this.setTheme(this.isDarkMode)
    },
    setTheme(isDark) {
      document.body.classList.toggle('dark-theme', isDark)
    },
    toggleFullscreen() {
      if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen().then(() => {
          this.isFullscreen = true
        }).catch(() => {})
      } else {
        document.exitFullscreen().then(() => {
          this.isFullscreen = false
        }).catch(() => {})
      }
    },
    handleFullscreenChange() {
      this.isFullscreen = !!document.fullscreenElement
    },
    checkMobile() {
      this.isMobile = window.innerWidth <= 768
    },
    handleResize() {
      this.checkMobile()
      if (this.isMobile && !this.isCollapsed) {
        this.isCollapsed = true
        this.saveUserSettings()
      }
    },
    toggleSidebar() {
      this.isCollapsed = !this.isCollapsed
      this.saveUserSettings()
    },
    updateBreadcrumb() {
      const matched = this.$route.matched.filter(route => route.meta && route.meta.name)
      this.breadcrumbList = matched.map(route => ({
        name: route.meta.name,
        path: route.path
      }))
    },
    updateActiveMenu() {
      this.activeMenu = this.$route.path
    },
    updateTabList() {
      const currentRoute = this.$route
      const existingTab = this.tabList.find(tab => tab.name === currentRoute.name)
      if (!existingTab && currentRoute.meta && currentRoute.meta.title) {
        this.tabList.push({
          name: currentRoute.name,
          title: currentRoute.meta.title,
          path: currentRoute.path,
          closable: currentRoute.name !== 'home'
        })
      }
      this.activeTab = currentRoute.name || 'home'
    },
    handleTabClick(tab) {
      const target = this.tabList.find(t => t.name === tab.name)
      if (target) {
        this.$router.push(target.path)
      }
    },
    removeTab(targetName) {
      if (targetName === 'home') {
        this.$message.warning('首页不能关闭')
        return
      }
      let activeName = this.activeTab
      if (activeName === targetName) {
        const currentIndex = this.tabList.findIndex(tab => tab.name === targetName)
        const nextTab = this.tabList[currentIndex + 1] || this.tabList[currentIndex - 1]
        if (nextTab) {
          activeName = nextTab.name
        }
      }
      this.tabList = this.tabList.filter(tab => tab.name !== targetName)
      this.activeTab = activeName
      if (activeName !== this.$route.name) {
        const target = this.tabList.find(tab => tab.name === activeName)
        if (target) {
          this.$router.push(target.path)
        }
      }
    },
    handleTabsAction(command) {
      const actions = {
        refresh: () => this.handleRefresh(),
        closeCurrent: () => this.removeTab(this.activeTab),
        closeOther: () => {
          this.tabList = this.tabList.filter(tab => 
            tab.name === this.activeTab || tab.name === 'home'
          )
        },
        closeAll: () => {
          this.tabList = this.tabList.filter(tab => tab.name === 'home')
          this.$router.push('/manager/home')
        }
      }
      if (actions[command]) {
        actions[command]()
      }
    },
    querySearch(queryString, cb) {
      const results = queryString
        ? this.searchOptions.filter(option => 
            option.value.toLowerCase().includes(queryString.toLowerCase()) ||
            option.path.toLowerCase().includes(queryString.toLowerCase())
          )
        : this.searchOptions.slice(0, 5)
      cb(results)
    },
    handleSearchSelect(item) {
      this.$router.push(item.path)
      this.searchKeyword = ''
    },
    handleRefresh() {
      this.$router.go(0)
    },
    updateCurrentTime() {
      const now = new Date()
      this.currentTime = now.toLocaleTimeString('zh-CN', { 
        hour12: false,
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    },
    handleLogoError() {
      this.logoError = true
    },
    openHelp() {
      this.helpDialogVisible = true
    },
    setupKeyboardShortcuts() {
      document.addEventListener('keydown', this.handleKeydown)
    },
    handleKeydown(e) {
      if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return
      const shortcuts = {
        'F1': () => { e.preventDefault(); this.openHelp() },
        'F5': () => { e.preventDefault(); this.handleRefresh() },
        'Escape': () => { if (this.isFullscreen) document.exitFullscreen() },
        'b': () => { if (e.ctrlKey) { e.preventDefault(); this.toggleSidebar() } },
        'B': () => { if (e.ctrlKey) { e.preventDefault(); this.toggleSidebar() } },
        'f': () => { if (e.ctrlKey) { e.preventDefault(); document.querySelector('.search-box input')?.focus() } },
        'F': () => { if (e.ctrlKey) { e.preventDefault(); document.querySelector('.search-box input')?.focus() } }
      }
      if (shortcuts[e.key]) {
        shortcuts[e.key]()
      }
    }
  }
}
</script>

<style scoped>
.manager-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
  overflow: hidden;
}

.manager-header {
  height: 56px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 12px;
  z-index: 100;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.logo-wrapper {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  background: linear-gradient(135deg, #1890ff, #40a9ff);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 16px;
  overflow: hidden;
  flex-shrink: 0;
}

.logo {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.logo-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.system-name {
  font-size: 15px;
  font-weight: 600;
  color: #262626;
  white-space: nowrap;
}

.collapse-btn {
  width: 28px;
  height: 28px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #666;
  font-size: 18px;
}

.collapse-btn:hover {
  background: #f5f5f5;
  color: #1890ff;
}

.header-center {
  flex: 1;
  min-width: 0;
}

.breadcrumb {
  font-size: 14px;
}

.breadcrumb .el-breadcrumb__item:last-child .el-breadcrumb__inner {
  color: #1890ff;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.search-box {
  width: 200px;
}

.search-box .el-autocomplete {
  width: 100%;
}

.search-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-item i {
  color: #999;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-icon {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #666;
  font-size: 16px;
}

.action-icon:hover {
  background: #f5f5f5;
  color: #1890ff;
}

.user-dropdown {
  margin-left: 4px;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
}

.user-trigger:hover {
  background: #f5f5f5;
}

.user-name {
  font-size: 14px;
  color: #262626;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.manager-main {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.sidebar {
  width: 220px;
  background: #001529;
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar.collapsed {
  width: 64px;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  overflow-y: auto;
}

.sidebar-menu .el-menu-item,
.sidebar-menu .el-submenu__title {
  height: 44px;
  line-height: 44px;
}

.sidebar-menu .el-menu-item {
  padding-left: 20px !important;
}

.sidebar-menu .el-submenu__title {
  padding-left: 16px !important;
}

.sidebar-menu .el-menu-item:hover,
.sidebar-menu .el-submenu__title:hover {
  background: #1890ff;
}

.sidebar-menu .el-menu-item.is-active {
  background: #1890ff;
  border-right: 3px solid #40a9ff;
}

.sidebar-menu .el-submenu .el-menu-item {
  padding-left: 44px !important;
  height: 40px;
  line-height: 40px;
  font-size: 13px;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
  overflow: hidden;
}

.tabs-bar {
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  padding: 0 16px;
  flex-shrink: 0;
}

.tabs-bar .el-tabs {
  flex: 1;
}

.tabs-bar .el-tabs__header {
  margin: 0;
  border-bottom: none;
}

.tabs-bar .el-tabs__nav {
  border: none;
}

.tabs-bar .el-tabs__item {
  height: 36px;
  line-height: 34px;
  padding: 0 12px;
  border: 1px solid #e8e8e8;
  border-bottom: none;
  margin-right: 4px;
  background: #fafafa;
  color: #666;
  font-size: 13px;
}

.tabs-bar .el-tabs__item:hover {
  color: #1890ff;
}

.tabs-bar .el-tabs__item.is-active {
  background: #fff;
  color: #1890ff;
  border-color: #1890ff;
}

.tabs-more {
  margin-left: 8px;
  cursor: pointer;
  color: #999;
  font-size: 16px;
  padding: 4px;
}

.tabs-more:hover {
  color: #1890ff;
}

.content-area {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.2s ease;
}

.fade-transform-enter {
  opacity: 0;
  transform: translateX(10px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}

.help-content h3 {
  font-size: 15px;
  color: #262626;
  margin: 0 0 12px 0;
}

.shortcuts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.shortcut-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 4px;
}

.shortcut-item kbd {
  background: #fff;
  border: 1px solid #d1d5db;
  border-radius: 3px;
  padding: 2px 6px;
  font-size: 12px;
  color: #374151;
}

.shortcut-item span {
  font-size: 13px;
  color: #6b7280;
}

@media (max-width: 1200px) {
  .search-box {
    width: 160px;
  }
  .user-name {
    display: none;
  }
}

@media (max-width: 992px) {
  .search-box {
    display: none;
  }
}

@media (max-width: 768px) {
  .manager-header {
    padding: 0 12px;
    height: 50px;
  }
  .system-name {
    display: none;
  }
  .sidebar {
    position: fixed;
    top: 50px;
    left: 0;
    height: calc(100vh - 50px);
    z-index: 99;
    transform: translateX(-100%);
  }
  .sidebar:not(.collapsed) {
    transform: translateX(0);
  }
  .content-area {
    padding: 12px;
  }
}

@media (max-width: 480px) {
  .manager-header {
    height: 48px;
    padding: 0 8px;
    gap: 8px;
  }
  .logo-wrapper {
    width: 30px;
    height: 30px;
    font-size: 14px;
  }
  .action-icon {
    width: 28px;
    height: 28px;
    font-size: 14px;
  }
  .content-area {
    padding: 8px;
  }
}

.dark-theme .manager-container {
  background: #141414;
}

.dark-theme .manager-header {
  background: #1f1f1f;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
}

.dark-theme .system-name {
  color: #e6e6e6;
}

.dark-theme .collapse-btn:hover,
.dark-theme .action-icon:hover,
.dark-theme .user-trigger:hover {
  background: #262626;
}

.dark-theme .breadcrumb .el-breadcrumb__inner {
  color: #e6e6e6;
}

.dark-theme .breadcrumb .el-breadcrumb__item:last-child .el-breadcrumb__inner {
  color: #40a9ff;
}

.dark-theme .user-name {
  color: #e6e6e6;
}

.dark-theme .tabs-bar {
  background: #1f1f1f;
  border-color: #262626;
}

.dark-theme .tabs-bar .el-tabs__item {
  background: #262626;
  border-color: #333;
  color: #999;
}

.dark-theme .tabs-bar .el-tabs__item.is-active {
  background: #1f1f1f;
  color: #40a9ff;
  border-color: #40a9ff;
}

.dark-theme .content-area {
  background: #141414;
}

.dark-theme .help-content h3 {
  color: #e6e6e6;
}

.dark-theme .shortcut-item {
  background: #262626;
}

.dark-theme .shortcut-item kbd {
  background: #1f1f1f;
  border-color: #333;
  color: #e6e6e6;
}

.dark-theme .shortcut-item span {
  color: #999;
}
</style>
