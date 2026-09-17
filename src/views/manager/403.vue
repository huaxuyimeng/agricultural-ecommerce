/**
 * 权限不足页面（403）
 * 文件路径: src/views/manager/403.vue
 * 功能描述: 用户无权限访问时的提示拦截页面，展示403错误代码和图标、当前账户信息及所需权限提示、
 *           自动倒计时返回首页、操作按钮组（返回首页/返回上一页/重新登录（未登录时）/获取帮助），
 *           帮助弹窗（解释原因、解决方案、联系方式），开发环境显示调试信息（当前路径、用户角色、用户信息JSON）
 * 关联文件:
 * - src/router/index.js: 路由权限守卫触发403跳转
 * - src/store/index.js: 提供当前登录用户信息
 */
<template>
  <div class="no-auth-page">
    <div class="container">
      <div class="no-auth-content">
        <!-- 错误代码和图标 -->
        <div class="error-code">403</div>
        <div class="error-icon">🚫</div>
        <div class="error-title">权限不足</div>
        
        <div class="error-description">
          <p>抱歉，您没有权限访问此页面</p>
          <p class="detail-info" v-if="userInfo">
            当前账户: <span class="user-info">{{ userInfo.name || userInfo.username }}</span> 
            ({{ roleMap[userInfo.role] || userInfo.role }})
          </p>
          <p class="detail-info" v-if="requiredRole">
            所需权限: <span class="role-required">{{ roleMap[requiredRole] || requiredRole }}</span>
          </p>
        </div>
        
        <!-- 倒计时提示 -->
        <div class="countdown" v-if="countdown > 0">
          {{ countdown }} 秒后自动返回首页...
        </div>
        
        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button 
            type="primary" 
            @click="goHome"
            class="back-home-btn"
            :loading="isLoadingHome"
          >
            <i class="el-icon-s-home"></i>
            {{ getHomeButtonText() }}
          </el-button>
          
          <el-button 
            @click="goBack"
            class="back-btn"
            :disabled="!canGoBack"
          >
            <i class="el-icon-arrow-left"></i>
            返回上一页
          </el-button>
          
          <el-button 
            @click="goLogin"
            class="login-btn"
            v-if="!userInfo"
          >
            <i class="el-icon-user"></i>
            重新登录
          </el-button>
          
          <el-button 
            @click="showHelp = true"
            class="help-btn"
          >
            <i class="el-icon-question"></i>
            获取帮助
          </el-button>
        </div>
        
        <!-- 调试信息（开发环境显示） -->
        <div class="debug-info" v-if="isDevelopment">
          <p>当前路径: {{ currentPath }}</p>
          <p>用户角色: {{ userInfo ? userInfo.role : '未登录' }}</p>
          <p>用户信息: {{ JSON.stringify(userInfo) }}</p>
        </div>
      </div>
    </div>

    <!-- 帮助对话框 -->
    <el-dialog
      title="权限问题帮助"
      :visible.sync="showHelp"
      width="500px"
      center
    >
      <div class="help-content">
        <h3>为什么会出现这个页面？</h3>
        <p>您可能遇到以下情况：</p>
        <ul>
          <li>🔐 您的账户权限不足，无法访问此页面</li>
          <li>🔁 登录信息已过期，请重新登录</li>
          <li>⚙️ 系统配置变更导致权限变更</li>
          <li>🔄 您正在访问不存在的页面</li>
        </ul>
        
        <h3>解决方案：</h3>
        <ol>
          <li>尝试返回首页重新操作</li>
          <li>使用具有相应权限的账户重新登录</li>
          <li>联系管理员获取帮助</li>
        </ol>
        
        <div class="contact-info">
          <p><i class="el-icon-phone"></i> 客服热线: 400-888-6666</p>
          <p><i class="el-icon-message"></i> 技术支持: support@greenfarm.com</p>
        </div>
      </div>
      
      <span slot="footer" class="dialog-footer">
        <el-button @click="showHelp = false">关 闭</el-button>
        <el-button type="primary" @click="goHome">返回首页</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { ROLE_OPTIONS, LANDING_PATH_BY_ROLE } from '@/config/auth'

