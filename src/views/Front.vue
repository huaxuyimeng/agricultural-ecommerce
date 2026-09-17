/**
 * 前台布局组件
 * 文件路径: src/views/Front.vue
 * 功能描述: 前台页面的公共布局，包含公告栏、头部导航、主体内容和回到顶部按钮
 * 关联文件:
 * - src/config/auth.js: 认证配置
 * - src/utils/cart.js: 购物车工具
 * - src/views/front/Home.vue: 首页
 * - src/views/front/Products.vue: 商品列表
 * - src/views/front/ProductDetail.vue: 商品详情
 * - src/views/front/News.vue: 新闻列表
 * - src/views/front/NewsDetail.vue: 新闻详情
 * - src/views/front/Cart.vue: 购物车
 * - src/views/front/Favorites.vue: 收藏
 * - src/views/front/MyOrders.vue: 我的订单
 * - src/views/front/Person.vue: 个人中心
 */
<template>
  <div>
    <!-- 公告栏 -->
    <div class="front-notice" @mouseenter="pauseNotice" @mouseleave="resumeNotice">
      <div class="notice-content">
        <i class="el-icon-bell notice-icon"></i>
        <div class="notice-text">
          <div class="notice-scroll" :style="{ transform: `translateY(${noticeOffset}px)` }">
            <div v-for="(item, index) in notice" :key="index" class="notice-item">
              公告：{{ item.content }}
            </div>
          </div>
        </div>
        <div class="notice-actions">
          <span class="notice-index">{{ currentNoticeIndex + 1 }}/{{ notice.length }}</span>
          <i class="el-icon-arrow-up notice-arrow" @click="prevNotice"></i>
          <i class="el-icon-arrow-down notice-arrow" @click="nextNotice"></i>
        </div>
      </div>
    </div>

    <!-- 头部导航 -->
    <div class="header-wrapper">
      <div class="front-header">
        <!-- Logo区域 -->
        <router-link to="/front/home" class="logo-link">
          <div class="front-header-left">
            <div class="logo-container">
              <img src="/imgs/logo1.png" alt="网站Logo" class="logo-img">
              <div class="logo-shine"></div>
            </div>
            <div class="title-container">
              <div class="title-main">农产品商城</div>
              <div class="title-sub">绿色 · 健康 · 新鲜</div>
            </div>
          </div>
        </router-link>

        <!-- 中间区域：菜单 + 搜索 -->
        <div class="front-header-center">
          <!-- 菜单栏 -->
          <div class="menu-wrapper">
            <div 
              v-for="item in menus" 
              :key="item.text" 
              class="menu-item"
              :class="{ 
                'menu-item-active': isMenuActive(item.path),
                'menu-item-home': item.path === '/front/home' 
              }"
              @click="changePath(item.path)"
              @mouseenter="onMenuHover(item)"
              @mouseleave="onMenuLeave"
            >
              <span class="menu-icon" v-if="item.icon">
                <i :class="item.icon"></i>
              </span>
              <span class="menu-text">{{ item.text }}</span>
              <div class="menu-hover-bg"></div>
              <div class="menu-active-line" v-if="isMenuActive(item.path)"></div>
            </div>
          </div>

          <!-- 搜索框 -->
          <div class="search-wrapper">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索商品名称..."
              class="search-input"
              clearable
              @keyup.enter="handleSearch"
              @focus="onSearchFocus"
              @blur="onSearchBlur"
              size="medium"
            >
              <template #prefix>
                <i class="el-icon-search search-icon"></i>
              </template>
              <template #suffix>
                <i v-if="searchKeyword" class="el-icon-close search-clear" @click="clearSearch"></i>
              </template>
            </el-input>
            <el-dropdown 
              placement="bottom" 
              trigger="click" 
              class="search-dropdown"
              v-if="searchHistory.length > 0 && isSearchFocused"
            >
              <el-button
                type="warning"
                @click="handleSearch"
                class="search-btn"
                size="medium"
              >
                搜索
              </el-button>
              <template #dropdown>
                <el-dropdown-menu class="search-history">
                  <el-dropdown-item 
                    v-for="(item, index) in searchHistory" 
                    :key="index"
                    @click="selectHistory(item)"
                    class="history-item"
                  >
                    <i class="el-icon-time history-icon"></i>
                    <span class="history-text">{{ item }}</span>
                    <i class="el-icon-close history-delete" @click.stop="deleteHistory(index)"></i>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button
              v-else
              type="warning"
              @click="handleSearch"
              class="search-btn"
              size="medium"
            >
              搜索
            </el-button>
          </div>
        </div>

        <!-- 右侧用户区域 -->
        <div class="front-header-right">
          <div v-if="!user.username" class="auth-wrapper">
            <el-button 
              @click="handleLogin" 
              class="login-btn"
              size="medium"
              :loading="loginLoading"
            >
              <i class="el-icon-user login-icon"></i>
              <span>登录</span>
            </el-button>
            <el-button 
              @click="handleRegister" 
              type="success"
              class="register-btn"
              size="medium"
            >
              <i class="el-icon-edit register-icon"></i>
              <span>注册</span>
            </el-button>
          </div>
          <div v-else class="user-wrapper">
            <div class="user-notification" @click="showNotifications">
              <el-badge 
                :value="unreadCount" 
                :max="99" 
                :hidden="unreadCount === 0"
                class="notification-badge"
              >
                <i class="el-icon-bell notification-icon"></i>
              </el-badge>
              <div class="notification-tip" v-if="showNotificationTip">有{{ unreadCount }}条新消息</div>
            </div>
            
            <el-dropdown 
              placement="bottom-end" 
              trigger="click" 
              :show-timeout="100" 
              :hide-timeout="100"
              @command="handleUserCommand"
            >
              <div class="user-dropdown-trigger">
                <div class="user-avatar">
                  <img :src="userAvatar" alt="用户头像">
                  <div class="user-status" :class="{ 'online': user.isOnline }"></div>
                </div>
                <div class="user-info">
                  <div class="user-name">{{ user.name || user.username }}</div>
                  <div class="user-level" v-if="user.level">VIP{{ user.level }}</div>
                </div>
                <i class="el-icon-arrow-down user-arrow" :class="{ 'rotate': dropdownVisible }"></i>
              </div>
              
              <template #dropdown>
                <el-dropdown-menu class="user-dropdown-menu">
                  <el-dropdown-item class="user-profile">
                    <div class="profile-header">
                      <div class="profile-avatar">
                        <img :src="userAvatar" alt="头像">
                      </div>
                      <div class="profile-info">
                        <div class="profile-name">{{ user.name || user.username }}</div>
                        <div class="profile-email" v-if="user.email">{{ user.email }}</div>
                      </div>
                    </div>
                  </el-dropdown-item>
                  
                  <el-dropdown-item divided>
                    <router-link to="/front/person" class="dropdown-item">
                      <i class="el-icon-user dropdown-icon"></i>
                      <span>个人中心</span>
                    </router-link>
                  </el-dropdown-item>
                  
                  <el-dropdown-item>
                    <router-link to="/front/my-orders" class="dropdown-item">
                      <i class="el-icon-s-order dropdown-icon"></i>
                      <span>我的订单</span>
                      <el-badge :value="orderCount" :max="99" class="dropdown-badge" v-if="orderCount > 0"></el-badge>
                    </router-link>
                  </el-dropdown-item>
                  
                  <el-dropdown-item>
                    <router-link to="/front/cart" class="dropdown-item">
                      <i class="el-icon-shopping-cart-2 dropdown-icon"></i>
                      <span>购物车</span>
                      <el-badge :value="cartCount" :max="99" class="dropdown-badge" v-if="cartCount > 0"></el-badge>
                    </router-link>
                  </el-dropdown-item>
                  
                  <el-dropdown-item>
                    <router-link to="/front/my-coupons" class="dropdown-item">
                      <i class="el-icon-present dropdown-icon"></i>
                      <span>我的优惠券</span>
                    </router-link>
                  </el-dropdown-item>
                  
                  <!-- 账户设置已合并到个人中心，移除此导航入口 -->
                  
                  <el-dropdown-item divided command="logout">
                    <div class="dropdown-item logout-item">
                      <i class="el-icon-switch-button dropdown-icon"></i>
                      <span>退出登录</span>
                    </div>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="main-body">
      <router-view ref="child" @update:user="updateUser" />
    </div>

    <!-- 回到顶部按钮 -->
    <transition name="fade">
      <div 
        v-show="showBackTop" 
        class="back-top" 
        @click="scrollToTop"
        @mouseenter="onBackTopHover"
        @mouseleave="onBackTopLeave"
      >
        <i class="el-icon-top back-top-icon"></i>
        <div class="back-top-tooltip" v-show="showBackTopTooltip">回到顶部</div>
      </div>
    </transition>
  </div>
