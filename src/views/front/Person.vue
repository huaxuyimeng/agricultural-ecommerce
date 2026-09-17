/**
 * 个人中心页面
 * 文件路径: src/views/front/Person.vue
 * 功能描述: 用户个人信息与设置管理，左侧个人信息栏（头像上传/预览、用户名、角色、邮箱、手机号、地址、简介编辑），
 *           右侧设置区Tab切换：基本信息编辑、安全设置（修改密码）、收货地址管理、
 *           支持头像更换（上传/裁剪）、个人资料保存、密码修改验证
 * 关联文件:
 * - src/api/index.js: 提供用户信息更新、文件上传接口
 * - src/store/index.js: 提供当前登录用户状态
 */
<template>
  <div class="profile-container">
    <div class="page-header">
      <h1 class="page-title">
        <i class="el-icon-user-solid"></i>
        个人中心
      </h1>
      <el-button @click="$router.back()" class="back-btn" size="small" plain>
        <i class="el-icon-arrow-left"></i>
        返回
      </el-button>
    </div>

    <div class="profile-layout">
      <!-- 左侧个人信息栏 -->
      <div class="profile-sidebar">
        <div class="sidebar-card profile-info-card">
          <div class="avatar-wrapper">
            <el-upload
              class="avatar-uploader"
              action=""
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleAvatarChange"
            >
              <div class="avatar-container">
                <img 
                  v-if="user.avatar" 
                  :src="getAvatarUrl(user.avatar)" 
                  class="avatar"
                  @error="handleAvatarError"
                />
                <div v-else class="default-avatar">
                  <i class="el-icon-user"></i>
                </div>
                <div class="avatar-overlay">
                  <i class="el-icon-camera"></i>
                  <span>更换头像</span>
                </div>
              </div>
            </el-upload>
          </div>
          
          <div class="user-basic-info">
            <h3 class="user-name">{{ user.name || user.username || '用户' }}</h3>
            <p class="user-desc" v-if="user.description">{{ user.description }}</p>
            <div class="user-meta">
              <span class="meta-item">
                <i class="el-icon-date"></i>
                注册于 {{ formatDate(user.createTime) }}
              </span>
            </div>
          </div>
        </div>

        <div class="sidebar-card balance-card">
          <div class="balance-header">
            <i class="el-icon-wallet"></i>
            <span>账户余额</span>
          </div>
          <div class="balance-amount">
            <span class="currency">¥</span>
            <span class="value">{{ user.account || '0.00' }}</span>
          </div>
          <el-button 
            type="primary" 
            size="small" 
            @click="showChargeDialog"
            class="charge-btn"
          >
            <i class="el-icon-plus"></i>
            立即充值
          </el-button>
        </div>

        <div class="sidebar-card quick-nav-card">
          <div class="card-title">
            <i class="el-icon-menu"></i>
            <span>快捷导航</span>
          </div>
          <div class="nav-grid">
            <div class="nav-item" @click="$router.push('/front/my-orders')">
              <div class="nav-icon order-icon">
                <i class="el-icon-s-order"></i>
              </div>
              <span class="nav-label">我的订单</span>
              <span class="nav-badge" v-if="orderStats.pending > 0">{{ orderStats.pending }}</span>
            </div>
            
            <div class="nav-item" @click="$router.push('/front/favorites')">
              <div class="nav-icon favorite-icon">
                <i class="el-icon-star-off"></i>
              </div>
              <span class="nav-label">我的收藏</span>
              <span class="nav-badge" v-if="favoriteCount > 0">{{ favoriteCount }}</span>
            </div>
            
            <div class="nav-item" @click="$router.push('/front/my-coupons')">
              <div class="nav-icon coupon-icon">
                <i class="el-icon-present"></i>
              </div>
              <span class="nav-label">优惠券</span>
              <span class="nav-badge" v-if="couponStats.available > 0">{{ couponStats.available }}</span>
            </div>
            
            <div class="nav-item" @click="$router.push('/front/my-address')">
              <div class="nav-icon address-icon">
                <i class="el-icon-location"></i>
              </div>
              <span class="nav-label">收货地址</span>
            </div>
          </div>
        </div>

        <div class="sidebar-card security-card">
          <div class="card-title">
            <i class="el-icon-lock"></i>
            <span>账户安全</span>
          </div>
          <div class="security-level">
            <div class="level-header">
              <span class="level-label">安全等级</span>
              <span class="level-value" :class="securityLevel.class">{{ securityLevel.text }}</span>
            </div>
            <div class="level-bar">
              <div class="level-fill" :style="{ width: securityLevel.percent + '%' }"></div>
            </div>
          </div>
          <div class="security-items">
            <div class="security-item">
              <i class="el-icon-mobile-phone"></i>
              <div class="item-info">
                <span class="item-label">绑定手机</span>
                <span class="item-value">{{ maskPhone(user.phone) }}</span>
              </div>
              <i class="el-icon-check item-status success"></i>
            </div>
            <div class="security-item">
              <i class="el-icon-message"></i>
              <div class="item-info">
                <span class="item-label">绑定邮箱</span>
                <span class="item-value">{{ maskEmail(user.email) }}</span>
              </div>
              <i class="el-icon-check item-status success" v-if="user.email"></i>
              <i class="el-icon-close item-status warning" v-else></i>
            </div>
            <div class="security-item" @click="showPasswordDialog">
              <i class="el-icon-key"></i>
              <div class="item-info">
                <span class="item-label">登录密码</span>
                <span class="item-value">定期修改更安全</span>
              </div>
              <i class="el-icon-arrow-right item-action"></i>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧内容区 -->
      <div class="profile-main">
        <el-card class="main-card">
          <el-tabs v-model="activeTab" class="profile-tabs">
            <el-tab-pane label="基本信息" name="basic">
              <div class="tab-content">
                <div class="section-header">
                  <h3 class="section-title">基本信息</h3>
                  <p class="section-desc">完善您的个人信息，方便我们为您提供更好的服务</p>
                </div>
                
                <el-form :model="user" label-width="100px" class="info-form">
                  <div class="form-section">
                    <h4 class="form-section-title">账号信息</h4>
                    <el-row :gutter="24">
                      <el-col :span="12">
                        <el-form-item label="用户名">
                          <span class="readonly-text">{{ user.username }}</span>
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="真实姓名">
                          <span class="readonly-text">{{ user.name || '未设置' }}</span>
                        </el-form-item>
                      </el-col>
                    </el-row>
                  </div>

                  <div class="form-section">
                    <h4 class="form-section-title">联系方式</h4>
                    <el-row :gutter="24">
                      <el-col :span="12">
                        <el-form-item label="手机号">
                          <el-input 
                            v-model="user.phone" 
                            placeholder="请输入手机号"
                            clearable
                          />
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="邮箱">
                          <el-input 
                            v-model="user.email" 
                            placeholder="请输入邮箱"
                            clearable
                          />
                        </el-form-item>
                      </el-col>
                    </el-row>
                  </div>

                  <div class="form-section">
                    <h4 class="form-section-title">个人资料</h4>
                    <el-row :gutter="24">
                      <el-col :span="12">
                        <el-form-item label="性别">
                          <el-radio-group v-model="user.gender">
                            <el-radio label="男">男</el-radio>
                            <el-radio label="女">女</el-radio>
                          </el-radio-group>
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="生日">
                          <el-date-picker
                            v-model="user.birthday"
                            type="date"
                            placeholder="选择生日"
                            style="width: 100%"
                          />
                        </el-form-item>
                      </el-col>
                    </el-row>
                    <el-form-item label="个人简介">
                      <el-input 
                        v-model="user.description" 
                        placeholder="介绍一下自己吧..."
                        type="textarea"
                        :rows="3"
                        maxlength="200"
                        show-word-limit
                      />
                    </el-form-item>
                  </div>

                  <div class="form-section">
                    <h4 class="form-section-title">收货地址</h4>
                    <el-form-item label="默认地址">
                      <el-input 
                        v-model="user.address" 
                        placeholder="请输入收货地址"
                        type="textarea"
                        :rows="2"
                      />
                    </el-form-item>
                  </div>

                  <div class="form-actions">
                    <el-button @click="resetForm">重置</el-button>
                    <el-button type="primary" @click="saveProfile" :loading="saving">
                      <i class="el-icon-check"></i>
                      保存修改
                    </el-button>
                  </div>
                </el-form>
              </div>
            </el-tab-pane>

            <el-tab-pane label="修改密码" name="password">
              <div class="tab-content">
                <div class="section-header">
                  <h3 class="section-title">
                    <i class="el-icon-lock"></i>
                    修改密码
                  </h3>
                  <p class="section-desc">定期修改密码可以提高账户安全性</p>
                </div>
                
                <el-form 
                  :model="passwordForm" 
                  label-width="120px" 
                  class="password-form"
                  @submit.native.prevent
                >
                  <el-form-item label="原密码">
                    <el-input
                      v-model="passwordForm.oldPassword"
                      type="password"
                      placeholder="请输入原密码"
                      show-password
                    />
                  </el-form-item>
                  <el-form-item label="新密码">
                    <el-input
                      v-model="passwordForm.newPassword"
                      type="password"
                      placeholder="请输入新密码（至少6位）"
                      show-password
                    />
                  </el-form-item>
                  <el-form-item label="确认密码">
                    <el-input
                      v-model="passwordForm.confirmPassword"
                      type="password"
                      placeholder="请再次输入新密码"
                      show-password
                    />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="changePassword" :loading="changingPassword">
                      <i class="el-icon-check"></i>
                      确认修改
                    </el-button>
                  </el-form-item>
                </el-form>
              </div>
            </el-tab-pane>

            <el-tab-pane label="商家信息" name="merchant" v-if="user.role === 'MERCHANT'">
              <div class="tab-content">
                <div class="section-header">
                  <h3 class="section-title">
                    <i class="el-icon-shop"></i>
                    店铺信息
                  </h3>
                  <p class="section-desc">完善店铺信息，让顾客更好地了解您的店铺</p>
                </div>
                
                <el-form :model="user" label-width="100px" class="info-form">
                  <el-row :gutter="24">
                    <el-col :span="12">
                      <el-form-item label="店铺名称">
                        <el-input 
                          v-model="user.shopName" 
                          placeholder="请输入店铺名称"
                          clearable
                        />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="店铺地址">
                        <el-input 
                          v-model="user.shopAddress" 
                          placeholder="请输入店铺地址"
                          clearable
                        />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  <el-form-item label="店铺描述">
                    <el-input 
                      v-model="user.shopDescription" 
                      placeholder="请输入店铺描述"
                      type="textarea"
                      :rows="3"
                    />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="saveProfile" :loading="saving">
                      <i class="el-icon-check"></i>
                      保存修改
                    </el-button>
                  </el-form-item>
                </el-form>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </div>
    </div>

    <!-- 充值对话框 -->
    <el-dialog
      title="账户充值"
      :visible.sync="chargeVisible"
      width="420px"
      center
      :append-to-body="true"
      class="charge-dialog"
    >
      <div class="charge-content">
        <div class="charge-amount-display">
          <span class="label">充值金额</span>
          <div class="amount-value">
            <span class="currency">¥</span>
            <span class="value">{{ finalAmount }}</span>
          </div>
        </div>
        
        <div class="amount-section">
          <div class="amount-title">选择充值金额</div>
          <div class="amount-options">
            <el-button
              v-for="amount in [10, 20, 50, 100, 200]"
              :key="amount"
              :class="{ active: selectedAmount === amount }"
              @click="selectAmount(amount)"
              plain
              size="small"
            >
              ¥{{ amount }}
            </el-button>
          </div>
          
          <div class="custom-amount">
            <el-input
              v-model="customAmount"
              placeholder="自定义金额"
              type="number"
              :min="1"
            >
              <template #prepend>¥</template>
            </el-input>
          </div>
        </div>

        <div class="qrcode-section">
          <img src="/imgs/pay.png" alt="支付二维码" class="qrcode-img" />
          <p class="qrcode-hint">请使用微信/支付宝扫码支付</p>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="chargeVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmCharge" :loading="charging">
            <i class="el-icon-check"></i>
            确认支付
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getCurrentUser, updateUser, chargeAccount as apiChargeAccount, changePassword as apiChangePassword } from '@/api'