export default {
  name: "NoAuth",
  data() {
    return {
      countdown: 5, // 倒计时秒数
      countdownTimer: null,
      isLoadingHome: false,
      showHelp: false,
      canGoBack: true,
      currentPath: this.$route.path,
      isDevelopment: process.env.NODE_ENV === 'development',
      userInfo: null,
      roleMap: {},
      requiredRole: null
    }
  },
  created() {
    // 初始化角色映射
    this.roleMap = ROLE_OPTIONS.reduce((map, role) => {
      map[role.value] = role.label
      return map
    }, {})
    
    // 获取用户信息
    this.loadUserInfo()
    
    // 从路由查询参数中获取所需的角色
    this.requiredRole = this.$route.query.requiredRole || null
    
    // 检查是否可以返回上一页
    this.checkGoBack()
  },
  mounted() {
    // 开始倒计时
    this.startCountdown()
  },
  beforeDestroy() {
    // 清除定时器
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer)
    }
  },
  methods: {
    loadUserInfo() {
      try {
        const userStr = localStorage.getItem('xm-user')
        if (userStr) {
          this.userInfo = JSON.parse(userStr)
        }
      } catch (error) {
        console.error('解析用户信息失败:', error)
      }
    },
    
    getHomePath() {
      if (!this.userInfo) {
        return '/login'
      }
      
      const landingPath = LANDING_PATH_BY_ROLE[this.userInfo.role]
      return landingPath || '/front/home'
    },
    
    getHomeButtonText() {
      if (!this.userInfo) {
        return '前往登录'
      }
      
      const roleText = this.roleMap[this.userInfo.role] || this.userInfo.role
      switch (this.userInfo.role) {
        case 'ADMIN':
          return '返回管理后台'
        case 'MERCHANT':
          return '返回商家中心'
        case 'USER':
          return '返回用户首页'
        default:
          return '返回首页'
      }
    },
    
    goHome() {
      this.isLoadingHome = true
      
      const homePath = this.getHomePath()
      console.log(`跳转到: ${homePath}`)
      
      setTimeout(() => {
        this.$router.replace(homePath)
        this.isLoadingHome = false
      }, 300)
    },
    
    goBack() {
      if (window.history.length > 1) {
        this.$router.go(-1)
      } else {
        this.goHome()
      }
    },
    
    goLogin() {
      this.$router.replace('/login')
    },
    
    checkGoBack() {
      this.canGoBack = window.history.length > 1
    },
    
    startCountdown() {
      this.countdownTimer = setInterval(() => {
        if (this.countdown > 0) {
          this.countdown--
        } else {
          clearInterval(this.countdownTimer)
          this.goHome()
        }
      }, 1000)
    }
  }
}
</script>

<style scoped>
.no-auth-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  box-sizing: border-box;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

.container {
  max-width: 600px;
  width: 100%;
  background: white;
  border-radius: 20px;
  padding: 50px 40px;
  box-shadow: 0 20px 60px rgba(103, 151, 255, 0.1);
  animation: slide-up 0.6s ease-out;
  border: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
  overflow: hidden;
}

.container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 5px;
  background: linear-gradient(90deg, #f56c6c, #f78989, #ff9f9f);
}

