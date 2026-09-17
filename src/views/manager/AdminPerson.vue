/**
 * 管理员个人中心页面
 * 文件路径: src/views/manager/AdminPerson.vue
 * 功能描述: 管理员个人账户信息与安全设置管理，顶部显示账户状态标签和上次登录时间，
 *           左侧基本信息卡片（头像上传预览、用户名、角色标签、姓名、邮箱、手机、性别、注册时间），
 *           支持编辑模式切换（基本信息修改和保存），右侧安全设置面板（密码修改含原密码/新密码/确认密码验证），
 *           头像更换弹窗（上传/预览）
 * 关联文件:
 * - src/api/index.js: 提供用户信息更新、文件上传接口
 * - src/store/index.js: 提供当前登录用户状态
 * - src/views/manager/Admin.vue: 管理员管理列表页面
 */
<template>
  <div class="admin-person-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <div class="header-icon">
            <i class="el-icon-user"></i>
          </div>
          <div>
            <h1 class="page-title">个人信息</h1>
            <p class="page-subtitle">管理您的个人账户信息和安全设置</p>
          </div>
        </div>
        <div class="header-right">
          <div class="user-status">
            <el-tag type="success" class="status-tag" effect="dark">
              <i class="el-icon-success"></i>
              账户状态正常
            </el-tag>
            <div class="last-login">
              <i class="el-icon-time"></i>
              上次登录：{{ formatLastLogin() }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 左侧：个人信息 -->
      <div class="left-content">
        <!-- 基本信息卡片 -->
        <el-card shadow="never" class="info-card">
          <template #header>
            <div class="card-header">
              <div class="card-title-wrapper">
                <div class="title-icon">
                  <i class="el-icon-s-custom"></i>
                </div>
                <h3 class="card-title">基本信息</h3>
              </div>
              <el-button 
                type="primary" 
                @click="toggleEditMode"
                class="edit-toggle-btn"
                :icon="editMode ? 'el-icon-close' : 'el-icon-edit'"
                plain
              >
                {{ editMode ? '取消编辑' : '编辑信息' }}
              </el-button>
            </div>
          </template>
          
          <div class="card-content">
            <!-- 头像区域 -->
            <div class="avatar-section">
              <div class="avatar-container">
                <div class="avatar-wrapper" @click="showAvatarDialog">
                  <el-image
                    :src="avatarSrc"
                    fit="cover"
                    class="avatar-image"
                    :preview-src-list="[avatarSrc]"
                  >
                    <template #error>
                      <div class="avatar-error">
                        <i class="el-icon-user"></i>
                      </div>
                    </template>
                    <template #placeholder>
                      <div class="avatar-loading">
                        <i class="el-icon-loading"></i>
                      </div>
                    </template>
                  </el-image>
                  <div class="avatar-overlay">
                    <i class="el-icon-camera"></i>
                    <span>更换头像</span>
                  </div>
                </div>
                
                <div class="avatar-info">
                  <h3 class="avatar-name">{{ form.name || form.username }}</h3>
                  <div class="avatar-meta">
                    <el-tag :type="getRoleTagType(form.role)" class="role-tag" effect="dark">
                      <i class="el-icon-medal"></i>
                      {{ getRoleText(form.role) }}
                    </el-tag>
                    <span class="avatar-id">
                      <i class="el-icon-user-solid"></i>
                      ID: {{ form.id }}
                    </span>
                  </div>
                  <p class="avatar-tips">点击头像可预览和更换</p>
                </div>
              </div>
            </div>
            
            <!-- 基本信息表单 -->
            <el-form 
              :model="form" 
              :rules="rules" 
              ref="formRef" 
              label-width="120px"
              class="info-form"
              :disabled="!editMode"
            >
              <div class="form-section">
                <div class="section-title">
                  <div class="title-icon-small">
                    <i class="el-icon-user"></i>
                  </div>
                  <span>账户信息</span>
                </div>
                
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="登录账号" prop="username">
                      <el-input 
                        v-model="form.username" 
                        placeholder="登录账号"
                        disabled
                        class="disabled-input"
                        prefix-icon="el-icon-user"
                      >
                        <template #append>
                          <el-tooltip content="账号不可修改" placement="top">
                            <i class="el-icon-lock"></i>
                          </el-tooltip>
                        </template>
                      </el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="用户姓名" prop="name">
                      <el-input 
                        v-model="form.name" 
                        placeholder="请输入真实姓名"
                        clearable
                        maxlength="20"
                        show-word-limit
                        prefix-icon="el-icon-s-custom"
                        :disabled="!editMode"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>
              
              <div class="form-section">
                <div class="section-title">
                  <div class="title-icon-small">
                    <i class="el-icon-phone-outline"></i>
                  </div>
                  <span>联系信息</span>
                </div>
                
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="手机号码" prop="phone">
                      <el-input 
                        v-model="form.phone" 
                        placeholder="请输入手机号码"
                        clearable
                        maxlength="11"
                        prefix-icon="el-icon-phone"
                        :disabled="!editMode"
                      >
                        <template #prepend>+86</template>
                      </el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="电子邮箱" prop="email">
                      <el-input 
                        v-model="form.email" 
                        placeholder="请输入电子邮箱"
                        clearable
                        maxlength="50"
                        prefix-icon="el-icon-message"
                        :disabled="!editMode"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>
              
              <div class="form-section">
                <div class="section-title">
                  <div class="title-icon-small">
                    <i class="el-icon-time"></i>
                  </div>
                  <span>账户详情</span>
                </div>
                
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="账户角色" prop="role">
                      <el-input 
                        v-model="form.role" 
                        placeholder="账户角色"
                        disabled
                        class="disabled-input"
                        prefix-icon="el-icon-s-custom"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="账户状态" prop="status">
                      <el-input 
                        v-model="form.statusText" 
                        placeholder="账户状态"
                        disabled
                        class="disabled-input"
                        prefix-icon="el-icon-check"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="注册时间" prop="createTime">
                      <el-input 
                        v-model="form.createTime" 
                        placeholder="注册时间"
                        disabled
                        class="disabled-input"
                        prefix-icon="el-icon-time"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="最后修改" prop="updateTime">
                      <el-input 
                        v-model="form.updateTime" 
                        placeholder="最后修改时间"
                        disabled
                        class="disabled-input"
                        prefix-icon="el-icon-edit"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>
              
              <div class="form-section" v-if="editMode">
                <div class="section-title">
                  <div class="title-icon-small">
                    <i class="el-icon-edit"></i>
                  </div>
                  <span>个人描述</span>
                </div>
                
                <el-form-item label="个人简介" prop="description">
                  <el-input
                    v-model="form.description"
                    type="textarea"
                    :rows="4"
                    placeholder="请输入个人简介（可选）"
                    maxlength="200"
                    show-word-limit
                    resize="none"
                  />
                </el-form-item>
              </div>
              
              <!-- 操作按钮 -->
              <div v-if="editMode" class="form-actions">
                <el-button 
                  type="primary" 
                  @click="handleSave"
                  :loading="saving"
                  class="save-btn"
                >
                  <i class="el-icon-check"></i>
                  保存更改
                </el-button>
                <el-button 
                  @click="handleReset"
                  class="reset-btn"
                >
                  <i class="el-icon-refresh"></i>
                  重置
                </el-button>
              </div>
            </el-form>
          </div>
        </el-card>
      </div>
      
      <!-- 右侧：安全设置 -->
      <div class="right-content">
        <!-- 安全设置卡片 -->
        <el-card shadow="never" class="security-card">
          <template #header>
            <div class="card-header">
              <div class="card-title-wrapper">
                <div class="title-icon">
                  <i class="el-icon-lock"></i>
                </div>
                <h3 class="card-title">安全设置</h3>
              </div>
            </div>
          </template>
          
          <div class="security-content">
            <!-- 密码修改 -->
            <div class="security-item">
              <div class="item-header">
                <div class="item-title">
                  <i class="el-icon-key"></i>
                  修改密码
                </div>
                <el-button 
                  type="text" 
                  @click="togglePasswordForm"
                  class="toggle-btn"
                >
                  {{ showPasswordForm ? '收起' : '修改' }}
                </el-button>
              </div>
              
              <div v-if="showPasswordForm" class="password-form">
                <el-form 
                  :model="passwordForm" 
                  :rules="passwordRules" 
                  ref="passwordFormRef" 
                  label-width="120px"
                  class="password-form-content"
                >
                  <el-form-item label="当前密码" prop="currentPassword">
                    <el-input 
                      v-model="passwordForm.currentPassword" 
                      type="password"
                      placeholder="请输入当前密码"
                      show-password
                      clearable
                      maxlength="20"
                      prefix-icon="el-icon-lock"
                    />
                  </el-form-item>
                  
                  <el-form-item label="新密码" prop="newPassword">
                    <el-input 
                      v-model="passwordForm.newPassword" 
                      type="password"
                      placeholder="请输入新密码"
                      show-password
                      clearable
                      maxlength="20"
                      show-word-limit
                      prefix-icon="el-icon-key"
                    />
                    <div class="password-strength">
                      <div class="strength-label">密码强度：</div>
                      <el-progress 
                        :percentage="passwordStrength" 
                        :color="passwordStrengthColor"
                        :show-text="false"
                        class="strength-bar"
                      />
                      <span class="strength-text">{{ passwordStrengthText }}</span>
                    </div>
                  </el-form-item>
                  
                  <el-form-item label="确认密码" prop="confirmPassword">
                    <el-input 
                      v-model="passwordForm.confirmPassword" 
                      type="password"
                      placeholder="请再次输入新密码"
                      show-password
                      clearable
                      maxlength="20"
                      show-word-limit
                      prefix-icon="el-icon-key"
                    />
                  </el-form-item>
                  
                  <div class="password-tips">
                    <p><strong>密码要求：</strong></p>
                    <p>• 长度6-20个字符</p>
                    <p>• 至少包含字母、数字、特殊字符中的两种</p>
                    <p>• 不能与当前密码相同</p>
                  </div>
                  
                  <div class="form-actions">
                    <el-button 
                      type="warning" 
                      @click="handleChangePassword"
                      :loading="changingPassword"
                      class="change-btn"
                    >
                      <i class="el-icon-refresh"></i>
                      确认修改
                    </el-button>
                    <el-button 
                      @click="resetPasswordForm"
                      class="reset-password-btn"
                    >
                      重置
                    </el-button>
                  </div>
                </el-form>
              </div>
            </div>
            
            <!-- 账户安全 -->
            <div class="security-item">
              <div class="item-header">
                <div class="item-title">
                  <i class="el-icon-shield"></i>
                  账户安全
                </div>
              </div>
              
              <div class="security-options">
                <div class="option-item">
                  <div class="option-content">
                    <div class="option-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                      <i class="el-icon-lock"></i>
                    </div>
                    <div class="option-info">
                      <div class="option-title">登录密码</div>
                      <div class="option-desc">定期修改密码可以提高账户安全性</div>
                    </div>
                  </div>
                  <el-button 
                    type="primary" 
                    size="small"
                    @click="togglePasswordForm"
                    class="security-action-btn"
                  >
                    {{ showPasswordForm ? '收起' : '修改' }}
                  </el-button>
                </div>
                
                <div class="option-item">
                  <div class="option-content">
                    <div class="option-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7);">
                      <i class="el-icon-user-solid"></i>
                    </div>
                    <div class="option-info">
                      <div class="option-title">账户角色</div>
                      <div class="option-desc">{{ getRoleText(form.role) }}</div>
                    </div>
                  </div>
                  <el-tag type="danger" size="small" effect="dark">
                    {{ getRoleText(form.role) }}
                  </el-tag>
                </div>
              </div>
            </div>
            
            <!-- 退出登录 -->
            <div class="security-actions">
              <el-button 
                type="danger"
                plain
                @click="handleLogout"
                class="logout-btn"
              >
                <i class="el-icon-switch-button"></i>
                退出登录
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 头像对话框 -->
    <el-dialog
      title="更换头像"
      :visible.sync="avatarDialog.visible"
      width="600px"
      class="avatar-dialog"
    >
      <div class="avatar-dialog-content">
        <div class="avatar-preview-section">
          <div class="avatar-preview">
            <img 
              :src="avatarDialog.previewSrc" 
              alt="头像预览"
              class="preview-image"
              :style="{
                width: avatarDialog.size + 'px',
                height: avatarDialog.size + 'px',
                borderRadius: avatarDialog.shape === 'circle' ? '50%' : '8px'
              }"
            />
          </div>
          
          <div class="avatar-controls">
            <div class="control-group">
              <label class="control-label">头像形状：</label>
              <el-radio-group v-model="avatarDialog.shape" size="small">
                <el-radio label="circle">圆形</el-radio>
                <el-radio label="square">方形</el-radio>
              </el-radio-group>
            </div>
            
            <div class="control-group">
              <label class="control-label">头像大小：</label>
              <el-slider
                v-model="avatarDialog.size"
                :min="100"
                :max="300"
                :step="10"
                show-input
                class="size-slider"
              />
            </div>
          </div>
        </div>
        
        <div class="avatar-upload-section">
          <div class="upload-methods">
            <el-upload
              class="avatar-uploader"
              action="#"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleAvatarUpload"
              accept="image/*"
              :before-upload="beforeAvatarUpload"
            >
              <div class="upload-area">
                <i class="el-icon-upload"></i>
                <div class="upload-text">上传图片</div>
                <div class="upload-hint">支持 JPG、PNG 格式，大小不超过 2MB</div>
              </div>
            </el-upload>
            
            <div class="avatar-templates">
              <div class="templates-title">系统头像</div>
              <div class="templates-list">
                <div 
                  v-for="template in avatarTemplates" 
                  :key="template.id"
                  class="template-item"
                  @click="selectAvatarTemplate(template)"
                >
                  <img 
                    :src="template.url" 
                    alt="系统头像"
                    class="template-image"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="avatarDialog.visible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="saveAvatar"
            :loading="avatarDialog.saving"
          >
            保存头像
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getUserById, updateUser, setCurrentUser, getCurrentUser } from '@/data/user'