</template>

<script>
import { getCartStats } from '@/data/cart'
import { FRONT_USER_MENUS, FRONT_MERCHANT_MENUS } from '@/config/auth'
import { getNewsPage } from '@/api'

export default {
  name: "FrontLayout",

  data() {
    return {
      searchKeyword: this.$route.query.name || '',
      top: '',
      notice: [],
      currentNoticeIndex: 0,
      noticeOffset: 0,
      noticeTimer: null,
      isNoticePaused: false,
      user: JSON.parse((sessionStorage.getItem("xm-user") || localStorage.getItem("xm-user")) || '{}'),
      searchHistory: JSON.parse(localStorage.getItem('search-history') || '[]'),
      isSearchFocused: false,
      unreadCount: 3,
      orderCount: 2,
      cartCount: 5,
      showNotificationTip: false,
      dropdownVisible: false,
      showBackTop: false,
      showBackTopTooltip: false,
      loginLoading: false,
      menus: FRONT_USER_MENUS.slice(),
      hoverMenu: null
    }
  },

  computed: {
    userAvatar() {
      const u = this.user && this.user.avatar
      if (!u) {
        return '/imgs/user/zhangsan.png'
      }

      if (typeof u === 'string' && u.startsWith('data:')) {
        return u
      }

      if (typeof u === 'string' && u.startsWith('http')) {
        return u
      }

      if (typeof u === 'string' && (u.startsWith('imgs') || u.startsWith('/imgs'))) {
        return u.startsWith('/') ? u : '/' + u
      }

      if (typeof u === 'string' && (u.startsWith('upload') || u.startsWith('upload/'))) {
        const baseUrl = process.env.VUE_APP_BASEURL?.replace('/api', '') || 'http://localhost:9090'
        return `${baseUrl}/upload/${u.replace('upload/', '')}`
      }

      return '/imgs/user/zhangsan.png'
    }
  },

  mounted() {
    this.loadNotice()
    this.setupScrollListener()
    this.rebuildMenusByRole()
    this.loadUserData()
    // 监听用户信息更新（来自个人中心或其他组件）以同步导航栏头像
    window.addEventListener('xm-user-updated', this.updateUser)
    // 监听购物车变更以同步购物车数量
    window.addEventListener('xm-cart-changed', this.loadCartCount)
  },

  beforeDestroy() {
    if (this.noticeTimer) {
      clearInterval(this.noticeTimer)
    }
    window.removeEventListener('scroll', this.handleScroll)
    window.removeEventListener('xm-user-updated', this.updateUser)
    window.removeEventListener('xm-cart-changed', this.loadCartCount)
  },

  beforeUnmount() {
    if (this.noticeTimer) {
      clearInterval(this.noticeTimer)
    }
    window.removeEventListener('scroll', this.handleScroll)
    window.removeEventListener('xm-user-updated', this.updateUser)
    window.removeEventListener('xm-cart-changed', this.loadCartCount)
  },

  methods: {
    isMenuActive(path) {
      if (path.startsWith('#')) {
        return window.location.hash === path
      }
      return this.$route.path === path
    },

    onMenuHover(menu) {
      this.hoverMenu = menu
    },

    onMenuLeave() {
      this.hoverMenu = null
    },

    handleSearch() {
      if (this.searchKeyword.trim()) {
        this.addToSearchHistory(this.searchKeyword.trim())
        window.open('/front/products?name=' + encodeURIComponent(this.searchKeyword.trim()))
      } else {
        this.$message({
          message: '请输入搜索关键词',
          type: 'warning',
          duration: 1500,
          offset: 80
        })
      }
    },

    addToSearchHistory(keyword) {
      if (!keyword) return
      
      const index = this.searchHistory.indexOf(keyword)
      if (index > -1) {
        this.searchHistory.splice(index, 1)
      }
      
      this.searchHistory.unshift(keyword)
      
      if (this.searchHistory.length > 5) {
        this.searchHistory = this.searchHistory.slice(0, 5)
      }
      
      localStorage.setItem('search-history', JSON.stringify(this.searchHistory))
    },

    selectHistory(keyword) {
      this.searchKeyword = keyword
      this.handleSearch()
    },

    deleteHistory(index) {
      this.searchHistory.splice(index, 1)
      localStorage.setItem('search-history', JSON.stringify(this.searchHistory))
    },

    clearSearch() {
      this.searchKeyword = ''
    },

    onSearchFocus() {
      this.isSearchFocused = true
    },

    onSearchBlur() {
      setTimeout(() => {
        this.isSearchFocused = false
      }, 200)
    },

    handleLogin() {
      this.loginLoading = true
      setTimeout(() => {
        this.$router.push('/login')
        this.loginLoading = false
      }, 300)
    },

    handleRegister() {
      this.$router.push('/register')
    },

    changePath(path) {
      if (path.startsWith('#')) {
        this.scrollToAnchor(path)
      } else {
        this.$router.push(path)
      }
    },

    scrollToAnchor(anchor) {
      if (this.$route.path !== '/front/home') {
        this.$router.push({ path: '/front/home', hash: anchor.substring(1) })
          .then(() => {
            setTimeout(() => this.scrollToElement(anchor), 100)
          })
      } else {
        this.scrollToElement(anchor)
      }
    },

    scrollToElement(selector) {
      setTimeout(() => {
        const el = document.querySelector(selector)
        if (el) {
          const headerHeight = document.querySelector('.header-wrapper')?.offsetHeight || 0
          const top = el.getBoundingClientRect().top + window.pageYOffset - headerHeight
          
          window.scrollTo({
            top: top,
            behavior: 'smooth'
          })
        }
      }, 100)
    },

    loadNotice() {
      getNewsPage({ pageNum: 1, pageSize: 10 })
        .then(res => {
          const list = res?.data?.list || res?.data?.records || []
          if (list.length) {
            this.notice = list.map(item => ({
              id: item.id,
              content: item.title,
              category: item.category
            }))
            this.startNoticeScroll()
          } else {
            this.loadFallbackNotice()
          }
        })
        .catch(() => {
          this.loadFallbackNotice()
        })
    },

    loadFallbackNotice() {
      this.notice = [
        { id: 1, content: '欢迎来到农产品商城，新鲜果蔬每日送达！' },
        { id: 2, content: '新用户注册立享88元优惠券！' },
        { id: 3, content: '全场满199元包邮，立即选购！' }
      ]
      this.startNoticeScroll()
    },

    startNoticeScroll() {
      if (this.noticeTimer) {
        clearInterval(this.noticeTimer)
      }
      
      this.noticeTimer = setInterval(() => {
        if (!this.isNoticePaused && this.notice.length > 1) {
          this.nextNotice()
        }
      }, 3000)
    },

    nextNotice() {
      this.currentNoticeIndex = (this.currentNoticeIndex + 1) % this.notice.length
      this.noticeOffset = -this.currentNoticeIndex * 24
    },

    prevNotice() {
      this.currentNoticeIndex = (this.currentNoticeIndex - 1 + this.notice.length) % this.notice.length
      this.noticeOffset = -this.currentNoticeIndex * 24
    },

    pauseNotice() {
      this.isNoticePaused = true
    },

    resumeNotice() {
      this.isNoticePaused = false
    },

    loadUserData() {
      if (this.user.username) {
        this.loadNotifications()
        this.loadOrderCount()
        this.loadCartCount()
      }
    },

    loadNotifications() {
      setTimeout(() => {
        this.showNotificationTip = true
        setTimeout(() => {
          this.showNotificationTip = false
        }, 3000)
      }, 1000)
    },

    loadOrderCount() {
      this.orderCount = Math.floor(Math.random() * 5)
    },

    async loadCartCount() {
      try {
        const stats = await getCartStats()
        this.cartCount = stats.count || 0
      } catch (e) {
        this.cartCount = 0
      }
    },

    showNotifications() {
      this.$message({
        message: '通知功能开发中...',
        type: 'info',
        duration: 1500
      })
    },

    setupScrollListener() {
      window.addEventListener('scroll', this.handleScroll)
    },

    handleScroll() {
      this.showBackTop = window.scrollY > 300
    },

    scrollToTop() {
      window.scrollTo({
        top: 0,
        behavior: 'smooth'
      })
    },

    onBackTopHover() {
      this.showBackTopTooltip = true
    },

    onBackTopLeave() {
      this.showBackTopTooltip = false
    },

    rebuildMenusByRole() {
      const role = this.user && this.user.role
      this.menus = role === 'MERCHANT'
        ? FRONT_MERCHANT_MENUS.slice()
        : FRONT_USER_MENUS.slice()
    },

    updateUser(event) {
      // 支持带 detail 的自定义事件（来自其他组件的更新）
      if (event && event.detail && event.detail.user) {
        this.user = event.detail.user
      } else {
        // 从存储重新加载
        const userStr = sessionStorage.getItem('xm-user') || localStorage.getItem('xm-user')
        this.user = JSON.parse(userStr || '{}')
      }
      this.rebuildMenusByRole()
      this.loadUserData()
    },

    handleUserCommand(command) {
      if (command === 'logout') {
        this.handleLogout()
      }
    },

    handleLogout() {
      this.$confirm('确定要退出登录吗？', '退出确认', {
        confirmButtonText: '确定退出',
        cancelButtonText: '取消',
        type: 'warning',
        customClass: 'logout-confirm',
        confirmButtonClass: 'el-button--warning',
        cancelButtonClass: 'el-button--default',
        center: true
      }).then(() => {
        // 清理登录态及前台会话数据
        sessionStorage.removeItem('xm-user')
        sessionStorage.removeItem('xm-token')
        localStorage.removeItem('xm-user')
        localStorage.removeItem('xm-token')
        localStorage.removeItem('search-history')

        // 重置页面内状态，避免退出后仍显示旧数据
        this.user = {}
        this.searchKeyword = ''
        this.unreadCount = 0
        this.orderCount = 0
        this.cartCount = 0

        // 广播全局事件，通知其他组件刷新用户态
        try {
          window.dispatchEvent(new Event('xm-user-updated'))
          window.dispatchEvent(new Event('xm-cart-changed'))
        } catch (e) {
          const userEvt = document.createEvent('Event')
          userEvt.initEvent('xm-user-updated', true, true)
          window.dispatchEvent(userEvt)
          const cartEvt = document.createEvent('Event')
          cartEvt.initEvent('xm-cart-changed', true, true)
          window.dispatchEvent(cartEvt)
        }

        this.$message.success('退出成功')
        this.$router.replace('/login').catch(err => {
          // 忽略导航取消错误（用户快速操作导致）
          if (err.name !== 'NavigationDuplicated' && !err.message.includes('Navigation cancelled')) {
            console.error('路由跳转失败:', err)
          }
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
/* 公告栏 */
.front-notice {
  background: linear-gradient(135deg, #fff9e6 0%, #ffe7b3 100%);
  color: #d48806;
  height: 40px;
  overflow: hidden;
  position: relative;
  border-bottom: 1px solid #ffd591;
  cursor: pointer;
  transition: all 0.3s ease;
}

.front-notice:hover {
  background: linear-gradient(135deg, #ffe7b3 0%, #ffd591 100%);
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1);
}

.notice-content {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 0 20px;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
}

.notice-icon {
  font-size: 16px;
  margin-right: 12px;
  color: #fa8c16;
  animation: bell-ring 2s infinite;
}

@keyframes bell-ring {
  0%, 100% { transform: rotate(0); }
  5%, 15% { transform: rotate(20deg); }
  10%, 20% { transform: rotate(-20deg); }
  25% { transform: rotate(0); }
}

.notice-text {
  flex: 1;
  height: 24px;
  overflow: hidden;
  position: relative;
  text-align: center;
  font-size: 14px;
  font-weight: 500;
}

.notice-scroll {
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  position: absolute;
  width: 100%;
}

.notice-item {
  height: 24px;
  line-height: 24px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding: 0 10px;
  font-weight: 500;
}

.notice-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 20px;
  color: #fa8c16;
}

.notice-index {
  font-size: 12px;
  opacity: 0.8;
  min-width: 30px;
  text-align: center;
}

.notice-arrow {
  cursor: pointer;
  font-size: 12px;
  padding: 2px;
  border-radius: 2px;
  transition: all 0.2s;
  user-select: none;
}

.notice-arrow:hover {
  background: #ffd591;
  color: #874d00;
}

/* 头部包装 */
.header-wrapper {
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.08);
  background: linear-gradient(135deg, #ffffff 0%, #fffaf0 100%);
  position: sticky;
  top: 0;
  z-index: 1000;
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 152, 0, 0.1);
  padding: 12px 0;
}

/* 头部主体 */
.front-header {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 30px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 30px;
  position: relative;
}

/* Logo区域 */
.logo-link {
  text-decoration: none;
  color: inherit;
  transition: transform 0.3s ease;
  display: block;
  flex-shrink: 0;
}

.logo-link:hover {
  transform: translateY(-1px);
}

.front-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
}

.logo-container {
  position: relative;
  width: 44px;
  height: 44px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.2);
  transition: all 0.3s ease;
}

.logo-container:hover {
  transform: rotate(5deg) scale(1.05);
  box-shadow: 0 6px 20px rgba(255, 152, 0, 0.3);
}

.logo-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.logo-container:hover .logo-img {
  transform: scale(1.1);
}

.logo-shine {
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.4),
    transparent
  );
  transition: left 0.6s ease;
}

.logo-container:hover .logo-shine {
  left: 100%;
}

.title-container {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.title-main {
  font-size: 20px;
  font-weight: 800;
  background: linear-gradient(135deg, #ff9800 0%, #ff5722 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 4px rgba(255, 152, 0, 0.1);
  white-space: nowrap;
  line-height: 1.2;
}

.title-sub {
  font-size: 11px;
  color: #ff9800;
  opacity: 0.6;
  font-weight: 500;
  letter-spacing: 0.5px;
  white-space: nowrap;
  line-height: 1.2;
}

/* 中间区域 */
.front-header-center {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
  position: relative;
}

/* 菜单容器 */
.menu-wrapper {
  display: flex;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
  overflow-x: auto;
  overflow-y: hidden;
  padding: 4px 0;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.menu-wrapper::-webkit-scrollbar {
  display: none;
}

/* 菜单项 */
.menu-item {
  padding: 6px 10px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
  position: relative;
  display: flex;
  align-items: center;
  gap: 4px;
  z-index: 1;
  font-weight: 500;
  background: transparent;
  flex-shrink: 0;
  box-sizing: border-box;
  text-align: center;
  justify-content: center;
}

.menu-hover-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 193, 7, 0.1), rgba(255, 152, 0, 0.1));
  border-radius: 8px;
  opacity: 0;
  transform: scale(0.8);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: -1;
}

.menu-item:hover .menu-hover-bg {
  opacity: 1;
  transform: scale(1);
}

.menu-item:hover {
  color: #ff9800;
  transform: translateY(-1px);
}

.menu-item-home {
  background: linear-gradient(135deg, #fff9e6, #ffe7b3);
  border: 1px solid rgba(255, 193, 7, 0.2);
}

.menu-icon {
  font-size: 16px;
  opacity: 0.8;
  transition: all 0.3s ease;
  flex-shrink: 0; /* 防止图标被压缩 */
}

.menu-item:hover .menu-icon {
  transform: scale(1.1);
  opacity: 1;
}

.menu-text {
  position: relative;
  z-index: 1;
  transition: all 0.3s ease;
  overflow: visible; /* 确保文字不会被截断 */
  text-overflow: clip; /* 不显示省略号 */
  white-space: nowrap; /* 不换行 */
  flex-shrink: 0; /* 防止文字被压缩 */
  max-width: 100%; /* 最大宽度100% */
  display: inline-block; /* 确保宽度自适应内容 */
}

.menu-item:hover .menu-text {
  transform: translateX(2px);
}

.menu-active-line {
  position: absolute;
  bottom: 4px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  background: linear-gradient(90deg, #ff9800, #ff5722);
  border-radius: 2px;
  box-shadow: 0 2px 4px rgba(255, 152, 0, 0.3);
  animation: line-pulse 2s infinite;
}

@keyframes line-pulse {
  0%, 100% { width: 20px; }
  50% { width: 30px; }
}

.menu-item-active {
  color: #ff5722;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(255, 152, 0, 0.1);
}

.menu-item-active .menu-icon {
  color: #ff5722;
  opacity: 1;
  animation: icon-bounce 1s infinite;
}

@keyframes icon-bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
}

/* 搜索容器 */
.search-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  max-width: 500px;
  min-width: 300px;
  position: relative;
}

.search-input {
  flex: 1;
  min-width: 0;
  transition: all 0.3s ease;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 25px;
  border: 2px solid #e0e0e0;
  transition: all 0.3s;
  height: 42px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  padding: 0 16px;
}

.search-input :deep(.el-input__wrapper):hover {
  border-color: #ff9800;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.15);
}

.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: #ff9800;
  box-shadow: 0 4px 16px rgba(255, 152, 0, 0.2), 0 0 0 3px rgba(255, 152, 0, 0.1);
  background: #fff;
}

.search-input :deep(.el-input__inner) {
  height: 42px;
  line-height: 42px;
  font-size: 14px;
  padding: 0;
  color: #333;
}

.search-input :deep(.el-input__inner)::placeholder {
  color: #999;
  font-size: 14px;
}

.search-input :deep(.el-input__prefix) {
  display: flex;
  align-items: center;
  color: #ff9800;
  font-size: 16px;
  margin-right: 8px;
}

.search-input :deep(.el-input__suffix) {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-left: 8px;
}

.search-icon {
  animation: search-rotate 3s infinite linear;
  transform-origin: center;
}

@keyframes search-rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.search-input :deep(.el-input__suffix) {
  right: 16px;
  display: flex;
  align-items: center;
  cursor: pointer;
}

.search-clear {
  color: #999;
  font-size: 12px;
  transition: all 0.2s;
  padding: 4px;
  border-radius: 50%;
}

.search-clear:hover {
  color: #ff5722;
  background: rgba(255, 87, 34, 0.1);
  transform: rotate(90deg);
}

.search-dropdown {
  position: relative;
}

.search-btn {
  border-radius: 25px;
  padding: 0 28px;
  height: 42px;
  background: linear-gradient(135deg, #ff9800, #ff5722);
  border: none;
  font-weight: 600;
  letter-spacing: 1px;
  transition: all 0.3s;
  white-space: nowrap;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.3);
  position: relative;
  overflow: hidden;
  z-index: 1;
  flex-shrink: 0;
}

.search-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s ease;
  z-index: -1;
}

