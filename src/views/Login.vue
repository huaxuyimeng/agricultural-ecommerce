/**
 * 登录页面
 * 文件路径: src/views/Login.vue
 * 功能描述: 用户登录入口页面，支持账号密码登录和角色选择（管理员/商家/农户），表单验证（账号、密码、角色必填），
 *           登录成功后根据角色跳转到对应首页（管理员→管理后台，商家/农户→前台首页），
 *           登录失败提示错误信息，支持回车快捷登录，页面展示轮播图和品牌标语
 * 关联文件:
 * - src/api/index.js: 提供登录认证接口
 * - src/store/index.js: 提供用户状态管理和登录后信息存储
 * - src/views/Register.vue: 注册页面（登录页链接跳转）
 */
<template>
  <div class="container">
    <div class="header">
      <div class="header-content">
        <img src="@/assets/imgs/logo.png" alt="" class="logo-img">
        <span class="site-title">"绿源农鲜"农产品销售平台</span>
      </div>
    </div>

    <div class="main">
      <div class="login-wrapper">
        <div class="left-section">
          <el-carousel :interval="4000" arrow="always" height="260px" class="carousel-box" indicator-position="outside" :autoplay="true">
            <el-carousel-item v-for="(item, index) in carouselImages" :key="index">
              <div class="carousel-item-wrapper">
                <img :src="item" class="carousel-img" alt="三农图片" />
                <div class="carousel-overlay">
                  <div class="carousel-text">{{ carouselTexts[index] }}</div>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
          <div class="slogan">
            <p>源自田间 · 鲜达餐桌</p>
            <p>绿色健康 · 品质生活</p>
          </div>
        </div>

        <div class="right-section">
          <div class="login-box">
            <div class="login-title">欢 迎 登 录</div>
            <div class="login-subtitle">连接田间地头与您的餐桌</div>
            <el-form :model="form" :rules="rules" ref="formRef">
              <el-form-item prop="username">
                <el-input 
                  size="medium" 
                  prefix-icon="el-icon-user" 
                  placeholder="请输入账号" 
                  v-model="form.username"
                  @keyup.enter="handleLogin"
                ></el-input>
              </el-form-item>
              <el-form-item prop="password">
                <el-input 
                  size="medium" 
                  autocomplete="new-password" 
                  prefix-icon="el-icon-lock" 
                  placeholder="请输入密码" 
                  show-password 
                  v-model="form.password"
                  @keyup.enter="handleLogin"
                ></el-input>
              </el-form-item>
              <el-form-item prop="role">
                <el-select style="width: 100%" v-model="form.role" placeholder="请选择角色">
                  <el-option
                    v-for="item in roleOptions"
                    :key="item.value"
                    :value="item.value"
                    :label="item.label"
                  ></el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button 
                  size="medium" 
                  class="login-btn" 
                  @click="handleLogin"
                  :loading="loading"
                >
                  {{ loading ? '登录中...' : '登 录' }}
                </el-button>
              </el-form-item>
              <div class="login-options">
                <div class="flex-1"></div>
                <div class="register-link">
                  还没有账号？请 <a href="/register">立即注册</a>
                </div>
              </div>
            </el-form>
          </div>
        </div>
      </div>
    </div>

    <div class="footer">
      <p>©2024-2025 "绿源农鲜"农产品销售平台 | 让每一份新鲜都触手可及</p>
      <p style="font-size: 12px; margin-top: 5px; opacity: 0.8;">客服热线：400-888-6666 | 工作时间：周一至周日 8:00-20:00</p>
    </div>
  </div>
</template>

<script>
import { login, getCurrentUser } from '@/api'

