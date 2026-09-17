/**
 * 用户信息中心页面
 * 文件路径: src/views/manager/UserCenter.vue
 * 功能描述: 展示当前登录用户的个人信息概览，包括用户头像、用户名、角色、账户状态，
 *           用户统计数据（订单数、商品数、连续登录天数），个人资料详情（用户名、姓名、邮箱、电话、性别、角色、注册时间等），
 *           支持编辑个人资料（含头像上传）、修改密码，根据用户角色显示不同的快捷操作入口（管理员可见公告管理/用户管理，商家可见商品管理）
 * 关联文件:
 * - src/api/index.js: 提供用户信息查询和更新接口
 * - src/store/index.js: 提供当前登录用户状态
 */
<template>
  <div class="user-center-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <i class="el-icon-user"></i>
            用户信息中心
          </h1>
          <p class="page-subtitle">管理个人信息和系统设置</p>
        </div>
        <div class="header-right">
          <el-button 
            type="primary" 
            @click="editProfile"
            class="edit-profile-btn"
          >
            <i class="el-icon-edit"></i>
            编辑资料
          </el-button>
        </div>
      </div>
    </div>

    <el-card shadow="never" class="overview-card">
      <div class="overview-content">
        <div class="user-avatar-section">
          <el-avatar :size="120" :src="userAvatar" :alt="user.name" class="user-avatar">
            {{ getUserInitial }}
          </el-avatar>
          <div class="user-basic-info">
            <h2 class="user-name">{{ user.name || user.username || '用户' }}</h2>
            <div class="user-role">{{ getRoleText(user.role) }}</div>
            <div class="user-status">
              <span class="status-dot" :class="{ 'online': user.status === 1 }"></span>
              {{ user.status === 1 ? '正常' : '禁用' }}
            </div>
          </div>
        </div>
        
        <div class="user-stats">
          <div class="stat-item">
            <div class="stat-number">{{ userStats.orders }}</div>
            <div class="stat-label">订单数</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ userStats.products }}</div>
            <div class="stat-label">商品数</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ userStats.loginDays }}</div>
            <div class="stat-label">连续登录</div>
          </div>
        </div>
      </div>
    </el-card>

    <div class="info-cards">
      <el-card shadow="never" class="info-card">
        <div class="card-header">
          <i class="el-icon-user"></i>
          <h3 class="card-title">个人信息</h3>
        </div>
        <div class="info-list">
          <div class="info-item">
            <span class="info-label">用户名：</span>
            <span class="info-value">{{ user.username || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">真实姓名：</span>
            <span class="info-value">{{ user.name || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">邮箱：</span>
            <span class="info-value">{{ user.email || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">电话：</span>
            <span class="info-value">{{ user.phone || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">性别：</span>
            <span class="info-value">{{ getGenderText(user.gender) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">角色：</span>
            <span class="info-value">{{ getRoleText(user.role) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">注册时间：</span>
            <span class="info-value">{{ formatTime(user.createTime) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">最后登录：</span>
            <span class="info-value">{{ formatTime(user.lastLoginTime) }}</span>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="info-card">
        <div class="card-header">
          <i class="el-icon-setting"></i>
          <h3 class="card-title">快捷操作</h3>
        </div>
        <div class="quick-actions">
          <div class="action-item" @click="editProfile">
            <i class="el-icon-edit"></i>
            <span>编辑资料</span>
          </div>
          <div class="action-item" @click="changePassword">
            <i class="el-icon-lock"></i>
            <span>修改密码</span>
          </div>
          <div class="action-item" @click="goToPage('/home')">
            <i class="el-icon-s-home"></i>
            <span>系统首页</span>
          </div>
          <div class="action-item" @click="goToPage('/notices')" v-if="user.role === 'ADMIN'">
            <i class="el-icon-bell"></i>
            <span>公告管理</span>
          </div>
          <div class="action-item" @click="goToPage('/products')" v-if="user.role === 'MERCHANT'">
            <i class="el-icon-goods"></i>
            <span>商品管理</span>
          </div>
          <div class="action-item" @click="goToPage('/user')" v-if="user.role === 'ADMIN'">
            <i class="el-icon-user-solid"></i>
            <span>用户管理</span>
          </div>
        </div>
      </el-card>
    </div>

    <el-dialog
      title="编辑个人资料"
      :visible.sync="editDialog.visible"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" placeholder="用户名不可修改" disabled />
        </el-form-item>
        <el-form-item label="真实姓名" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" type="email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="editForm.gender">
            <el-radio label="male">男</el-radio>
            <el-radio label="female">女</el-radio>
            <el-radio label="other">其他</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleAvatarChange"
            :before-upload="beforeAvatarUpload"
          >
            <el-avatar :size="100" :src="editForm.avatar" class="avatar-preview">
              {{ getUserInitial }}
            </el-avatar>
          </el-upload>
          <div class="upload-hint">点击头像上传新图片</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="saveProfile" :loading="saving">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      title="修改密码"
      :visible.sync="passwordDialog.visible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="passwordDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="changePasswordSubmit" :loading="passwordDialog.saving">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getUserById, updateUser, setCurrentUser, getCurrentUser } from '@/data/user'

export default {
  name: "UserCenter",
  data() {
    const validatePassword = (rule, value, callback) => {
      if (value.length < 6) {
        callback(new Error('密码长度不能少于6位'))
      } else {
        const hasLetter = /[a-zA-Z]/.test(value)
        const hasNumber = /\d/.test(value)
        const hasSpecial = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(value)
        const count = [hasLetter, hasNumber, hasSpecial].filter(Boolean).length
        if (count < 2) {
          callback(new Error('密码至少包含字母、数字、特殊字符中的两种'))
        } else {
          callback()
        }
      }
    }
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      user: {},
      
      userStats: {
        orders: 0,
        products: 0,
        loginDays: 0
      },
      
      editDialog: {
        visible: false
      },
      
      editForm: {
        username: '',
        name: '',
        email: '',
        phone: '',
        gender: '',
        avatar: ''
      },
      
      editFormAvatarFile: null,
      
      editRules: {
        name: [
          { required: true, message: '请输入真实姓名', trigger: 'blur' }
        ],
        email: [
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ]
      },
      
      passwordDialog: {
        visible: false,
        saving: false
      },
      
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      
      passwordRules: {
        oldPassword: [
          { required: true, message: '请输入旧密码', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, validator: validatePassword, trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, validator: validateConfirmPassword, trigger: 'blur' }
        ]
      },
      
      saving: false
    }
  },
  
  computed: {
    userAvatar() {
      if (!this.user.avatar) {
        return '/imgs/user/admin.png'
      }
      
      const avatar = this.user.avatar
      if (avatar.startsWith('data:')) {
        return avatar
      }
      if (avatar.startsWith('http')) {
        return avatar
      }
      if (avatar.startsWith('/')) {
        return avatar
      }
      const baseUrl = (process.env.VUE_APP_BASEURL || '').replace('/api', '') || 'http://localhost:9090'
      return `${baseUrl}/upload/${avatar}`
    },
    
    getUserInitial() {
      const name = this.user.name || this.user.username || '用户'
      return name.charAt(0).toUpperCase()
    }
  },
  
  created() {
    this.loadUserInfo()
  },
  
  methods: {
    async loadUserInfo() {
      try {
        const currentUser = getCurrentUser()
        if (!currentUser || !currentUser.id) {
          this.$message.error('未获取到用户信息，请重新登录')
          this.$router.push('/login')
          return
        }
        
        const res = await getUserById(currentUser.id)
        if (res) {
          this.user = {
            id: res.id,
            username: res.username || '',
            name: res.name || '',
            phone: res.phone || '',
            email: res.email || '',
            avatar: res.avatar || '',
            role: res.role || 'USER',
            status: res.status ?? 1,
            gender: res.gender || '',
            createTime: res.createTime,
            lastLoginTime: res.lastLoginTime
          }
        }
      } catch (error) {
        console.error('加载用户信息失败:', error)
        this.$message.error('加载用户信息失败')
      }
    },
    
    loadUserStats() {
      this.userStats = {
        orders: Math.floor(Math.random() * 50) + 1,
        products: Math.floor(Math.random() * 100) + 1,
        loginDays: Math.floor(Math.random() * 30) + 1
      }
    },
    
    editProfile() {
      this.editForm = {
        username: this.user.username || '',
        name: this.user.name || '',
        email: this.user.email || '',
        phone: this.user.phone || '',
        gender: this.user.gender || '',
        avatar: this.user.avatar || ''
      }
      this.editFormAvatarFile = null
      this.editDialog.visible = true
    },
    
    async saveProfile() {
      const valid = await this.$refs.editFormRef.validate().catch(() => false)
      if (!valid) return
      
      this.saving = true
      try {
        let avatarPath = this.editForm.avatar
        
        if (this.editFormAvatarFile) {
          const formData = new FormData()
          formData.append('file', this.editFormAvatarFile)
          
          const uploadRes = await this.$request.post('/upload', formData)
          
          if (uploadRes && uploadRes.data) {
            avatarPath = uploadRes.data
          } else {
            this.$message.error('头像上传失败')
            return
          }
        }
        
        const submitData = {
          name: this.editForm.name,
          email: this.editForm.email,
          phone: this.editForm.phone,
          gender: this.editForm.gender,
          avatar: avatarPath
        }
        
        await updateUser(this.user.id, submitData)
        
        this.user = {
          ...this.user,
          ...submitData
        }
        
        this.syncCurrentUser()
        
        this.editDialog.visible = false
        this.editFormAvatarFile = null
        this.$message.success('资料更新成功')
      } catch (error) {
        console.error('资料更新失败:', error)
        this.$message.error(error.message || '资料更新失败')
      } finally {
        this.saving = false
      }
    },
    
    changePassword() {
      this.passwordForm = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
      this.passwordDialog.visible = true
      this.$nextTick(() => {
        this.$refs.passwordFormRef?.clearValidate()
      })
    },
    
    async changePasswordSubmit() {
      const valid = await this.$refs.passwordFormRef.validate().catch(() => false)
      if (!valid) return
      
      this.passwordDialog.saving = true
      try {
        const userData = {
          password: this.passwordForm.newPassword
        }
        
        const res = await updateUser(this.user.id, userData)
        
        if (res) {
          this.$message.success('密码修改成功，请重新登录')
          this.passwordDialog.visible = false
          
          this.$confirm('密码已修改，需要重新登录', '修改成功', {
            confirmButtonText: '重新登录',
            cancelButtonText: '稍后',
            type: 'success',
            center: true
          }).then(() => {
            this.handleLogout()
          }).catch(() => {})
        } else {
          this.$message.error('密码修改失败')
        }
      } catch (error) {
        console.error('修改密码失败:', error)
        this.$message.error(error.message || '修改密码失败')
      } finally {
        this.passwordDialog.saving = false
      }
    },
    
    handleLogout() {
      localStorage.removeItem('xm-user')
      localStorage.removeItem('xm-token')
      this.$router.push('/login')
    },
    
    syncCurrentUser() {
      const currentUser = getCurrentUser()
      if (currentUser) {
        setCurrentUser({ ...currentUser, ...this.user })
        window.dispatchEvent(new CustomEvent('xm-user-updated'))
      }
    },
    
    goToPage(path) {
      this.$router.push(path)
    },
    
    handleAvatarChange(file) {
      const isImage = file.raw.type.startsWith('image/')
      if (!isImage) {
        this.$message.error('只能上传图片文件')
        return
      }
      const isLt2M = file.raw.size / 1024 / 1024 < 2
      if (!isLt2M) {
        this.$message.error('图片大小不能超过 2MB')
        return
      }
      
      this.editFormAvatarFile = file.raw
      
      const reader = new FileReader()
      reader.onload = (e) => {
        this.editForm.avatar = e.target.result
      }
      reader.readAsDataURL(file.raw)
    },
    
    beforeAvatarUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2
      if (!isImage) {
        this.$message.error('只能上传图片文件')
        return false
      }
      if (!isLt2M) {
        this.$message.error('图片大小不能超过 2MB')
        return false
      }
      return true
    },
    
    formatTime(time) {
      if (!time) return '未记录'
      const date = new Date(time)
      const now = new Date()
      const diffMs = now - date
      const diffMins = Math.floor(diffMs / 60000)
      const diffHours = Math.floor(diffMs / 3600000)
      const diffDays = Math.floor(diffMs / 86400000)
      
      if (diffMins < 1) return '刚刚'
      if (diffMins < 60) return `${diffMins}分钟前`
      if (diffHours < 24) return `${diffHours}小时前`
      if (diffDays < 7) return `${diffDays}天前`
      
      return date.toLocaleDateString()
    },
    
    getRoleText(role) {
      const roleMap = {
        'ADMIN': '管理员',
        'MERCHANT': '农户/商家',
        'USER': '普通用户'
      }
      return roleMap[role] || role
    },
    
    getGenderText(gender) {
      const genderMap = {
        'male': '男',
        'female': '女',
        'other': '其他'
      }
      return genderMap[gender] || '未设置'
    }
  }
}
</script>

<style scoped>
.user-center-page {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  min-height: calc(100vh - 60px);
  box-sizing: border-box;
}

.page-header {
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #ebeef5;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.header-left {
  flex: 1;
  min-width: 0;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px 0;
  line-height: 1.4;
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-title::before {
  content: '';
  display: block;
  width: 4px;
  height: 20px;
  background: linear-gradient(135deg, #409EFF, #66B1FF);
  border-radius: 2px;
}

.page-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.overview-card {
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
}

.overview-content {
  display: flex;
  flex-wrap: wrap;
  gap: 40px;
  align-items: center;
  padding: 30px;
}

.user-avatar-section {
  display: flex;
  align-items: center;
  gap: 30px;
  flex: 1;
  min-width: 300px;
}

.user-avatar {
  border: 4px solid #ecf5ff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.user-basic-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px 0;
}

.user-role {
  font-size: 16px;
  color: #409EFF;
  margin-bottom: 8px;
}

.user-status {
  font-size: 14px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #909399;
}

.status-dot.online {
  background: #67c23a;
}

.user-stats {
  display: flex;
  gap: 30px;
  flex-wrap: wrap;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  min-width: 100px;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.info-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.info-card {
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.card-header i {
  font-size: 20px;
  color: #409EFF;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.info-label {
  font-size: 14px;
  color: #606266;
  min-width: 100px;
}

.info-value {
  font-size: 14px;
  color: #303133;
  flex: 1;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-item:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.action-item i {
  font-size: 20px;
  color: #409EFF;
}

.action-item span {
  font-size: 14px;
  color: #303133;
}

.avatar-uploader {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-preview {
  cursor: pointer;
  transition: all 0.3s ease;
}

.avatar-preview:hover {
  opacity: 0.8;
}

.upload-hint {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 768px) {
  .info-cards {
    grid-template-columns: 1fr;
  }
  
  .quick-actions {
    grid-template-columns: 1fr;
  }
  
  .overview-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .user-avatar-section {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