.search-btn:hover::before {
  left: 100%;
}

.search-btn:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 6px 20px rgba(255, 152, 0, 0.4);
  background: linear-gradient(135deg, #ff5722, #ff9800);
}

.search-history {
  min-width: 240px;
  border-radius: 12px;
  border: 1px solid rgba(255, 152, 0, 0.1);
  box-shadow: 0 8px 24px rgba(255, 152, 0, 0.1);
  margin-top: 8px;
  padding: 8px 0;
  background: linear-gradient(135deg, #fff, #fffaf0);
}

.history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  margin: 2px 0;
  border-radius: 8px;
  transition: all 0.2s;
  cursor: pointer;
  color: #666;
}

.history-item:hover {
  background: linear-gradient(135deg, #fff8e1, #ffe7b3);
  color: #ff9800;
  transform: translateX(4px);
}

.history-icon {
  font-size: 12px;
  opacity: 0.6;
  margin-right: 8px;
}

.history-text {
  flex: 1;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-delete {
  font-size: 10px;
  opacity: 0.4;
  padding: 4px;
  border-radius: 50%;
  transition: all 0.2s;
}

.history-delete:hover {
  opacity: 1;
  background: rgba(255, 87, 34, 0.1);
  color: #ff5722;
}

/* 右侧用户区域 */
.front-header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 16px;
  background: linear-gradient(135deg, rgba(255, 248, 225, 0.6), rgba(255, 231, 179, 0.3));
  border-radius: 25px;
  border: 1px solid rgba(255, 152, 0, 0.15);
  flex-shrink: 0;
}

/* 通知图标 */
.user-notification {
  position: relative;
  cursor: pointer;
  padding: 8px;
  border-radius: 12px;
  transition: all 0.3s;
}

.user-notification:hover {
  background: rgba(255, 152, 0, 0.1);
  transform: translateY(-1px);
}

.notification-badge :deep(.el-badge__content) {
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(255, 87, 34, 0.3);
  animation: badge-pulse 2s infinite;
}

@keyframes badge-pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.notification-icon {
  font-size: 20px;
  color: #666;
  transition: all 0.3s;
}

.user-notification:hover .notification-icon {
  color: #ff9800;
  transform: rotate(20deg);
}

.notification-tip {
  position: absolute;
  top: 100%;
  right: 0;
  background: #ff5722;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
  margin-top: 4px;
  animation: slide-down 0.3s;
  box-shadow: 0 2px 8px rgba(255, 87, 34, 0.3);
  z-index: 1001;
}

.notification-tip::before {
  content: '';
  position: absolute;
  top: -4px;
  right: 20px;
  width: 0;
  height: 0;
  border-left: 4px solid transparent;
  border-right: 4px solid transparent;
  border-bottom: 4px solid #ff5722;
}

@keyframes slide-down {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 用户信息 */
.user-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px 6px 8px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
  background: linear-gradient(135deg, rgba(255, 248, 225, 0.5), rgba(255, 231, 179, 0.2));
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.user-dropdown-trigger::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 152, 0, 0.1), rgba(255, 193, 7, 0.1));
  border-radius: 25px;
  opacity: 0;
  transition: opacity 0.3s;
  z-index: -1;
}