export default {
  name: 'Profile',
  data() {
    return {
      user: {},
      activeTab: 'basic',
      chargeVisible: false,
      selectedAmount: 50,
      customAmount: '',
      charging: false,
      saving: false,
      changingPassword: false,
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      orderStats: {
        pending: 0,
        total: 0
      },
      favoriteCount: 0,
      couponStats: {
        available: 0,
        total: 0
      }
    }
  },
  computed: {
    finalAmount() {
      if (this.customAmount && this.customAmount > 0) {
        return parseFloat(this.customAmount)
      }
      return this.selectedAmount
    },
    securityLevel() {
      let score = 0
      if (this.user.phone) score += 40
      if (this.user.email) score += 30
      if (this.user.birthday) score += 10
      if (this.user.address) score += 10
      if (this.user.description) score += 10
      
      if (score >= 80) return { text: '高', class: 'high', percent: 100 }
      if (score >= 50) return { text: '中', class: 'medium', percent: 60 }
      return { text: '低', class: 'low', percent: 30 }
    }
  },
  created() {
    this.loadUserInfo()
    this.loadStats()
  },
  methods: {
    async loadUserInfo() {
      try {
        const res = await getCurrentUser()
        if (res.data) {
          this.user = res.data.user || res.data
        } else {
          this.user = res
        }
        localStorage.setItem('xm-user', JSON.stringify(this.user))
        sessionStorage.setItem('xm-user', JSON.stringify(this.user))
      } catch (e) {
        console.error('获取用户信息失败:', e)
        const stored = localStorage.getItem('xm-user')
        if (stored) {
          this.user = JSON.parse(stored)
        }
      }
    },

    async loadStats() {
      try {
        const request = await import('@/utils/request')
        
        const ordersRes = await request.default({
          url: '/orders/my',
          method: 'get'
        })
        if (ordersRes.code === 200 && ordersRes.data) {
          const orders = ordersRes.data
          this.orderStats.total = orders.length
          this.orderStats.pending = orders.filter(o => 
            ['PENDING', 'PAID'].includes(o.status)
          ).length
        }
      } catch (e) {
        console.error('获取订单统计失败:', e)
      }

      try {
        const request = await import('@/utils/request')
        
        const couponsRes = await request.default({
          url: '/user-coupon/my/status',
          method: 'get',
          params: { status: 1 }
        })
        if (couponsRes.code === 200 && couponsRes.data) {
          this.couponStats.available = couponsRes.data.length
        }
      } catch (e) {
        console.error('获取优惠券统计失败:', e)
      }
    },
    
    async handleAvatarChange(file) {
      const isImage = file.raw.type.startsWith('image/')
      if (!isImage) {
        this.$message.error('只能上传图片文件')
        return
      }
      
      const isLt2M = file.raw.size / 1024 / 1024 < 2
      if (!isLt2M) {
        this.$message.error('头像图片大小不能超过 2MB')
        return
      }

      const formData = new FormData()
      formData.append('file', file.raw)
      
      try {
        const res = await this.$request.post('/upload', formData)
        let relativePath = res.data || res.url || res
        
        if (relativePath) {
          const baseUrl = (process.env.VUE_APP_BASEURL || '').replace('/api', '') || 'http://localhost:9090'
          const fullUrl = `${baseUrl}/upload/${relativePath}`
          
          await updateUser(this.user.id, { avatar: relativePath })
          
          const freshUserRes = await getCurrentUser()
          const freshUser = freshUserRes.data || freshUserRes
          this.user = { ...this.user, ...freshUser, avatar: relativePath, avatarUrl: fullUrl }
          
          localStorage.setItem('xm-user', JSON.stringify(this.user))
          sessionStorage.setItem('xm-user', JSON.stringify(this.user))
          
          window.dispatchEvent(new CustomEvent('xm-user-updated', { 
            detail: { user: this.user },
            bubbles: true,
            cancelable: true
          }))
          
          this.$message.success('头像更新成功')
        }
      } catch (error) {
        console.error('头像上传失败:', error)
        this.$message.error(error.message || '头像上传失败，请重试')
      }
    },
    
    async saveProfile() {
      if (this.user.phone && !/^1[3-9]\d{9}$/.test(this.user.phone)) {
        this.$message.error('请输入正确的手机号码')
        return
      }
      
      if (this.user.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.user.email)) {
        this.$message.error('请输入正确的邮箱地址')
        return
      }
      
      this.saving = true
      try {
        const updateData = {
          name: this.user.name,
          email: this.user.email,
          phone: this.user.phone,
          avatar: this.user.avatar,
          gender: this.user.gender,
          birthday: this.user.birthday,
          address: this.user.address,
          description: this.user.description
        }
        
        if (this.user.role === 'MERCHANT') {
          updateData.shopName = this.user.shopName
          updateData.shopAddress = this.user.shopAddress
          updateData.shopDescription = this.user.shopDescription
        }
        
        await updateUser(this.user.id, updateData)
        
        const freshUserRes = await getCurrentUser()
        const freshUser = freshUserRes.data || freshUserRes
        this.user = { ...this.user, ...freshUser }
        
        localStorage.setItem('xm-user', JSON.stringify(this.user))
        sessionStorage.setItem('xm-user', JSON.stringify(this.user))
        
        window.dispatchEvent(new CustomEvent('xm-user-updated', { 
          detail: { user: this.user },
          bubbles: true,
          cancelable: true
        }))
        
        this.$message.success('保存成功')
      } catch (e) {
        this.$message.error(e.message || '保存失败')
      } finally {
        this.saving = false
      }
    },

    async changePassword() {
      if (!this.passwordForm.oldPassword) {
        this.$message.error('请输入原密码')
        return
      }
      if (!this.passwordForm.newPassword || this.passwordForm.newPassword.length < 6) {
        this.$message.error('新密码长度至少为6位')
        return
      }
      if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
        this.$message.error('两次输入的密码不一致')
        return
      }

      this.changingPassword = true
      try {
        await apiChangePassword({
          oldPassword: this.passwordForm.oldPassword,
          newPassword: this.passwordForm.newPassword
        })
        this.passwordForm = {
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        }
        this.$message.success('密码修改成功')
        this.activeTab = 'basic'
      } catch (e) {
        this.$message.error(e.message || '密码修改失败')
      } finally {
        this.changingPassword = false
      }
    },
    
    showChargeDialog() {
      this.selectedAmount = 50
      this.customAmount = ''
      this.chargeVisible = true
    },
    
    selectAmount(amount) {
      this.selectedAmount = amount
      this.customAmount = ''
    },
    
    async confirmCharge() {
      if (!this.finalAmount || this.finalAmount < 1) {
        this.$message.warning('请选择或输入充值金额')
        return
      }
      
      this.charging = true
      try {
        await apiChargeAccount(this.finalAmount)
        
        const res = await getCurrentUser()
        if (res.data) {
          this.user = res.data.user || res.data
        }
        
        localStorage.setItem('xm-user', JSON.stringify(this.user))
        sessionStorage.setItem('xm-user', JSON.stringify(this.user))
        
        window.dispatchEvent(new CustomEvent('xm-user-updated', { 
          detail: { user: this.user },
          bubbles: true,
          cancelable: true
        }))

        this.$message.success(`充值成功！¥${this.finalAmount} 已到账`)
        this.chargeVisible = false
      } catch (e) {
        this.$message.error(e.message || '充值失败')
      } finally {
        this.charging = false
      }
    },

    showPasswordDialog() {
      this.activeTab = 'password'
    },

    resetForm() {
      this.loadUserInfo()
      this.$message.info('已重置为原始数据')
    },
    
    getAvatarUrl(avatar) {
      if (!avatar) return '/imgs/user/zhangsan.png'
      if (avatar.startsWith('data:')) return avatar
      if (avatar.startsWith('http')) return avatar
      if (avatar.startsWith('/')) return avatar
      const baseUrl = (process.env.VUE_APP_BASEURL || '').replace('/api', '') || 'http://localhost:9090'
      return `${baseUrl}/upload/${avatar}`
    },
    
    handleAvatarError(event) {
      event.target.src = '/imgs/user/zhangsan.png'
    },

    formatDate(time) {
      if (!time) return '-'
      const date = new Date(time)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },

    maskPhone(phone) {
      if (!phone || phone.length < 7) return '未绑定'
      return phone.substring(0, 3) + '****' + phone.substring(7)
    },

    maskEmail(email) {
      if (!email) return '未绑定'
      const parts = email.split('@')
      if (parts.length < 2) return email
      const name = parts[0]
      if (name.length <= 2) return name[0] + '**@' + parts[1]
      return name.substring(0, 2) + '***@' + parts[1]
    }
  }
}
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-title i {
  color: #52c41a;
}