export default {
  name: "AdminPerson",
  data() {
    const validatePasswordStrength = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入密码'))
      } else if (value.length < 6) {
        callback(new Error('密码长度不能少于6位'))
      } else if (value.length > 20) {
        callback(new Error('密码长度不能超过20位'))
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
      if (!value) {
        callback(new Error('请确认密码'))
      } else if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    
    return {
      editMode: false,
      form: {
        id: null,
        username: '',
        name: '',
        phone: '',
        email: '',
        avatar: '',
        role: '',
        status: 1,
        statusText: '正常',
        createTime: '',
        updateTime: '',
        description: '',
        lastLoginTime: null
      },
      rules: {
        name: [
          { required: true, message: '请输入姓名', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ],
        email: [
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        description: [
          { max: 200, message: '不能超过200个字符', trigger: 'blur' }
        ]
      },
      showPasswordForm: false,
      passwordForm: {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      passwordRules: {
        currentPassword: [
          { required: true, message: '请输入当前密码', trigger: 'blur' },
          { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, validator: validatePasswordStrength, trigger: 'blur' },
          { validator: (rule, value, callback) => {
            if (value === this.passwordForm.currentPassword) {
              callback(new Error('新密码不能与当前密码相同'))
            } else {
              callback()
            }
          }, trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, validator: validateConfirmPassword, trigger: 'blur' }
        ]
      },
      saving: false,
      changingPassword: false,
      avatarDialog: {
        visible: false,
        previewSrc: '',
        saving: false,
        selectedFile: null,
        shape: 'circle',
        size: 150
      },
      avatarTemplates: [
        { id: 1, url: '/imgs/user/admin.png' },
        { id: 2, url: '/imgs/user/zhangsan.png' },
        { id: 3, url: '/imgs/user/farmer.png' }
      ]
    }
  },
  computed: {
    avatarSrc() {
      if (this.form.avatar) {
        if (this.form.avatar.startsWith('data:image')) {
          return this.form.avatar
        }
        if (this.form.avatar.startsWith('http')) {
          return this.form.avatar
        }
        if (this.form.avatar.startsWith('/')) {
          return this.form.avatar
        }
        const baseUrl = (process.env.VUE_APP_BASEURL || '').replace('/api', '') || 'http://localhost:9090'
        return `${baseUrl}/upload/${this.form.avatar}`
      }
      return '/imgs/user/admin.png'
    },
    passwordStrength() {
      if (!this.passwordForm.newPassword) return 0
      const password = this.passwordForm.newPassword
      let strength = 0
      if (password.length >= 6) strength += 20
      if (password.length >= 8) strength += 20
      if (password.length >= 12) strength += 20
      if (/[a-zA-Z]/.test(password)) strength += 20
      if (/\d/.test(password)) strength += 20
      if (/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) strength += 20
      return Math.min(strength, 100)
    },
    passwordStrengthColor() {
      if (this.passwordStrength < 40) return '#f56c6c'
      if (this.passwordStrength < 70) return '#e6a23c'
      return '#67c23a'
    },
    passwordStrengthText() {
      if (this.passwordStrength < 40) return '弱'
      if (this.passwordStrength < 70) return '中'
      return '强'
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
          this.form = {
            id: res.id,
            username: res.username || '',
            name: res.name || '',
            phone: res.phone || '',
            email: res.email || '',
            avatar: res.avatar || '',
            role: res.role || 'ADMIN',
            status: res.status ?? 1,
            statusText: res.status === 1 ? '正常' : '禁用',
            createTime: this.formatDateTime(res.createTime),
            updateTime: this.formatDateTime(res.updateTime),
            description: res.description || '',
            lastLoginTime: res.lastLoginTime
          }
        }
      } catch (error) {
        console.error('加载用户信息失败:', error)
        this.$message.error('加载用户信息失败')
      }
    },
    
    getRoleText(role) {
      const roleMap = {
        'ADMIN': '管理员',
        'MERCHANT': '农户/商家',
        'USER': '普通用户'
      }
      return roleMap[role] || role
    },
    
    getRoleTagType(role) {
      const types = {
        'ADMIN': 'danger',
        'MERCHANT': 'warning',
        'USER': 'success'
      }
      return types[role] || 'info'
    },
    
    formatDateTime(time) {
      if (!time) return '-'
      return new Date(time).toLocaleString('zh-CN', {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit'
      })
    },
    
    formatLastLogin() {
      if (!this.form.lastLoginTime) return '从未登录'
      const date = new Date(this.form.lastLoginTime)
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
    
    toggleEditMode() {
      this.editMode = !this.editMode
      if (!this.editMode) {
        this.resetForm()
      }
    },
    
    resetForm() {
      this.loadUserInfo()
      this.$refs.formRef?.clearValidate()
    },
    
    async handleSave() {
      try {
        const valid = await this.$refs.formRef.validate().catch(() => false)
        if (!valid) return
        
        this.saving = true
        
        const userData = {
          name: this.form.name,
          phone: this.form.phone,
          email: this.form.email,
          description: this.form.description
        }
        
        const res = await updateUser(this.form.id, userData)
        
        if (res) {
          this.$message.success('个人信息更新成功')
          this.loadUserInfo()
          this.editMode = false
          this.syncCurrentUser()
        } else {
          this.$message.error('个人信息更新失败')
        }
      } catch (error) {
        console.error('保存个人信息失败:', error)
        this.$message.error(error.message || '保存失败')
      } finally {
        this.saving = false
      }
    },
    
    handleReset() {
      this.resetForm()
      this.$message.info('已重置修改')
    },
    
    togglePasswordForm() {
      this.showPasswordForm = !this.showPasswordForm
      if (!this.showPasswordForm) {
        this.resetPasswordForm()
      }
    },
    
    resetPasswordForm() {
      this.passwordForm = {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
      this.$refs.passwordFormRef?.clearValidate()
    },
    
    async handleChangePassword() {
      try {
        const valid = await this.$refs.passwordFormRef.validate().catch(() => false)
        if (!valid) return
        
        this.changingPassword = true
        
        const userData = {
          password: this.passwordForm.newPassword
        }
        
        const res = await updateUser(this.form.id, userData)
        
        if (res) {
          this.$message.success('密码修改成功，请重新登录')
          this.resetPasswordForm()
          this.showPasswordForm = false
          
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
        this.changingPassword = false
      }
    },
    
    showAvatarDialog() {
      this.avatarDialog.previewSrc = this.avatarSrc
      this.avatarDialog.selectedFile = null
      this.avatarDialog.visible = true
    },
    
    handleAvatarUpload(file) {
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
      
      this.avatarDialog.selectedFile = file.raw
      
      const reader = new FileReader()
      reader.onload = (e) => {
        this.avatarDialog.previewSrc = e.target.result
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
    
    selectAvatarTemplate(template) {
      this.avatarDialog.previewSrc = template.url
      this.avatarDialog.selectedFile = null
    },
    
    async saveAvatar() {
      try {
        this.avatarDialog.saving = true
        
        let avatarPath = this.form.avatar
        
        if (this.avatarDialog.selectedFile) {
          const formData = new FormData()
          formData.append('file', this.avatarDialog.selectedFile)
          
          const uploadRes = await this.$request.post('/upload', formData)
          
          if (uploadRes && uploadRes.data) {
            avatarPath = uploadRes.data
          } else {
            this.$message.error('头像上传失败')
            return
          }
        }
        
        const userData = {
          avatar: avatarPath
        }
        
        const res = await updateUser(this.form.id, userData)
        
        if (res) {
          this.$message.success('头像更新成功')
          this.avatarDialog.visible = false
          this.avatarDialog.selectedFile = null
          this.loadUserInfo()
          this.syncCurrentUser()
        } else {
          this.$message.error('头像更新失败')
        }
      } catch (error) {
        console.error('保存头像失败:', error)
        this.$message.error(error.message || '保存头像失败')
      } finally {
        this.avatarDialog.saving = false
      }
    },
    
    syncCurrentUser() {
      const currentUser = getCurrentUser()
      if (currentUser) {
        setCurrentUser({ ...currentUser, ...this.form })
        window.dispatchEvent(new CustomEvent('xm-user-updated'))
      }
    },
    
    handleLogout() {
      localStorage.removeItem('xm-user')
      localStorage.removeItem('xm-token')
      sessionStorage.removeItem('xm-user')
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.admin-person-page {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  min-height: calc(100vh - 60px);
  box-sizing: border-box;
}

/* 页面头部 */
.page-header {
  margin-bottom: 24px;
  padding: 32px 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
  position: relative;
  overflow: hidden;
  animation: slide-down 0.3s ease;
}

.page-header::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  pointer-events: none;
}

.page-header::after {
  content: '';
  position: absolute;
  bottom: -30%;
  left: -5%;
  width: 200px;
  height: 200px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
  pointer-events: none;
}

@keyframes slide-down {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  position: relative;
  z-index: 1;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
  flex: 1;
  min-width: 0;
}

.header-icon {
  width: 56px;
  height: 56px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: white;
  margin: 0 0 6px 0;
  line-height: 1.4;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  font-weight: 500;
}

.user-status {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.15);
  padding: 10px 20px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.status-tag {
  font-size: 13px;
  font-weight: 600;
  padding: 6px 14px;
  border-radius: 8px;
  border: none;
  display: flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3);
}

.last-login {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.95);
  background: rgba(255, 255, 255, 0.1);
  padding: 6px 12px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

.last-login i {
  font-size: 14px;
}

/* 主内容区域 */
.main-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 1fr;
  }
}

/* 卡片通用样式 */
.info-card,
.security-card {
  border-radius: 16px;
  border: 1px solid #ebeef5;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  margin-bottom: 20px;
  animation: fade-in 0.5s ease;
  overflow: hidden;
  background: white;
}

@keyframes fade-in {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.info-card:hover,
.security-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border-color: #dcdfe6;
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(to right, #fafbfc, #ffffff);
}

.card-title-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  box-shadow: 0 4px 8px rgba(102, 126, 234, 0.3);
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}

.edit-toggle-btn {
  font-weight: 600;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  border: 1px solid #409EFF;
}

.edit-toggle-btn:hover {
  background: #409EFF;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.edit-toggle-btn:hover {
  background: rgba(64, 158, 255, 0.1);
  transform: translateY(-1px);
}

/* 头像区域 */
.avatar-section {
  padding: 32px 24px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 24px;
  background: linear-gradient(to bottom, #fafbfc, #ffffff);
}

.avatar-container {
  display: flex;
  align-items: center;
  gap: 32px;
  flex-wrap: wrap;
}

.avatar-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 4px solid white;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.avatar-wrapper:hover {
  transform: translateY(-4px) scale(1.05);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.18);
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.3s ease;
}

.avatar-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa, #e4e7ed);
  color: #999;
  font-size: 40px;
}

.avatar-loading {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa, #e4e7ed);
  color: #409EFF;
  font-size: 30px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.7));
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  font-size: 12px;
  font-weight: 600;
  text-align: center;
  padding: 10px;
  gap: 6px;
}

.avatar-overlay i {
  font-size: 24px;
}

.avatar-info {
  flex: 1;
  min-width: 0;
}

.avatar-name {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 12px 0;
  line-height: 1.3;
}

.avatar-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.role-tag {
  font-size: 12px;
  padding: 6px 12px;
  height: 28px;
  line-height: 16px;
  border-radius: 8px;
  border: none;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}

.role-tag i {
  font-size: 14px;
}

.avatar-id {
  font-size: 13px;
  color: #606266;
  background: #f5f7fa;
  padding: 6px 12px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

.avatar-id i {
  font-size: 14px;
  color: #909399;
}

.avatar-tips {
  font-size: 12px;
  color: #909399;
  margin: 8px 0 0 0;
  font-style: italic;
}

/* 表单区域 */
.card-content {
  padding: 24px;
}

.form-section {
  margin-bottom: 28px;
  padding: 24px;
  background: #fafbfc;
  border-radius: 12px;
  border: 1px solid #e8eaed;
  transition: all 0.3s ease;
}

.form-section:hover {
  border-color: #d0d7de;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  background: #f8f9fa;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e8eaed;
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon-small {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  box-shadow: 0 2px 6px rgba(102, 126, 234, 0.25);
}

/* 表单样式 */
.info-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.info-form :deep(.el-form-item__label) {
  color: #4a5568;
  font-weight: 600;
  font-size: 14px;
  padding-right: 20px;
}

.info-form :deep(.el-input__inner),
.password-form-content :deep(.el-input__inner) {
  border-radius: 10px;
  border: 1.5px solid #e2e8f0;
  transition: all 0.3s ease;
  font-size: 14px;
  line-height: 1.5;
  background: white;
  height: 44px;
}

.info-form :deep(.el-input__inner:hover),
.password-form-content :deep(.el-input__inner:hover) {
  border-color: #cbd5e0;
}

.info-form :deep(.el-input__inner:focus),
.password-form-content :deep(.el-input__inner:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

.info-form :deep(.el-input__prefix) {
  color: #909399;
  font-size: 16px;
  display: flex;
  align-items: center;
  padding-left: 10px;
}

.disabled-input :deep(.el-input__inner) {
  background: #f7fafc;
  border-color: #e2e8f0;
  color: #718096;
  cursor: not-allowed;
  opacity: 0.7;
}

.disabled-input :deep(.el-input-group__append) {
  background: #f7fafc;
  border-color: #e2e8f0;
  color: #a0aec0;
  cursor: not-allowed;
}

/* 密码强度 */
.password-strength {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
}

.strength-label {
  font-size: 12px;
  color: #606266;
  font-weight: 500;
  min-width: 60px;
}

.strength-bar {
  flex: 1;
  max-width: 200px;
}

.strength-bar :deep(.el-progress-bar__outer) {
  background: #f0f0f0;
  border-radius: 10px;
  height: 8px;
}

.strength-bar :deep(.el-progress-bar__inner) {
  border-radius: 10px;
  transition: all 0.3s ease;
}

.strength-text {
  font-size: 12px;
  font-weight: 600;
  min-width: 20px;
  text-align: right;
}

.password-tips {
  margin-top: 20px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
  font-size: 12px;
  color: #606266;
  line-height: 1.6;
}

.password-tips p {
  margin: 4px 0;
}

/* 操作按钮 */
.form-actions {
  display: flex;
  gap: 12px;
  padding-top: 24px;
  border-top: 1px solid #e8eaed;
  margin-top: 24px;
}

.save-btn,
.change-btn {
  border-radius: 10px;
  font-weight: 600;
  padding: 12px 28px;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  min-width: 130px;
  justify-content: center;
  font-size: 14px;
}

.save-btn {
  background: linear-gradient(135deg, #67C23A, #85ce61);
  border: none;
  color: white;
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.25);
}

.save-btn:hover:not(.is-disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(103, 194, 58, 0.35);
}

.reset-btn {
  border-radius: 10px;
  border-color: #e2e8f0;
  background: white;
  color: #718096;
  font-weight: 600;
  padding: 12px 28px;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  min-width: 110px;
  justify-content: center;
  font-size: 14px;
}

.reset-btn:hover {
  border-color: #667eea;
  color: #667eea;
  background: #f8f9ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.change-btn {
  background: linear-gradient(135deg, #E6A23C, #ebb563);
  border: none;
  color: white;
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.25);
}

.change-btn:hover:not(.is-disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(230, 162, 60, 0.35);
}

.reset-password-btn {
  border-radius: 10px;
  border-color: #e2e8f0;
  background: white;
  color: #718096;
  font-weight: 600;
  padding: 12px 28px;
  transition: all 0.3s ease;
  font-size: 14px;
}

.reset-password-btn:hover {
  border-color: #E6A23C;
  color: #E6A23C;
  background: #fffbeb;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.15);
}

/* 安全设置 */
.security-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 24px;
}

.security-item {
  padding: 24px;
  background: #fafbfc;
  border-radius: 12px;
  border: 1px solid #e8eaed;
  transition: all 0.3s ease;
}

.security-item:hover {
  border-color: #d0d7de;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  background: #f8f9fa;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #e8eaed;
}

.item-title {
  font-size: 16px;
  font-weight: 700;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  gap: 10px;
}

.item-title i {
  font-size: 18px;
  color: #667eea;
}

.toggle-btn,
.view-more-btn {
  color: #667eea;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 6px;
  transition: all 0.3s ease;
  font-size: 13px;
}

.toggle-btn:hover,
.view-more-btn:hover {
  background: rgba(102, 126, 234, 0.1);
  transform: translateY(-1px);
}

.security-options {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.option-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: white;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
}

.option-item:hover {
  border-color: #cbd5e0;
  background: #f8f9ff;
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.08);
}

.option-content {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
}

.option-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  flex-shrink: 0;
}

.option-info {
  flex: 1;
  min-width: 0;
}

.option-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.option-desc {
  font-size: 12px;
  color: #8c8c8c;
}

.option-status {
  flex-shrink: 0;
}

.security-action-btn {
  padding: 6px 16px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 13px;
  transition: all 0.3s ease;
}

.security-action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.logout-btn {
  width: 100%;
  padding: 12px 24px;
  border-radius: 10px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.logout-btn:hover {
  background: #f56c6c;
  color: white;
  border-color: #f56c6c;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
}

/* 登录历史 */
.login-history {
  min-height: 100px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
  transition: all 0.3s ease;
  cursor: pointer;
}

.history-item:hover {
  border-color: #409EFF;
  background: #f0f9ff;
  transform: translateX(4px);
}

.history-info {
  flex: 1;
  min-width: 0;
}

.history-time {
  font-size: 12px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.history-device {
  font-size: 11px;
  color: #606266;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty-history {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px 20px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px dashed #e9ecef;
  color: #909399;
  text-align: center;
}

.empty-history i {
  font-size: 40px;
  margin-bottom: 8px;
  opacity: 0.6;
}

.empty-history p {
  font-size: 13px;
  margin: 0;
}

/* 安全操作 */
.security-actions {
  display: flex;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  margin-top: 20px;
  flex-wrap: wrap;
}

.logout-others-btn,
.export-data-btn {
  border-radius: 8px;
  font-weight: 500;
  padding: 8px 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
  flex: 1;
  min-width: 150px;
  justify-content: center;
}

.logout-others-btn:hover {
  border-color: #F56C6C;
  color: #F56C6C;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.2);
}

.export-data-btn:hover {
  border-color: #E6A23C;
  color: #E6A23C;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.2);
}

/* 头像对话框 */
.avatar-dialog {
  border-radius: 12px;
  overflow: hidden;
}

.avatar-dialog-content {
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.avatar-preview-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.avatar-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: white;
  border-radius: 8px;
  border: 1px solid #dcdfe6;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.preview-image {
  object-fit: cover;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.avatar-controls {
  width: 100%;
  max-width: 400px;
}

.control-group {
  margin-bottom: 15px;
}

.control-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 8px;
}

.size-slider {
  width: 100%;
}

.avatar-upload-section {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.upload-methods {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.avatar-uploader :deep(.el-upload) {
  width: 100%;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  background: #fafafa;
  cursor: pointer;
  transition: all 0.3s ease;
  display: block;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: #409EFF;
  background: #f0f9ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
  transition: all 0.3s ease;
}

.avatar-uploader :deep(.el-upload:hover) .upload-area {
  transform: translateY(-2px);
}

.upload-area i {
  font-size: 40px;
  color: #999;
  margin-bottom: 12px;
  transition: all 0.3s ease;
}

.avatar-uploader :deep(.el-upload:hover) .upload-area i {
  color: #409EFF;
  transform: scale(1.1);
}

.upload-text {
  font-size: 16px;
  font-weight: 600;
  color: #666;
  margin-bottom: 8px;
}

.upload-hint {
  font-size: 12px;
  color: #999;
  line-height: 1.4;
}

.avatar-templates {
  margin-top: 20px;
}

.templates-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.templates-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.template-item {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid #f0f0f0;
}

.template-item:hover {
  border-color: #409EFF;
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.template-image {
  width: 100%;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
  transition: all 0.3s ease;
}

/* 对话框底部 */
.dialog-footer {
  padding: 20px;
  border-top: 1px solid #ebeef5;
  text-align: center;
  display: flex;
  justify-content: center;
  gap: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-person-page {
    padding: 12px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: stretch;
  }
  
  .user-status {
    align-items: flex-start;
  }
  
  .avatar-container {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }
  
  .form-actions,
  .security-actions {
    flex-direction: column;
  }
  
  .save-btn,
  .reset-btn,
  .change-btn,
  .reset-password-btn,
  .logout-btn {
    width: 100%;
  }
  
  .templates-list {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 20px;
  }
  
  .templates-list {
    grid-template-columns: 1fr;
  }
}
</style>