.user-dropdown-trigger:hover::before {
  opacity: 1;
}

.user-dropdown-trigger:hover {
  border-color: rgba(255, 152, 0, 0.3);
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.2);
  transform: translateY(-1px);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
  position: relative;
  transition: all 0.3s;
}

/* 移除头像旋转效果 */
.user-dropdown-trigger:hover .user-avatar {
  transform: scale(1.1); /* 只保留缩放，移除旋转 */
  border-color: rgba(255, 152, 0, 0.3);
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.3);
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s;
}

.user-dropdown-trigger:hover .user-avatar img {
  transform: scale(1.2);
}

.user-status {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 2px solid #fff;
  background: #ddd;
  transition: background 0.3s;
}

.user-status.online {
  background: #52c41a;
  animation: status-pulse 2s infinite;
}

@keyframes status-pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(82, 196, 26, 0.4); }
  50% { box-shadow: 0 0 0 4px rgba(82, 196, 26, 0); }
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100px; /* 限制最大宽度，避免太长 */
  transition: all 0.3s;
}

.user-dropdown-trigger:hover .user-name {
  color: #ff9800;
  text-shadow: 0 2px 4px rgba(255, 152, 0, 0.1);
}

.user-level {
  font-size: 10px;
  background: linear-gradient(135deg, #ff9800, #ff5722);
  color: white;
  padding: 1px 6px;
  border-radius: 10px;
  text-align: center;
  font-weight: 600;
  letter-spacing: 0.5px;
  white-space: nowrap;
  box-shadow: 0 2px 4px rgba(255, 152, 0, 0.3);
  transition: all 0.3s;
}

.user-dropdown-trigger:hover .user-level {
  transform: scale(1.1);
  box-shadow: 0 4px 8px rgba(255, 152, 0, 0.4);
}

.user-arrow {
  font-size: 12px;
  color: #999;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin-left: 4px;
}

.user-arrow.rotate {
  transform: rotate(180deg);
  color: #ff9800;
}

/* 下拉菜单 */
.user-dropdown-menu {
  min-width: 240px;
  border-radius: 16px;
  border: 1px solid rgba(255, 152, 0, 0.1);
  box-shadow: 0 12px 32px rgba(255, 152, 0, 0.2);
  margin-top: 8px;
  padding: 12px 0;
  background: linear-gradient(135deg, #fff, #fffaf0);
  overflow: hidden;
}

.user-dropdown-menu .el-dropdown-menu__item {
  padding: 0;
  margin: 2px 8px;
  border-radius: 10px;
  transition: all 0.3s;
}

.user-dropdown-menu .el-dropdown-menu__item:hover {
  background: linear-gradient(135deg, #fff8e1, #ffe7b3);
  transform: translateX(4px);
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  text-decoration: none;
  color: #333;
  width: 100%;
  font-size: 14px;
  position: relative;
  transition: all 0.3s;
}

.dropdown-item:hover {
  color: #ff9800;
}

.dropdown-icon {
  font-size: 16px;
  color: #666;
  width: 20px;
  text-align: center;
  transition: all 0.3s;
}

.dropdown-item:hover .dropdown-icon {
  color: #ff9800;
  transform: scale(1.1);
}

.dropdown-badge {
  margin-left: auto;
}

.dropdown-badge :deep(.el-badge__content) {
  border: 2px solid #fff8e1;
  font-size: 10px;
  height: 16px;
  line-height: 12px;
  min-width: 16px;
  padding: 0 4px;
  box-shadow: 0 2px 4px rgba(255, 87, 34, 0.3);
}

.logout-item {
  color: #ff4d4f;
}

.logout-item:hover {
  color: #ff4d4f;
  background: linear-gradient(135deg, #ffccc7, #ffa39e);
}

.logout-item .dropdown-icon {
  color: #ff4d4f;
}

.logout-item:hover .dropdown-icon {
  color: #ff4d4f;
  animation: shake 0.5s;
}

@keyframes shake {
  0%, 100% { transform: rotate(0); }
  25% { transform: rotate(-15deg); }
  75% { transform: rotate(15deg); }
}

/* 个人资料区域 */
.user-profile {
  padding: 16px !important;
  cursor: default;
  border-bottom: 1px solid rgba(255, 152, 0, 0.1) !important;
  margin-bottom: 8px !important;
}

.user-profile:hover {
  background: transparent !important;
  transform: none !important;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.profile-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #ff9800;
  padding: 2px;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.3);
  flex-shrink: 0;
}

.profile-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.profile-info {
  flex: 1;
  min-width: 0;
}

.profile-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.profile-email {
  font-size: 12px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 回到顶部按钮 */
.back-top {
  position: fixed;
  bottom: 40px;
  right: 40px;
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #ff9800, #ff5722);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 6px 20px rgba(255, 152, 0, 0.4);
  z-index: 999;
  overflow: hidden;
}

.back-top::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.3s;
}

.back-top:hover::before {
  opacity: 1;
}

.back-top:hover {
  transform: translateY(-4px) scale(1.1);
  box-shadow: 0 10px 30px rgba(255, 152, 0, 0.6);
}

.back-top-icon {
  font-size: 24px;
  color: white;
  transition: all 0.3s;
  position: relative;
  z-index: 1;
}

.back-top:hover .back-top-icon {
  animation: bounce 1s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.back-top-tooltip {
  position: absolute;
  top: 50%;
  right: 60px;
  transform: translateY(-50%);
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 12px;
  white-space: nowrap;
  pointer-events: none;
  animation: slide-in 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.back-top-tooltip::before {
  content: '';
  position: absolute;
  top: 50%;
  right: -4px;
  transform: translateY(-50%) rotate(45deg);
  width: 8px;
  height: 8px;
  background: rgba(0, 0, 0, 0.8);
}

@keyframes slide-in {
  from {
    opacity: 0;
    transform: translateY(-50%) translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateY(-50%) translateX(0);
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

/* 主体内容 */
.main-body {
  min-height: calc(100vh - 112px);
  background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
  position: relative;
  z-index: 1;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .front-header {
    padding: 0 24px;
    gap: 20px;
  }
  
  .search-wrapper {
    max-width: 400px;
    min-width: 260px;
  }
  
  .menu-item {
    padding: 6px 8px;
    font-size: 12px;
  }
  
  .menu-icon {
    font-size: 14px;
  }
}

@media (max-width: 992px) {
  .front-header {
    padding: 0 20px;
    gap: 16px;
  }
  
  .front-header-left {
    gap: 10px;
  }
  
  .title-main {
    font-size: 18px;
  }
  
  .title-sub {
    display: none;
  }
  
  .front-header-center {
    gap: 12px;
  }
  
  .search-wrapper {
    max-width: 320px;
    min-width: 220px;
  }
  
  .menu-item {
    padding: 5px 7px;
    font-size: 12px;
  }
  
  .menu-icon {
    display: none;
  }
  
  .front-header-right {
    padding: 6px 12px;
    gap: 12px;
  }
  
  .user-info {
    display: none;
  }
  
  .user-arrow {
    display: none;
  }
  
  .user-dropdown-trigger {
    padding: 4px;
  }
}

@media (max-width: 768px) {
  .header-wrapper {
    padding: 8px 0;
  }
  
  .front-header {
    flex-wrap: wrap;
    padding: 8px 16px;
    gap: 12px;
  }
  
  .front-header-left {
    order: 1;
    flex: 1;
  }
  
  .front-header-right {
    order: 2;
    flex: 1;
    justify-content: flex-end;
    padding: 6px 10px;
  }
  
  .front-header-center {
    order: 3;
    flex: 100%;
    flex-direction: column;
    gap: 10px;
    margin-top: 8px;
  }
  
  .menu-wrapper {
    width: 100%;
    overflow-x: auto;
    padding: 6px 0;
    gap: 2px;
    justify-content: flex-start;
  }
  
  .search-wrapper {
    width: 100%;
    max-width: 100%;
    min-width: 100%;
  }
  
  .menu-item {
    padding: 5px 8px;
    font-size: 12px;
  }
  
  .user-notification {
    padding: 4px;
  }
  
  .notification-icon {
    font-size: 18px;
  }
  
  .back-top {
    width: 40px;
    height: 40px;
    bottom: 20px;
    right: 20px;
  }
  
  .back-top-icon {
    font-size: 20px;
  }
}
</style>

<style>
/* 全局样式覆盖 */
.logout-confirm {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(255, 152, 0, 0.2);
  border: 1px solid rgba(255, 152, 0, 0.2);
}

.logout-confirm .el-message-box__header {
  background: linear-gradient(135deg, #fff9e6, #ffe7b3);
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255, 152, 0, 0.1);
}

.logout-confirm .el-message-box__title {
  color: #d48806;
  font-weight: 600;
}

.logout-confirm .el-message-box__content {
  padding: 20px;
  color: #666;
}

.logout-confirm .el-button--warning {
  background: linear-gradient(135deg, #ff9800, #ff5722);
  border: none;
  border-radius: 8px;
  color: white;
  font-weight: 600;
  transition: all 0.3s;
}

.logout-confirm .el-button--warning:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.4);
}

.logout-confirm .el-button--default {
  border-radius: 8px;
  transition: all 0.3s;
}

.logout-confirm .el-button--default:hover {
  border-color: #ff9800;
  color: #ff9800;
  transform: translateY(-1px);
}
</style>