.back-btn {
  color: #666;
}

.profile-layout {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 24px;
  align-items: start;
}

.profile-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sidebar-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
}

.profile-info-card {
  text-align: center;
}

.avatar-wrapper {
  margin-bottom: 16px;
}

.avatar-container {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto;
  position: relative;
  cursor: pointer;
  border: 3px solid #fff;
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.2);
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-avatar {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #52c41a, #73d13d);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 40px;
}

.avatar-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 6px;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.avatar-container:hover .avatar-overlay {
  opacity: 1;
}

.user-basic-info {
  text-align: center;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.user-desc {
  font-size: 13px;
  color: #8c8c8c;
  margin: 0 0 12px 0;
  line-height: 1.5;
}

.user-meta {
  font-size: 12px;
  color: #bfbfbf;
}

.meta-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.balance-card {
  background: linear-gradient(135deg, #52c41a, #73d13d);
  color: white;
  border: none;
}

.balance-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 12px;
}

.balance-amount {
  margin-bottom: 16px;
}

.balance-amount .currency {
  font-size: 18px;
  font-weight: 500;
}

.balance-amount .value {
  font-size: 32px;
  font-weight: 700;
  margin-left: 4px;
}

.charge-btn {
  width: 100%;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.4);
  color: white;
}

.charge-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.6);
}