@keyframes slide-up {
  from {
    opacity: 0;
    transform: translateY(40px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.no-auth-content {
  text-align: center;
  position: relative;
  z-index: 1;
}

.error-code {
  font-size: 140px;
  font-weight: 900;
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  margin-bottom: 10px;
  text-shadow: 5px 5px 20px rgba(245, 108, 108, 0.1);
  font-family: 'Arial Black', sans-serif;
}

.error-icon {
  font-size: 60px;
  margin-bottom: 20px;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.error-title {
  font-size: 32px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 20px;
  line-height: 1.3;
  position: relative;
  display: inline-block;
}

.error-title::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 4px;
  background: linear-gradient(90deg, #f56c6c, #f78989);
  border-radius: 2px;
}

.error-description {
  font-size: 18px;
  color: #5a6c7d;
  margin-bottom: 25px;
  line-height: 1.8;
}

.error-description p {
  margin: 8px 0;
}

.detail-info {
  font-size: 16px;
  color: #7f8c8d;
  margin-top: 15px;
  padding: 12px 20px;
  background: #f8f9fa;
  border-radius: 10px;
  display: inline-block;
  border-left: 4px solid #f56c6c;
}

.user-info {
  color: #409eff;
  font-weight: 600;
  margin: 0 5px;
}

.role-required {
  color: #f56c6c;
  font-weight: 600;
  margin: 0 5px;
}

.countdown {
  font-size: 16px;
  color: #e74c3c;
  background: rgba(231, 76, 60, 0.1);
  padding: 10px 20px;
  border-radius: 25px;
  display: inline-block;
  margin-bottom: 30px;
  font-weight: 600;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.7; }
  100% { opacity: 1; }
}

.action-buttons {
  display: flex;
  gap: 15px;
  justify-content: center;
  flex-wrap: wrap;
  margin-top: 30px;
}

.back-home-btn {
  border-radius: 12px;
  padding: 15px 32px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  border: none;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
}

.back-home-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(64, 158, 255, 0.4);
  background: linear-gradient(135deg, #66b1ff, #409eff);
}

.back-btn {
  border-radius: 12px;
  padding: 15px 32px;
  font-size: 16px;
  font-weight: 600;
  border-color: #409eff;
  color: #409eff;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
}

.back-btn:hover:not(.is-disabled) {
  background: #409eff;
  color: white;
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(64, 158, 255, 0.2);
}

.back-btn.is-disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-btn {
  border-radius: 12px;
  padding: 15px 32px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #67c23a, #85ce61);
  border: none;
  color: white;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(103, 194, 58, 0.3);
}

.login-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(103, 194, 58, 0.4);
  background: linear-gradient(135deg, #85ce61, #67c23a);
}

.help-btn {
  border-radius: 12px;
  padding: 15px 32px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #f56c6c, #f78989);
  border: none;
  color: white;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(245, 108, 108, 0.3);
}

.help-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(245, 108, 108, 0.4);
  background: linear-gradient(135deg, #f78989, #f56c6c);
}

.debug-info {
  margin-top: 30px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  color: #666;
  text-align: left;
  border-left: 4px solid #409eff;
}

.debug-info p {
  margin: 5px 0;
  word-break: break-all;
}

/* 帮助对话框样式 */
.help-content h3 {
  color: #2c3e50;
  margin: 20px 0 10px 0;
  font-size: 18px;
}

.help-content ul,
.help-content ol {
  margin: 10px 0 20px 20px;
  line-height: 1.8;
}

.help-content li {
  margin-bottom: 8px;
  color: #5a6c7d;
}

.contact-info {
  margin-top: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  border-left: 4px solid #67c23a;
}

.contact-info p {
  margin: 8px 0;
  color: #666;
  display: flex;
  align-items: center;
  gap: 8px;
}

.contact-info i {
  color: #67c23a;
  font-size: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 15px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding: 30px 20px;
    border-radius: 15px;
  }
  
  .error-code {
    font-size: 100px;
  }
  
  .error-title {
    font-size: 24px;
  }
  
  .error-description {
    font-size: 16px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 12px;
  }
  
  .back-home-btn,
  .back-btn,
  .login-btn,
  .help-btn {
    width: 100%;
    justify-content: center;
    padding: 12px 20px;
  }
  
  .detail-info {
    font-size: 14px;
    padding: 10px 15px;
  }
}

@media (max-width: 480px) {
  .error-code {
    font-size: 80px;
  }
  
  .error-title {
    font-size: 20px;
  }
  
  .error-description {
    font-size: 15px;
  }
}
</style>