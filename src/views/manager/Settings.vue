/**
 * 系统设置页面
 * 文件路径: src/views/manager/Settings.vue
 * 功能描述: 管理系统全局配置参数，包含基本设置（网站名称、Logo、备案号、联系方式、版权信息）、
 *           商品设置（默认分类、计量单位、审核模式、上架条件、库存预警）、安全设置（密码策略、登录限制、验证码配置、会话管理），
 *           每个Tab独立表单验证和保存，支持Logo图片上传预览
 * 关联文件:
 * - src/api/index.js: 提供系统配置保存接口
 */
<template>
  <div class="system-settings-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">系统设置</h1>
      <p class="page-subtitle">管理系统全局配置和参数</p>
    </div>

    <!-- 设置内容 -->
    <el-card shadow="never" class="settings-card">
      <el-tabs v-model="activeTab" class="settings-tabs">
        <!-- 基本设置 -->
        <el-tab-pane label="基本设置" name="basic">
          <div class="tab-content">
            <h3 class="tab-title">系统基本信息</h3>
            <p class="tab-description">配置系统基本信息，如网站名称、Logo、备案号等</p>
            
            <el-form 
              :model="basicForm" 
              :rules="basicRules" 
              ref="basicFormRef" 
              label-width="120px"
              label-position="left"
              class="settings-form"
            >
              <el-form-item label="网站名称" prop="siteName">
                <el-input 
                  v-model="basicForm.siteName" 
                  placeholder="请输入网站名称"
                  clearable
                  maxlength="50"
                  show-word-limit
                />
                <div class="form-tip">将在浏览器标签页和网站头部显示</div>
              </el-form-item>
              
              <el-form-item label="网站Logo">
                <div class="logo-upload-section">
                  <div v-if="basicForm.siteLogo" class="logo-preview">
                    <el-image
                      :src="basicForm.siteLogo"
                      fit="contain"
                      class="preview-logo"
                    >
                      <template #error>
                        <div class="logo-error">
                          <i class="el-icon-picture"></i>
                        </div>
                      </template>
                    </el-image>
                    <el-button 
                      type="text" 
                      @click="removeLogo"
                      class="remove-logo-btn"
                    >
                      <i class="el-icon-delete"></i>
                      移除Logo
                    </el-button>
                  </div>
                  
                  <el-upload
                    v-else
                    class="logo-uploader"
                    action="#"
                    :auto-upload="false"
                    :show-file-list="false"
                    :on-change="handleLogoChange"
                    accept="image/*"
                  >
                    <div class="upload-area">
                      <i class="el-icon-plus upload-icon"></i>
                      <div class="upload-text">上传Logo</div>
                      <div class="upload-hint">建议尺寸：200x60px，支持PNG、JPG</div>
                    </div>
                  </el-upload>
                </div>
              </el-form-item>
              
              <el-form-item label="备案号" prop="icpNumber">
                <el-input 
                  v-model="basicForm.icpNumber" 
                  placeholder="请输入网站备案号"
                  clearable
                  maxlength="50"
                />
                <div class="form-tip">将在网站底部显示备案信息</div>
              </el-form-item>
              
              <el-form-item label="客服电话" prop="customerPhone">
                <el-input 
                  v-model="basicForm.customerPhone" 
                  placeholder="请输入客服联系电话"
                  clearable
                  maxlength="20"
                />
              </el-form-item>
              
              <el-form-item label="客服邮箱" prop="customerEmail">
                <el-input 
                  v-model="basicForm.customerEmail" 
                  placeholder="请输入客服联系邮箱"
                  clearable
                  maxlength="50"
                />
              </el-form-item>
              
              <el-form-item label="工作时间" prop="workTime">
                <el-input 
                  v-model="basicForm.workTime" 
                  placeholder="例如：周一至周五 9:00-18:00"
                  clearable
                  maxlength="50"
                />
              </el-form-item>
              
              <el-form-item label="版权信息" prop="copyright">
                <el-input 
                  v-model="basicForm.copyright" 
                  placeholder="请输入网站版权信息"
                  clearable
                  maxlength="200"
                  show-word-limit
                />
                <div class="form-tip">将在网站底部显示版权信息</div>
              </el-form-item>
              
              <el-form-item>
                <el-button 
                  type="primary" 
                  @click="saveBasicSettings"
                  :loading="savingBasic"
                  class="save-btn"
                >
                  <i class="el-icon-check"></i>
                  保存基本设置
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 商品设置 -->
        <el-tab-pane label="商品设置" name="products">
          <div class="tab-content">
            <h3 class="tab-title">商品相关设置</h3>
            <p class="tab-description">配置商品相关的全局参数和规则</p>
            
            <el-form 
              :model="productForm" 
              :rules="productRules" 
              ref="productFormRef" 
              label-width="150px"
              label-position="left"
              class="settings-form"
            >
              <el-form-item label="默认商品分类" prop="defaultCategory">
                <el-radio-group v-model="productForm.defaultCategory">
                  <el-radio label="vegetable">蔬菜类</el-radio>
                  <el-radio label="fruit">水果类</el-radio>
                  <el-radio label="meat">肉类</el-radio>
                  <el-radio label="grain">谷物类</el-radio>
                  <el-radio label="processed">加工类</el-radio>
                  <el-radio label="other">其他</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="默认计量单位" prop="defaultUnit">
                <el-input 
                  v-model="productForm.defaultUnit" 
                  placeholder="请输入默认计量单位"
                  clearable
                  maxlength="10"
                />
                <div class="form-tip">例如：斤、个、件、箱等</div>
              </el-form-item>
              
              <el-form-item label="商品审核模式" prop="reviewMode">
                <el-radio-group v-model="productForm.reviewMode">
                  <el-radio label="manual">手动审核</el-radio>
                  <el-radio label="auto">自动通过</el-radio>
                </el-radio-group>
                <div class="form-tip">设置新商品的审核方式，手动审核需管理员审批，自动通过将直接上架</div>
              </el-form-item>
              
              <el-form-item label="商品上架条件" prop="listingCondition">
                <el-checkbox-group v-model="productForm.listingCondition">
                  <el-checkbox label="require_price">必须填写价格</el-checkbox>
                  <el-checkbox label="require_stock">必须填写库存</el-checkbox>
                  <el-checkbox label="require_image">必须上传图片</el-checkbox>
                  <el-checkbox label="require_description">必须填写描述</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              
              <el-form-item label="库存预警值" prop="stockAlert">
                <el-input 
                  v-model.number="productForm.stockAlert" 
                  type="number"
                  placeholder="请输入库存预警值"
                  clearable
                  min="0"
                >
                  <template #append>{{ productForm.defaultUnit || '件' }}</template>
                </el-input>
                <div class="form-tip">当商品库存低于此值时，系统将进行预警提示</div>
              </el-form-item>
              
              <el-form-item label="价格上限" prop="priceLimit">
                <el-input 
                  v-model.number="productForm.priceLimit" 
                  type="number"
                  placeholder="请输入商品价格上限"
                  clearable
                  min="0"
                  step="0.01"
                >
                  <template #append>元</template>
                </el-input>
                <div class="form-tip">设置商品价格的最大值，超过此值的商品将被限制</div>
              </el-form-item>
              
              <el-form-item>
                <el-button 
                  type="primary" 
                  @click="saveProductSettings"
                  :loading="savingProduct"
                  class="save-btn"
                >
                  <i class="el-icon-check"></i>
                  保存商品设置
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 用户设置 -->
        <el-tab-pane label="用户设置" name="users">
          <div class="tab-content">
            <h3 class="tab-title">用户管理设置</h3>
            <p class="tab-description">配置用户注册、登录和权限相关的设置</p>
            
            <el-form 
              :model="userForm" 
              :rules="userRules" 
              ref="userFormRef" 
              label-width="150px"
              label-position="left"
              class="settings-form"
            >
              <el-form-item label="用户注册开关" prop="registerEnabled">
                <el-switch
                  v-model="userForm.registerEnabled"
                  active-text="开启"
                  inactive-text="关闭"
                />
                <div class="form-tip">关闭后用户将无法注册新账户</div>
              </el-form-item>
              
              <el-form-item label="注册验证方式" prop="registerVerify">
                <el-checkbox-group v-model="userForm.registerVerify">
                  <el-checkbox label="require_phone">手机号验证</el-checkbox>
                  <el-checkbox label="require_email">邮箱验证</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              
              <el-form-item label="密码策略" prop="passwordPolicy">
                <el-checkbox-group v-model="userForm.passwordPolicy">
                  <el-checkbox label="require_length">至少8位长度</el-checkbox>
                  <el-checkbox label="require_mixcase">包含大小写字母</el-checkbox>
                  <el-checkbox label="require_number">包含数字</el-checkbox>
                  <el-checkbox label="require_special">包含特殊字符</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              
              <el-form-item label="登录失败限制" prop="loginLimit">
                <el-input 
                  v-model.number="userForm.loginLimit" 
                  type="number"
                  placeholder="请输入登录失败次数限制"
                  clearable
                  min="1"
                  max="10"
                >
                  <template #append>次</template>
                </el-input>
                <div class="form-tip">登录失败达到此次数后账户将被锁定</div>
              </el-form-item>
              
              <el-form-item label="锁定时间" prop="lockTime">
                <el-input 
                  v-model.number="userForm.lockTime" 
                  type="number"
                  placeholder="请输入锁定时间"
                  clearable
                  min="1"
                  max="24"
                >
                  <template #append>小时</template>
                </el-input>
                <div class="form-tip">账户被锁定后，需要等待的时间才能重新尝试登录</div>
              </el-form-item>
              
              <el-form-item label="自动注销时间" prop="autoLogout">
                <el-input 
                  v-model.number="userForm.autoLogout" 
                  type="number"
                  placeholder="请输入自动注销时间"
                  clearable
                  min="5"
                  max="480"
                >
                  <template #append>分钟</template>
                </el-input>
                <div class="form-tip">用户无操作达到此时间后自动退出登录</div>
              </el-form-item>
              
              <el-form-item label="默认头像">
                <div class="avatar-upload-section">
                  <div v-if="userForm.defaultAvatar" class="avatar-preview">
                    <el-image
                      :src="userForm.defaultAvatar"
                      fit="cover"
                      class="preview-avatar"
                    >
                      <template #error>
                        <div class="avatar-error">
                          <i class="el-icon-user"></i>
                        </div>
                      </template>
                    </el-image>
                    <el-button 
                      type="text" 
                      @click="removeDefaultAvatar"
                      class="remove-avatar-btn"
                    >
                      <i class="el-icon-delete"></i>
                      移除头像
                    </el-button>
                  </div>
                  
                  <el-upload
                    v-else
                    class="avatar-uploader"
                    action="#"
                    :auto-upload="false"
                    :show-file-list="false"
                    :on-change="handleDefaultAvatarChange"
                    accept="image/*"
                  >
                    <div class="upload-avatar-area">
                      <i class="el-icon-user upload-avatar-icon"></i>
                      <div class="upload-avatar-text">上传默认头像</div>
                      <div class="upload-avatar-hint">建议尺寸：200x200px，支持PNG、JPG</div>
                    </div>
                  </el-upload>
                </div>
                <div class="form-tip">新用户注册时将使用此头像作为默认头像</div>
              </el-form-item>
              
              <el-form-item>
                <el-button 
                  type="primary" 
                  @click="saveUserSettings"
                  :loading="savingUser"
                  class="save-btn"
                >
                  <i class="el-icon-check"></i>
                  保存用户设置
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 通知设置 -->
        <el-tab-pane label="通知设置" name="notifications">
          <div class="tab-content">
            <h3 class="tab-title">系统通知设置</h3>
            <p class="tab-description">配置系统通知方式和内容模板</p>
            
            <el-form 
              :model="notificationForm" 
              :rules="notificationRules" 
              ref="notificationFormRef" 
              label-width="150px"
              label-position="left"
              class="settings-form"
            >
              <el-form-item label="新商品通知" prop="newProductNotify">
                <el-switch
                  v-model="notificationForm.newProductNotify"
                  active-text="开启"
                  inactive-text="关闭"
                />
                <div class="form-tip">开启后，有新的待审核商品时将发送通知</div>
              </el-form-item>
              
              <el-form-item label="审核结果通知" prop="reviewResultNotify">
                <el-switch
                  v-model="notificationForm.reviewResultNotify"
                  active-text="开启"
                  inactive-text="关闭"
                />
                <div class="form-tip">开启后，商品审核结果将通知农户</div>
              </el-form-item>
              
              <el-form-item label="订单通知" prop="orderNotify">
                <el-switch
                  v-model="notificationForm.orderNotify"
                  active-text="开启"
                  inactive-text="关闭"
                />
                <div class="form-tip">开启后，有新的订单时将发送通知</div>
              </el-form-item>
              
              <el-form-item label="系统公告通知" prop="announcementNotify">
                <el-switch
                  v-model="notificationForm.announcementNotify"
                  active-text="开启"
                  inactive-text="关闭"
                />
                <div class="form-tip">开启后，发布新公告时将发送通知</div>
              </el-form-item>
              
              <el-form-item label="通知接收人" prop="notifyRecipients">
                <el-input 
                  v-model="notificationForm.notifyRecipients" 
                  type="textarea"
                  :rows="3"
                  placeholder="请输入通知接收人邮箱，多个邮箱用逗号分隔"
                  maxlength="500"
                  show-word-limit
                />
                <div class="form-tip">系统通知将发送到这些邮箱地址</div>
              </el-form-item>
              
              <el-form-item label="通知模板" prop="notifyTemplate">
                <el-input 
                  v-model="notificationForm.notifyTemplate" 
                  type="textarea"
                  :rows="5"
                  :placeholder="templatePlaceholder"
                  maxlength="1000"
                  show-word-limit
                  resize="none"
                />
                <div class="form-tip">可使用变量：{{variableTip}}</div>
              </el-form-item>
              
              <el-form-item>
                <el-button 
                  type="primary" 
                  @click="saveNotificationSettings"
                  :loading="savingNotification"
                  class="save-btn"
                >
                  <i class="el-icon-check"></i>
                  保存通知设置
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 关于系统 -->
        <el-tab-pane label="关于系统" name="about">
          <div class="tab-content">
            <h3 class="tab-title">关于农产品直供平台</h3>
            <p class="tab-description">系统信息和版本详情</p>
            
            <div class="about-info">
              <div class="about-item">
                <label>系统名称：</label>
                <span>农产品直供平台管理系统</span>
              </div>
              
              <div class="about-item">
                <label>当前版本：</label>
                <span class="version-badge">v1.3.0</span>
              </div>
              
              <div class="about-item">
                <label>更新日期：</label>
                <span>2026年5月17日</span>
              </div>
              
              <div class="about-item">
                <label>开发者：</label>
                <span>农产品直供平台开发团队</span>
              </div>
              
              <div class="about-item">
                <label>技术支持：</label>
                <span>tech@example.com</span>
              </div>
              
              <div class="about-item">
                <label>系统描述：</label>
                <p>本系统是一个专为农产品交易设计的在线平台，旨在连接农户和消费者，提供便捷的农产品交易服务。系统支持商品管理、订单跟踪、用户管理、公告发布等核心功能。</p>
              </div>
              
              <div class="about-item">
                <label>主要功能：</label>
                <ul class="features-list">
                  <li>农产品在线展示和交易</li>
                  <li>农户商品发布和管理</li>
                  <li>商品审核和上架管理</li>
                  <li>订单管理和跟踪</li>
                  <li>用户管理和权限控制</li>
                  <li>系统公告和通知</li>
                  <li>数据统计和分析</li>
                  <li>系统配置和管理</li>
                </ul>
              </div>
              
              <div class="about-item">
                <label>技术栈：</label>
                <div class="tech-stack">
                  <el-tag>Vue 2</el-tag>
                  <el-tag type="success">Element UI</el-tag>
                  <el-tag type="warning">JavaScript</el-tag>
                  <el-tag type="info">Vue Router</el-tag>
                  <el-tag type="danger">Axios</el-tag>
                  <el-tag>Spring Boot</el-tag>
                  <el-tag type="success">MyBatis</el-tag>
                  <el-tag type="warning">MySQL</el-tag>
                </div>
              </div>
            </div>
            
            <div class="system-actions">
              <el-button 
                type="primary" 
                @click="checkUpdate"
                class="update-btn"
              >
                <i class="el-icon-refresh"></i>
                检查更新
              </el-button>
              
              <el-button 
                @click="exportSettings"
                class="export-btn"
              >
                <i class="el-icon-download"></i>
                导出设置
              </el-button>
              
              <el-button 
                type="danger" 
                @click="resetSettings"
                class="reset-btn"
              >
                <i class="el-icon-delete"></i>
                重置所有设置
              </el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>