.quick-nav-card .card-title,
.security-card .card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.nav-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  background: #fafafa;
}

.nav-item:hover {
  background: #f6ffed;
  transform: translateY(-2px);
}

.nav-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: white;
}

.order-icon { background: linear-gradient(135deg, #ff7a45, #ff9c6e); }
.favorite-icon { background: linear-gradient(135deg, #ff4d4f, #ff7875); }
.coupon-icon { background: linear-gradient(135deg, #faad14, #ffc53d); }
.address-icon { background: linear-gradient(135deg, #1890ff, #40a9ff); }

.nav-label {
  font-size: 13px;
  color: #595959;
  font-weight: 500;
}

.nav-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  background: #ff4d4f;
  color: white;
  font-size: 11px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.security-level {
  margin-bottom: 16px;
}

.level-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.level-label {
  font-size: 13px;
  color: #8c8c8c;
}

.level-value {
  font-size: 13px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
}

.level-value.high {
  background: #f6ffed;
  color: #52c41a;
}

.level-value.medium {
  background: #fff7e6;
  color: #faad14;
}

.level-value.low {
  background: #fff1f0;
  color: #ff4d4f;
}

.level-bar {
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
}

.level-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s;
}

.level-value.high ~ .level-bar .level-fill,
.level-fill {
  background: linear-gradient(90deg, #52c41a, #73d13d);
}

.security-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.security-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  border-radius: 8px;
  background: #fafafa;
  cursor: pointer;
  transition: background 0.2s;
}

.security-item:hover {
  background: #f6ffed;
}

.security-item > i:first-child {
  font-size: 18px;
  color: #8c8c8c;
  width: 20px;
  text-align: center;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.item-label {
  font-size: 13px;
  color: #595959;
  font-weight: 500;
}

.item-value {
  font-size: 12px;
  color: #bfbfbf;
}

.item-status {
  font-size: 16px;
}

.item-status.success {
  color: #52c41a;
}

.item-status.warning {
  color: #faad14;
}

.item-action {
  color: #bfbfbf;
  font-size: 14px;
}

.profile-main {
  min-width: 0;
}

.main-card {
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.profile-tabs ::v-deep .el-tabs__header {
  margin-bottom: 0;
  border-bottom: 1px solid #f0f0f0;
}

.profile-tabs ::v-deep .el-tabs__item {
  font-size: 15px;
  color: #8c8c8c;
  padding: 0 24px;
  height: 56px;
  line-height: 56px;
}

.profile-tabs ::v-deep .el-tabs__item.is-active {
  color: #52c41a;
  font-weight: 600;
}

.profile-tabs ::v-deep .el-tabs__active-bar {
  background: #52c41a;
  height: 3px;
}

.tab-content {
  padding: 24px;
}

.section-header {
  margin-bottom: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title i {
  color: #52c41a;
}

.section-desc {
  font-size: 13px;
  color: #8c8c8c;
  margin: 0;
}

.form-section {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f5f5f5;
}

.form-section:last-of-type {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.form-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #595959;
  margin: 0 0 16px 0;
  padding-left: 12px;
  border-left: 3px solid #52c41a;
}

.readonly-text {
  font-size: 14px;
  color: #595959;
  line-height: 32px;
}

.info-form ::v-deep .el-form-item__label {
  color: #595959;
  font-weight: 500;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f5f5f5;
}

.password-form {
  max-width: 500px;
}

.charge-dialog ::v-deep .el-dialog__header {
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 16px;
}

.charge-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.charge-amount-display {
  text-align: center;
  padding: 20px;
  background: linear-gradient(135deg, #f6ffed, #e6fffb);
  border-radius: 12px;
}

.charge-amount-display .label {
  font-size: 14px;
  color: #8c8c8c;
  display: block;
  margin-bottom: 8px;
}

.amount-value .currency {
  font-size: 20px;
  color: #52c41a;
  font-weight: 500;
}

.amount-value .value {
  font-size: 36px;
  color: #52c41a;
  font-weight: 700;
  margin-left: 4px;
}

.amount-section {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.amount-title {
  font-size: 14px;
  font-weight: 600;
  color: #595959;
  margin-bottom: 12px;
}

.amount-options {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
  margin-bottom: 12px;
}

.amount-options .el-button {
  padding: 8px;
}

.amount-options .el-button.active {
  background: #52c41a;
  border-color: #52c41a;
  color: white;
}

.qrcode-section {
  text-align: center;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.qrcode-img {
  width: 140px;
  height: 140px;
  border: 1px solid #e8e8e8;
  padding: 8px;
  background: white;
  border-radius: 8px;
  margin-bottom: 8px;
}

.qrcode-hint {
  font-size: 12px;
  color: #8c8c8c;
  margin: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 900px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }
  
  .profile-sidebar {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  
  .profile-info-card {
    grid-column: 1 / -1;
  }
}

@media (max-width: 600px) {
  .profile-container {
    padding: 16px;
  }
  
  .profile-sidebar {
    grid-template-columns: 1fr;
  }
  
  .nav-grid {
    grid-template-columns: repeat(4, 1fr);
  }
  
  .amount-options {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .tab-content {
    padding: 16px;
  }
}
</style>