export default {
  name: "Login",
  data() {
    return {
      form: { 
        username: '',
        password: '',
        role: 'USER' 
      },
      roleOptions: [
        { label: '普通用户', value: 'USER' },
        { label: '农户/商家', value: 'MERCHANT' },
        { label: '平台管理员', value: 'ADMIN' }
      ],
      loading: false,
      rules: {
        username: [
          { required: true, message: '请输入账号', trigger: 'blur' },
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
        ]
      },
      carouselImages: [
        'https://images.unsplash.com/photo-1488459716781-31db52582fe9?w=800',
        'https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=800',
        'https://images.unsplash.com/photo-1464226184884-fa280b87c399?w=800',
        'https://images.unsplash.com/photo-1504868584819-f8e8b4b6d7e3?w=800',
        'https://images.unsplash.com/photo-1542838132-92c53300491e?w=800',
      ],
      carouselTexts: [
        '新鲜蔬菜 · 产地直供',
        '绿色有机 · 健康生活',
        '丰收季节 · 品质保证',
        '从田间到餐桌',
        '优选农产品 · 安心之选'
      ]
    }
  },
  mounted() {
    // 路由守卫已处理登录状态检查，此处不再重复跳转
  },
  methods: {

    getLandingPath(role) {
      const paths = {
        'ADMIN': '/home',
        'USER': '/front/home',
        'MERCHANT': '/front/home'
      }
      return paths[role]
    },

    async handleLogin() {
      try {
        const valid = await this.$refs.formRef.validate().catch(() => false)
        if (!valid) return

        this.loading = true
        
        const res = await login({
          username: this.form.username,
          password: this.form.password,
          role: this.form.role
        })
        
        // 处理不同的响应格式
        let token = ''
        let user = null
        
        if (res.data) {
          token = res.data.token || res.data.accessToken || ''
          user = res.data.user || res.data.userInfo || res.data.data
        } else {
          token = res.token || res.accessToken || ''
          user = res.user || res.userInfo || res
        }
        
        if (!user) {
          this.$message.error('登录失败：用户信息为空')
          return
        }
        
        if (!user.role) {
          this.$message.error('登录失败：用户角色信息缺失')
          return
        }

        // 保存 token 到 storage
        sessionStorage.setItem('xm-token', token)
        localStorage.setItem('xm-token', token)
        
        // 登录成功后，从数据库获取完整的用户信息
        try {
          const userRes = await getCurrentUser()
          if (userRes.data) {
            user = userRes.data.user || userRes.data
          } else {
            user = userRes
          }
        } catch (e) {
          console.warn('获取完整用户信息失败，使用登录返回的信息:', e)
        }
        
        // 清理用户信息，保存到 storage
        const cleanUser = {
          id: user.id,
          username: user.username,
          name: user.name,
          role: user.role,
          avatar: user.avatar,
          email: user.email,
          phone: user.phone,
          account: user.account,
          shopName: user.shopName,
          shopAddress: user.shopAddress,
          shopDescription: user.shopDescription,
          shopLogo: user.shopLogo,
          approvalStatus: user.approvalStatus,
          gender: user.gender,
          birthday: user.birthday,
          address: user.address,
          studentId: user.studentId,
          description: user.description
        }
        
        const userStr = JSON.stringify(cleanUser)
        sessionStorage.setItem('xm-user', userStr)
        localStorage.setItem('xm-user', userStr)
        
        // 广播用户登录事件
        try {
          window.dispatchEvent(new Event('xm-user-updated'))
        } catch (e) {
          const evt = document.createEvent('Event')
          evt.initEvent('xm-user-updated', true, true)
          window.dispatchEvent(evt)
        }
        
        this.$message.success('登录成功')
        
        const landingPath = this.getLandingPath(user.role)
        if (landingPath) {
          setTimeout(() => {
            window.location.href = landingPath
          }, 500)
        } else {
          this.$message.error('未找到对应的首页路径')
        }
      } catch (error) {
        console.error('登录错误:', error)
        this.$message.error(error.message || '登录失败，请检查账号、密码和角色')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.container {
  height: 100vh;
  overflow: hidden;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 50%, #a5d6a7 100%);
  position: relative;
}

.header {
  background-color: rgba(255, 255, 255, 0.95);
  height: 70px;
  box-shadow: 0 2px 10px rgba(76, 175, 80, 0.1);
  position: relative;
  z-index: 10;
}

.header-content {
  margin: 0 auto;
  width: 60%;
  height: 100%;
  display: flex;
  align-items: center;
}

.logo-img {
  width: 50px;
  height: 50px;
  object-fit: contain;
}

.site-title {
  font-weight: bold;
  font-size: 24px;
  margin-left: 10px;
  color: #2e7d32;
  letter-spacing: 1px;
}

.main {
  margin: 0 auto;
  width: 60%;
  height: calc(100vh - 140px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-wrapper {
  display: flex;
  width: 100%;
  max-width: 1000px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(46, 125, 50, 0.15);
}

.left-section {
  flex: 1;
  background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.carousel-box {
  width: 100%;
  background: transparent;
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.carousel-item-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.carousel-img {
  width: 100%;
  height: 260px;
  object-fit: cover;
  transition: transform 0.6s ease;
}

.carousel-item-wrapper:hover .carousel-img {
  transform: scale(1.05);
}

.carousel-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.6), transparent);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 12px;
}

.carousel-text {
  color: #fff;
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 1px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
  animation: fadeInUp 0.6s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.slogan {
  margin-top: 30px;
  text-align: center;
  color: white;
  font-size: 16px;
  line-height: 2;
  text-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.right-section {
  flex: 1;
  padding: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-box {
  width: 100%;
  max-width: 350px;
}

.login-title {
  text-align: center;
  font-size: 24px;
  margin-bottom: 10px;
  color: #2e7d32;
  font-weight: bold;
  letter-spacing: 2px;
}

.login-subtitle {
  text-align: center;
  font-size: 13px;
  color: #81c784;
  margin-bottom: 30px;
}

.login-box >>> .el-input__inner {
  border-radius: 25px;
  border: 1px solid #e0e0e0;
  transition: all 0.3s;
}

.login-box >>> .el-input__inner:focus {
  border-color: #4caf50;
  box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.2);
}

.login-box >>> .el-input__prefix {
  color: #4caf50;
}

.login-btn {
  width: 100%;
  background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
  border: none;
  color: white;
  font-size: 16px;
  letter-spacing: 2px;
  border-radius: 25px;
  height: 44px;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(76, 175, 80, 0.3);
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(76, 175, 80, 0.4);
  background: linear-gradient(135deg, #57a75a 0%, #388e3c 100%);
}

.login-options {
  display: flex;
  align-items: center;
  margin-top: 10px;
}

.flex-1 {
  flex: 1;
}

.register-link {
  font-size: 13px;
  color: #666;
}

.register-link a {
  color: #4caf50;
  text-decoration: none;
  font-weight: bold;
  transition: color 0.3s;
}

.register-link a:hover {
  color: #2e7d32;
  text-decoration: underline;
}

.footer {
  height: 70px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #558b2f;
  font-size: 13px;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(10px);
}

@media (max-width: 1200px) {
  .header-content,
  .main {
    width: 80%;
  }
}

@media (max-width: 768px) {
  .left-section {
    display: none;
  }
  
  .login-wrapper {
    max-width: 400px;
  }
}
</style>