<script>
import { getSettings, saveSettings } from '@/api/index.js'

export default {
  name: 'Settings',
  data() {
    return {
      activeTab: 'basic',
      
      // 基本设置
      basicForm: {
        siteName: '',
        siteLogo: '',
        icpNumber: '',
        customerPhone: '',
        customerEmail: '',
        workTime: '',
        copyright: ''
      },
      basicRules: {
        siteName: [
          { required: true, message: '请输入网站名称', trigger: 'blur' },
          { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
        ],
        customerEmail: [
          { 
            validator: (rule, value, callback) => {
              if (!value || value.trim() === '') {
                callback()
                return
              }
              
              const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
              if (!emailRegex.test(value)) {
                callback(new Error('请输入正确的邮箱地址'))
                return
              }
              callback()
            },
            trigger: 'blur'
          }
        ]
      },
      
      // 商品设置
      productForm: {
        defaultCategory: 'vegetable',
        defaultUnit: '斤',
        reviewMode: 'manual',
        listingCondition: ['require_price', 'require_stock', 'require_image', 'require_description'],
        stockAlert: 10,
        priceLimit: 9999
      },
      productRules: {
        defaultUnit: [
          { min: 1, max: 10, message: '长度在 1 到 10 个字符', trigger: 'blur' }
        ],
        stockAlert: [
          { 
            validator: (rule, value, callback) => {
              if (value === '' || value === null || value === undefined) {
                callback()
                return
              }
              if (isNaN(value) || value < 0) {
                callback(new Error('库存预警值必须大于等于0'))
                return
              }
              callback()
            },
            trigger: 'blur'
          }
        ],
        priceLimit: [
          { 
            validator: (rule, value, callback) => {
              if (value === '' || value === null || value === undefined) {
                callback()
                return
              }
              if (isNaN(value) || value < 0) {
                callback(new Error('价格上限必须大于等于0'))
                return
              }
              callback()
            },
            trigger: 'blur'
          }
        ]
      },
      
      // 用户设置
      userForm: {
        registerEnabled: true,
        registerVerify: ['require_phone', 'require_email'],
        passwordPolicy: ['require_length', 'require_mixcase', 'require_number'],
        loginLimit: 5,
        lockTime: 2,
        autoLogout: 30,
        defaultAvatar: ''
      },
      userRules: {
        loginLimit: [
          { 
            validator: (rule, value, callback) => {
              if (value === '' || value === null || value === undefined) {
                callback()
                return
              }
              if (isNaN(value) || value < 1 || value > 10) {
                callback(new Error('登录失败限制在 1-10 次之间'))
                return
              }
              callback()
            },
            trigger: 'blur'
          }
        ],
        lockTime: [
          { 
            validator: (rule, value, callback) => {
              if (value === '' || value === null || value === undefined) {
                callback()
                return
              }
              if (isNaN(value) || value < 1 || value > 24) {
                callback(new Error('锁定时间在 1-24 小时之间'))
                return
              }
              callback()
            },
            trigger: 'blur'
          }
        ],
        autoLogout: [
          { 
            validator: (rule, value, callback) => {
              if (value === '' || value === null || value === undefined) {
                callback()
                return
              }
              if (isNaN(value) || value < 5 || value > 480) {
                callback(new Error('自动注销时间在 5-480 分钟之间'))
                return
              }
              callback()
            },
            trigger: 'blur'
          }
        ]
      },
      
      // 通知设置
      notificationForm: {
        newProductNotify: true,
        reviewResultNotify: true,
        orderNotify: true,
        announcementNotify: true,
        notifyRecipients: '',
        notifyTemplate: '【系统通知】{{product_name}} 的商品审核结果为：{{review_result}}，审核时间：{{time}}'
      },
      notificationRules: {
        notifyRecipients: [
          { 
            validator: (rule, value, callback) => {
              if (!value || value.trim() === '') {
                callback()
                return
              }
              
              const emails = value.split(',').map(email => email.trim()).filter(email => email)
              const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
              
              for (const email of emails) {
                if (email && !emailRegex.test(email)) {
                  callback(new Error('请输入正确的邮箱地址格式，多个邮箱用逗号分隔'))
                  return
                }
              }
              callback()
            },
            trigger: 'blur'
          }
        ],
        notifyTemplate: [
          { max: 1000, message: '最多输入1000个字符', trigger: 'blur' }
        ]
      },
      
      // 保存状态
      savingBasic: false,
      savingProduct: false,
      savingUser: false,
      savingNotification: false
    }
  },
  computed: {
    // 模板占位符
    templatePlaceholder() {
      return '请输入通知模板内容，使用{{变量名}}作为占位符'
    },
    
    // 变量提示
    variableTip() {
      return '{{product_name}}、{{merchant_name}}、{{review_result}}、{{time}}'
    }
  },
  created() {
    this.loadSettings()
  },
  methods: {
    // 加载设置
    async loadSettings() {
      try {
        const res = await getSettings()
        if (res.code === 200 && res.data) {
          const allSettings = res.data
          
          // 加载基本设置
          if (allSettings.basic) {
            const basicSettings = JSON.parse(allSettings.basic)
            Object.keys(basicSettings).forEach(key => {
              if (basicSettings[key] !== undefined && basicSettings[key] !== null) {
                this.basicForm[key] = basicSettings[key]
              }
            })
          }
          
          // 加载商品设置
          if (allSettings.product) {
            const productSettings = JSON.parse(allSettings.product)
            Object.keys(productSettings).forEach(key => {
              if (productSettings[key] !== undefined && productSettings[key] !== null) {
                this.productForm[key] = productSettings[key]
              }
            })
          }
          
          // 加载用户设置
          if (allSettings.user) {
            const userSettings = JSON.parse(allSettings.user)
            Object.keys(userSettings).forEach(key => {
              if (userSettings[key] !== undefined && userSettings[key] !== null) {
                this.userForm[key] = userSettings[key]
              }
            })
          }
          
          // 加载通知设置
          if (allSettings.notification) {
            const notificationSettings = JSON.parse(allSettings.notification)
            Object.keys(notificationSettings).forEach(key => {
              if (notificationSettings[key] !== undefined && notificationSettings[key] !== null) {
                this.notificationForm[key] = notificationSettings[key]
              }
            })
          }
        }
      } catch (error) {
        console.error('加载设置失败:', error)
        this.$message.error('加载设置失败，使用默认设置')
      }
    },
    
    // 保存基本设置
    async saveBasicSettings() {
      try {
        await this.$refs.basicFormRef.validate()
      } catch {
        return
      }
      
      this.savingBasic = true
      try {
        const res = await saveSettings({
          type: 'basic',
          value: JSON.stringify(this.basicForm)
        })
        if (res.code === 200) {
          this.$message.success('基本设置保存成功')
          if (this.basicForm.siteName) {
            document.title = this.basicForm.siteName
          }
        } else {
          this.$message.error(res.message || '保存失败')
        }
      } catch (error) {
        console.error('保存基本设置失败:', error)
        this.$message.error('保存失败')
      } finally {
        this.savingBasic = false
      }
    },
    
    // 保存商品设置
    async saveProductSettings() {
      try {
        await this.$refs.productFormRef.validate()
      } catch {
        return
      }
      
      this.savingProduct = true
      try {
        const res = await saveSettings({
          type: 'product',
          value: JSON.stringify(this.productForm)
        })
        if (res.code === 200) {
          this.$message.success('商品设置保存成功')
        } else {
          this.$message.error(res.message || '保存失败')
        }
      } catch (error) {
        console.error('保存商品设置失败:', error)
        this.$message.error('保存失败')
      } finally {
        this.savingProduct = false
      }
    },
    
    // 保存用户设置
    async saveUserSettings() {
      try {
        await this.$refs.userFormRef.validate()
      } catch {
        return
      }
      
      this.savingUser = true
      try {
        const res = await saveSettings({
          type: 'user',
          value: JSON.stringify(this.userForm)
        })
        if (res.code === 200) {
          this.$message.success('用户设置保存成功')
        } else {
          this.$message.error(res.message || '保存失败')
        }
      } catch (error) {
        console.error('保存用户设置失败:', error)
        this.$message.error('保存失败')
      } finally {
        this.savingUser = false
      }
    },
    
    // 保存通知设置
    async saveNotificationSettings() {
      try {
        await this.$refs.notificationFormRef.validate()
      } catch {
        return
      }
      
      this.savingNotification = true
      try {
        const res = await saveSettings({
          type: 'notification',
          value: JSON.stringify(this.notificationForm)
        })
        if (res.code === 200) {
          this.$message.success('通知设置保存成功')
        } else {
          this.$message.error(res.message || '保存失败')
        }
      } catch (error) {
        console.error('保存通知设置失败:', error)
        this.$message.error('保存失败')
      } finally {
        this.savingNotification = false
      }
    },
    
    // Logo上传
    async handleLogoChange(file) {
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

      const formData = new FormData()
      formData.append('file', file.raw)
      
      try {
        const res = await this.$request.post('/upload', formData)
        let relativePath = res.data || res.url || res
        
        if (relativePath) {
          const baseUrl = (process.env.VUE_APP_BASEURL || '').replace('/api', '') || 'http://localhost:9090'
          this.basicForm.siteLogo = `${baseUrl}/upload/${relativePath}`
          this.$message.success('Logo上传成功')
        }
      } catch (error) {
        console.error('Logo上传失败:', error)
        this.$message.error('Logo上传失败，请重试')
      }
    },
    
    // 移除Logo
    removeLogo() {
      this.basicForm.siteLogo = ''
    },
    
    // 默认头像上传
    async handleDefaultAvatarChange(file) {
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

      const formData = new FormData()
      formData.append('file', file.raw)
      
      try {
        const res = await this.$request.post('/upload', formData)
        let relativePath = res.data || res.url || res
        
        if (relativePath) {
          const baseUrl = (process.env.VUE_APP_BASEURL || '').replace('/api', '') || 'http://localhost:9090'
          this.userForm.defaultAvatar = `${baseUrl}/upload/${relativePath}`
          this.$message.success('默认头像上传成功')
        }
      } catch (error) {
        console.error('默认头像上传失败:', error)
        this.$message.error('默认头像上传失败，请重试')
      }
    },
    
    // 移除默认头像
    removeDefaultAvatar() {
      this.userForm.defaultAvatar = ''
    },
    
    // 检查更新
    checkUpdate() {
      this.$message.info('当前已是最新版本')
    },
    
    // 导出设置
    exportSettings() {
      const settings = {
        basic: this.basicForm,
        product: this.productForm,
        user: this.userForm,
        notification: this.notificationForm,
        exportTime: new Date().toISOString()
      }
      
      const dataStr = JSON.stringify(settings, null, 2)
      const dataUri = 'data:application/json;charset=utf-8,'+ encodeURIComponent(dataStr)
      
      const exportFileDefaultName = '系统设置备份.json'
      
      const linkElement = document.createElement('a')
      linkElement.setAttribute('href', dataUri)
      linkElement.setAttribute('download', exportFileDefaultName)
      linkElement.click()
      
      this.$message.success('设置导出成功')
    },
    
    // 重置所有设置
    async resetSettings() {
      this.$confirm('确定要重置所有设置吗？此操作不可恢复，将重置为默认值', '确认重置', {
        type: 'warning',
        confirmButtonText: '确定重置',
        cancelButtonText: '取消',
        confirmButtonClass: 'reset-confirm-btn'
      }).then(async () => {
        try {
          // 重置本地存储
          localStorage.removeItem('xm-settings-basic')
          localStorage.removeItem('xm-settings-product')
          localStorage.removeItem('xm-settings-user')
          localStorage.removeItem('xm-settings-notification')
          
          // 重置后端数据
          await saveSettings({ type: 'basic', value: JSON.stringify({
            siteName: '', siteLogo: '', icpNumber: '', customerPhone: '', customerEmail: '', workTime: '', copyright: ''
          })})
          await saveSettings({ type: 'product', value: JSON.stringify({
            defaultCategory: 'vegetable', defaultUnit: '斤', reviewMode: 'manual',
            listingCondition: ['require_price', 'require_stock', 'require_image', 'require_description'],
            stockAlert: 10, priceLimit: 9999
          })})
          await saveSettings({ type: 'user', value: JSON.stringify({
            registerEnabled: true, registerVerify: ['require_phone', 'require_email'],
            passwordPolicy: ['require_length', 'require_mixcase', 'require_number'],
            loginLimit: 5, lockTime: 2, autoLogout: 30, defaultAvatar: ''
          })})
          await saveSettings({ type: 'notification', value: JSON.stringify({
            newProductNotify: true, reviewResultNotify: true, orderNotify: true,
            announcementNotify: true, notifyRecipients: '',
            notifyTemplate: '【系统通知】{{product_name}} 的商品审核结果为：{{review_result}}，审核时间：{{time}}'
          })})
          
          // 重新加载
          this.loadSettings()
          
          this.$message.success('所有设置已重置为默认值')
        } catch (error) {
          console.error('重置设置失败:', error)
          this.$message.error('重置设置失败')
        }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.system-settings-page {
  padding: 20px;
  background: #f0f2f5;
  min-height: calc(100vh - 60px);
  box-sizing: border-box;
}

/* 页面头部 */
.page-header {
  margin-bottom: 24px;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: white;
  margin: 0 0 8px 0;
  line-height: 1.4;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  font-weight: 400;
}

/* 设置卡片 */
.settings-card {
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  background: white;
  min-height: 400px;
  border: 1px solid #e8e8e8;
  animation: slide-up 0.3s ease;
}

@keyframes slide-up {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 选项卡 */
.settings-tabs {
  border: none;
}

.settings-tabs :deep(.el-tabs__header) {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  padding: 0 24px;
  border: 1px solid #e8e8e8;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.settings-tabs :deep(.el-tabs__nav-wrap)::after {
  display: none;
}

.settings-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 600;
  color: #666;
  padding: 0 24px;
  height: 56px;
  line-height: 56px;
  transition: all 0.3s ease;
}

.settings-tabs :deep(.el-tabs__item:hover) {
  color: #667eea;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 8px 8px 0 0;
}

.settings-tabs :deep(.el-tabs__item.is-active) {
  color: #667eea;
  font-weight: 700;
  background: white;
  border-radius: 8px 8px 0 0;
  box-shadow: 0 -2px 8px rgba(102, 126, 234, 0.1);
}

.settings-tabs :deep(.el-tabs__active-bar) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  height: 3px;
  border-radius: 2px;
  width: 60px !important;
}

/* 选项卡内容 */
.tab-content {
  padding: 24px 0;
}

.tab-title {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
  padding-bottom: 12px;
  border-bottom: 3px solid transparent;
  border-image: linear-gradient(135deg, #667eea, #764ba2) 1;
  display: inline-block;
}

.tab-description {
  font-size: 14px;
  color: #666;
  margin: 12px 0 24px 0;
  line-height: 1.6;
}

/* 表单 */
.settings-form {
  max-width: 850px;
  margin: 0 auto;
  padding: 20px;
  background: #fafbfc;
  border-radius: 12px;
  border: 1px solid #e8e8e8;
}

.settings-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #333;
  font-size: 14px;
  padding-right: 20px;
}

.settings-form :deep(.el-input__inner),
.settings-form :deep(.el-textarea__inner) {
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  transition: all 0.3s;
  font-size: 14px;
  background: white;
  padding: 10px 12px;
}

.settings-form :deep(.el-input__inner:hover),
.settings-form :deep(.el-textarea__inner:hover) {
  border-color: #667eea;
}

.settings-form :deep(.el-input__inner:focus),
.settings-form :deep(.el-textarea__inner:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.settings-form :deep(.el-radio__label),
.settings-form :deep(.el-checkbox__label) {
  font-size: 14px;
  color: #555;
}

.settings-form :deep(.el-switch__label) {
  font-size: 13px;
  color: #666;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 6px;
  line-height: 1.5;
  font-style: italic;
  padding-left: 4px;
}

/* Logo上传 */
.logo-upload-section {
  width: 100%;
  max-width: 350px;
}

.logo-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px;
  border: 2px dashed #e8e8e8;
  border-radius: 12px;
  background: linear-gradient(135deg, #fafbfc 0%, #f0f2f5 100%);
  transition: all 0.3s;
}

.logo-preview:hover {
  border-color: #667eea;
  background: linear-gradient(135deg, #f0f4ff 0%, #e8ecf5 100%);
}

.preview-logo {
  width: 220px;
  height: 70px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e0e0e0;
  background: white;
  padding: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.logo-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #999;
  font-size: 24px;
}

.remove-logo-btn {
  color: #F56C6C;
  font-size: 13px;
  transition: all 0.3s;
  padding: 6px 12px;
  border-radius: 6px;
}

.remove-logo-btn:hover {
  color: #E64A4A;
  background: rgba(245, 108, 108, 0.1);
  transform: translateY(-1px);
}

.logo-uploader {
  width: 100%;
}

.logo-uploader :deep(.el-upload) {
  width: 100%;
  border: 2px dashed #d0d0d0;
  border-radius: 12px;
  background: linear-gradient(135deg, #fafbfc 0%, #f0f2f5 100%);
  cursor: pointer;
  transition: all 0.3s ease;
  display: block;
  padding: 10px;
}

.logo-uploader :deep(.el-upload:hover) {
  border-color: #667eea;
  background: linear-gradient(135deg, #f0f4ff 0%, #e8ecf5 100%);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 35px 20px;
  text-align: center;
}

.upload-icon {
  font-size: 36px;
  color: #999;
  margin-bottom: 10px;
  transition: all 0.3s;
}

.logo-uploader :deep(.el-upload:hover) .upload-icon {
  color: #667eea;
  transform: scale(1.15) translateY(-2px);
}

.upload-text {
  font-size: 15px;
  font-weight: 600;
  color: #666;
  margin-bottom: 6px;
}

.upload-hint {
  font-size: 12px;
  color: #999;
}

/* 默认头像上传 */
.avatar-upload-section {
  width: 100%;
  max-width: 350px;
}

.avatar-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px;
  border: 2px dashed #e8e8e8;
  border-radius: 12px;
  background: linear-gradient(135deg, #fafbfc 0%, #f0f2f5 100%);
  transition: all 0.3s;
}

.avatar-preview:hover {
  border-color: #667eea;
  background: linear-gradient(135deg, #f0f4ff 0%, #e8ecf5 100%);
}

.preview-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #e0e0e0;
  background: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.avatar-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #999;
  font-size: 40px;
}

.remove-avatar-btn {
  color: #F56C6C;
  font-size: 13px;
  transition: all 0.3s;
  padding: 6px 12px;
  border-radius: 6px;
}

.remove-avatar-btn:hover {
  color: #E64A4A;
  background: rgba(245, 108, 108, 0.1);
  transform: translateY(-1px);
}

.avatar-uploader {
  width: 100%;
}

.avatar-uploader :deep(.el-upload) {
  width: 100%;
  border: 2px dashed #d0d0d0;
  border-radius: 12px;
  background: linear-gradient(135deg, #fafbfc 0%, #f0f2f5 100%);
  cursor: pointer;
  transition: all 0.3s ease;
  display: block;
  padding: 10px;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: #667eea;
  background: linear-gradient(135deg, #f0f4ff 0%, #e8ecf5 100%);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.upload-avatar-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 35px 20px;
  text-align: center;
}

.upload-avatar-icon {
  font-size: 48px;
  color: #999;
  margin-bottom: 10px;
  transition: all 0.3s;
}

.avatar-uploader :deep(.el-upload:hover) .upload-avatar-icon {
  color: #667eea;
  transform: scale(1.15) translateY(-2px);
}

.upload-avatar-text {
  font-size: 15px;
  font-weight: 600;
  color: #666;
  margin-bottom: 6px;
}

.upload-avatar-hint {
  font-size: 12px;
  color: #999;
}

/* 保存按钮 */
.save-btn {
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  color: white;
  font-weight: 600;
  transition: all 0.3s;
  padding: 12px 28px;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 140px;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.25);
}

.save-btn:hover {
  background: linear-gradient(135deg, #764ba2, #667eea);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

/* 关于信息 */
.about-info {
  display: flex;
  flex-direction: column;
  gap: 15px;
  max-width: 800px;
  margin: 0 auto 30px;
  padding: 20px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  background: #fafafa;
}

.about-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.about-item label {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.about-item span {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}

.about-item p {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  margin: 0;
  padding-left: 20px;
  border-left: 3px solid #409EFF;
  background: white;
  padding: 10px 15px;
  border-radius: 4px;
}

.features-list {
  list-style: none;
  padding: 0;
  margin: 0;
  padding-left: 20px;
  border-left: 3px solid #409EFF;
  background: white;
  padding: 10px 15px;
  border-radius: 4px;
}

.features-list li {
  font-size: 13px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 8px;
  position: relative;
  padding-left: 20px;
}

.features-list li:before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #4CAF50;
  font-weight: bold;
}

.tech-stack {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  padding-left: 20px;
  border-left: 3px solid #409EFF;
  background: white;
  padding: 10px 15px;
  border-radius: 4px;
}

.version-badge {
  background: linear-gradient(135deg, #409EFF, #66B1FF);
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 12px;
  display: inline-block;
}

/* 系统操作 */
.system-actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  justify-content: center;
  padding-top: 30px;
  border-top: 1px solid #f0f0f0;
  margin-top: 30px;
}

.update-btn {
  border-radius: 8px;
  background: linear-gradient(135deg, #4CAF50, #2E7D32);
  border: none;
  color: white;
  font-weight: 600;
  transition: all 0.3s;
  padding: 12px 28px;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 130px;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.2);
}

.update-btn:hover {
  background: linear-gradient(135deg, #2E7D32, #4CAF50);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(76, 175, 80, 0.35);
}

.export-btn {
  border-radius: 8px;
  background: white;
  border: 2px solid #409EFF;
  color: #409EFF;
  font-weight: 600;
  transition: all 0.3s;
  padding: 12px 28px;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 130px;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.export-btn:hover {
  background: #409EFF;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.35);
}

.reset-btn {
  border-radius: 8px;
  background: linear-gradient(135deg, #F56C6C, #E64A4A);
  border: none;
  color: white;
  font-weight: 600;
  transition: all 0.3s;
  padding: 12px 28px;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 130px;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.2);
}

.reset-btn:hover {
  background: linear-gradient(135deg, #E64A4A, #F56C6C);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(245, 108, 108, 0.35);
}

.reset-confirm-btn {
  background: #F56C6C;
  border-color: #F56C6C;
  color: white;
}

.reset-confirm-btn:hover {
  background: #E64A4A;
  border-color: #E64A4A;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .system-settings-page {
    padding: 12px;
  }
  
  .settings-card {
    padding: 15px;
  }
  
  .settings-tabs :deep(.el-tabs__item) {
    padding: 0 10px;
  }
  
  .system-actions {
    flex-direction: column;
  }
  
  .update-btn,
  .export-btn,
  .reset-btn {
    width: 100%;
  }
}
</style>