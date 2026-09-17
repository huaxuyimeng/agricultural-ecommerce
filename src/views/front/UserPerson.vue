/**
 * 用户个人中心页面（新版）
 * 文件路径: src/views/front/UserPerson.vue
 * 功能描述: 用户个人信息与数据概览，左侧用户信息卡片（头像、用户名、角色、邮箱、手机号、注册时间），
 *           左侧菜单导航（个人信息/修改密码/收货地址/我的订单/我的收藏/优惠券），
 *           右侧内容区根据菜单切换展示对应Tab内容，支持头像上传和资料编辑
 * 关联文件:
 * - src/api/index.js: 提供用户信息、订单、收藏、优惠券接口
 * - src/store/index.js: 提供当前登录用户状态
 * - src/views/front/MyOrders.vue: 我的订单页面
 * - src/views/front/Favorites.vue: 收藏夹页面
 * - src/views/front/MyCoupons.vue: 我的优惠券页面
 */
<template>
  <div class="user-center">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <i class="el-icon-user"></i> 个人中心
        </h1>
        <el-button @click="$router.back()" size="small" plain icon="el-icon-arrow-left">返回</el-button>
      </div>
    </div>

    <div class="center-container">
      <!-- 左侧用户信息卡片 -->
      <div class="user-sidebar">
        <el-card class="user-card" shadow="never">
          <div class="user-profile">
            <div class="avatar-section">
              <div class="avatar-wrapper">
                <el-avatar 
                  :size="80" 
                  :src="getAvatarUrl(user.avatar)"
                  @error="handleAvatarError"
                  class="user-avatar"
                >
                  <i class="el-icon-user-solid"></i>
                </el-avatar>
                <el-upload
                  class="avatar-upload"
                  action=""
                  :auto-upload="false"
                  :show-file-list="false"
                  :on-change="handleAvatarChange"
                >
                  <div class="avatar-edit">
                    <i class="el-icon-camera"></i>
                  </div>
                </el-upload>
              </div>
              <div class="user-info">
                <h2 class="user-name">{{ user.name || user.username }}</h2>
                <div class="user-role">
                  <el-tag v-if="user.role === 'USER'" type="success" size="small" effect="dark">普通用户</el-tag>
                  <el-tag v-else-if="user.role === 'MERCHANT'" type="warning" size="small" effect="dark">商家用户</el-tag>
                  <el-tag v-else type="danger" size="small" effect="dark">管理员</el-tag>
                </div>
                <p class="user-desc">{{ user.description || '这个人很懒，什么都没写~' }}</p>
              </div>
            </div>

            <div class="user-stats">
              <div class="stat-item">
                <div class="stat-value">¥{{ formatMoney(user.account || 0) }}</div>
                <div class="stat-label">账户余额</div>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item">
                <div class="stat-value">{{ formatShortDate(user.createTime) }}</div>
                <div class="stat-label">注册时间</div>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item">
                <div class="stat-value">{{ formatShortDate(user.lastLoginTime) }}</div>
                <div class="stat-label">最后登录</div>
              </div>
            </div>

            <div class="charge-btn-wrapper">
              <el-button type="primary" size="small" @click="showChargeDialog" class="charge-btn">
                <i class="el-icon-wallet"></i> 充值
              </el-button>
            </div>
          </div>

          <div class="quick-nav">
            <div class="nav-title">快捷操作</div>
            <div class="nav-grid">
              <div class="nav-item" @click="activeTab = 'orders'">
                <div class="nav-icon" style="background: linear-gradient(135deg, #ff9800 0%, #ff5722 100%);">
                  <i class="el-icon-s-order"></i>
                </div>
                <span class="nav-text">我的订单</span>
              </div>
              <div class="nav-item" @click="$router.push('/front/favorites')">
                <div class="nav-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
                  <i class="el-icon-star-on"></i>
                </div>
                <span class="nav-text">我的收藏</span>
              </div>
              <div class="nav-item" @click="$router.push('/front/my-coupons')">
                <div class="nav-icon" style="background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);">
                  <i class="el-icon-present"></i>
                </div>
                <span class="nav-text">我的优惠券</span>
              </div>
              <div class="nav-item" @click="activeTab = 'address'">
                <div class="nav-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
                  <i class="el-icon-location"></i>
                </div>
                <span class="nav-text">收货地址</span>
              </div>
              <div class="nav-item" @click="activeTab = 'password'">
                <div class="nav-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
                  <i class="el-icon-lock"></i>
                </div>
                <span class="nav-text">修改密码</span>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧内容区域 -->
      <div class="content-main">
        <el-card class="content-card" shadow="never">
          <el-tabs v-model="activeTab" class="user-tabs">
            <!-- 基本信息标签页 -->
            <el-tab-pane label="基本信息" name="basic">
              <div class="tab-header">
                <div class="tab-title">
                  <i class="el-icon-user"></i>
                  <span>基本信息</span>
                </div>
                <p class="tab-desc">完善您的个人信息，方便我们为您提供更好的服务</p>
              </div>
              
              <el-form 
                :model="userForm" 
                :rules="rules" 
                ref="basicForm"
                label-width="100px"
                class="info-form"
              >
                <div class="form-section">
                  <h3 class="section-title">账号信息</h3>
                  <el-row :gutter="24">
                    <el-col :span="12">
                      <el-form-item label="用户名">
                        <el-input v-model="userForm.username" disabled />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="真实姓名" prop="name">
                        <el-input v-model="userForm.name" placeholder="请输入真实姓名" />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  
                  <el-row :gutter="24">
                    <el-col :span="12">
                      <el-form-item label="手机号" prop="phone">
                        <el-input v-model="userForm.phone" placeholder="请输入手机号" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="邮箱" prop="email">
                        <el-input v-model="userForm.email" placeholder="请输入邮箱" />
                      </el-form-item>
                    </el-col>
                  </el-row>
                </div>

                <div class="form-section">
                  <h3 class="section-title">个人资料</h3>
                  <el-row :gutter="24">
                    <el-col :span="12">
                      <el-form-item label="性别">
                        <el-radio-group v-model="userForm.gender">
                          <el-radio label="男">男</el-radio>
                          <el-radio label="女">女</el-radio>
                        </el-radio-group>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="生日">
                        <el-date-picker
                          v-model="userForm.birthday"
                          type="date"
                          placeholder="选择生日"
                          style="width: 100%"
                          value-format="yyyy-MM-dd"
                        />
                      </el-form-item>
                    </el-col>
                  </el-row>

                  <el-row :gutter="24">
                    <el-col :span="12">
                      <el-form-item label="学号">
                        <el-input v-model="userForm.studentId" placeholder="请输入学号" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="账户状态">
                        <el-tag :type="userForm.status === 1 ? 'success' : 'danger'" size="medium" effect="dark">
                          {{ userForm.status === 1 ? '正常' : '禁用' }}
                        </el-tag>
                      </el-form-item>
                    </el-col>
                  </el-row>

                  <el-form-item label="个人地址">
                    <el-input 
                      v-model="userForm.address" 
                      type="textarea"
                      :rows="2"
                      placeholder="请输入收货地址"
                      maxlength="100"
                      show-word-limit
                    />
                  </el-form-item>

                  <el-form-item label="个人简介">
                    <el-input 
                      v-model="userForm.description" 
                      type="textarea"
                      :rows="3"
                      placeholder="请输入个人简介"
                      maxlength="255"
                      show-word-limit
                    />
                  </el-form-item>
                </div>

                <div class="form-actions">
                  <el-button type="primary" @click="updateBasicInfo" :loading="basicLoading" icon="el-icon-check">保存修改</el-button>
                  <el-button @click="resetBasicForm" icon="el-icon-refresh">重置</el-button>
                </div>
              </el-form>
            </el-tab-pane>

            <!-- 修改密码标签页 -->
            <el-tab-pane label="修改密码" name="password">
              <div class="tab-header">
                <div class="tab-title">
                  <i class="el-icon-lock"></i>
                  <span>修改密码</span>
                </div>
                <p class="tab-desc">定期修改密码可以提高账户安全性</p>
              </div>
              
              <el-form 
                :model="passwordForm" 
                :rules="passwordRules"
                ref="passwordFormRef"
                label-width="120px"
                class="password-form"
              >
                <div class="form-section">
                  <el-form-item label="原密码" prop="oldPassword">
                    <el-input 
                      v-model="passwordForm.oldPassword" 
                      type="password" 
                      placeholder="请输入原密码"
                      show-password
                    />
                  </el-form-item>

                  <el-form-item label="新密码" prop="newPassword">
                    <el-input 
                      v-model="passwordForm.newPassword" 
                      type="password" 
                      placeholder="请输入新密码（6位以上）"
                      show-password
                    />
                  </el-form-item>

                  <el-form-item label="确认密码" prop="confirmPassword">
                    <el-input 
                      v-model="passwordForm.confirmPassword" 
                      type="password" 
                      placeholder="请再次输入新密码"
                      show-password
                    />
                  </el-form-item>
                </div>

                <div class="form-actions">
                  <el-button type="primary" @click="changePassword" :loading="passwordLoading" icon="el-icon-key">修改密码</el-button>
                  <el-button @click="resetPasswordForm" icon="el-icon-refresh">重置</el-button>
                </div>
              </el-form>
            </el-tab-pane>

            <!-- 收货地址标签页 -->
            <el-tab-pane label="收货地址" name="address">
              <div class="tab-header">
                <div class="tab-title">
                  <i class="el-icon-location"></i>
                  <span>收货地址</span>
                </div>
                <p class="tab-desc">管理您的收货地址，方便购物结算</p>
              </div>

              <div class="address-actions">
                <el-button type="primary" @click="showAddressDialog()" icon="el-icon-plus">添加新地址</el-button>
              </div>

              <div class="address-list" v-if="addresses.length > 0">
                <el-card v-for="item in addresses" :key="item.id" class="address-card" shadow="hover">
                  <div class="address-content">
                    <div class="address-main">
                      <div class="address-header">
                        <span class="receiver-name">{{ item.receiverName }}</span>
                        <span class="receiver-phone">{{ item.phone }}</span>
                        <el-tag v-if="item.isDefault" type="success" size="mini" class="default-tag">默认</el-tag>
                        <el-tag v-if="item.label" size="mini" class="label-tag">{{ item.label }}</el-tag>
                      </div>
                      <div class="address-detail">
                        {{ item.province }} {{ item.city }} {{ item.district }} {{ item.detailAddress }}
                      </div>
                    </div>
                    <div class="address-actions">
                      <el-button type="text" size="small" @click="showAddressDialog(item)">编辑</el-button>
                      <el-button type="text" size="small" @click="setDefaultAddress(item.id)" v-if="!item.isDefault">设为默认</el-button>
                      <el-button type="text" size="small" style="color: #f56c6c" @click="deleteAddress(item.id)">删除</el-button>
                    </div>
                  </div>
                </el-card>
              </div>
              
              <div v-else class="empty-address">
                <i class="el-icon-location-outline"></i>
                <p>暂无收货地址，点击上方按钮添加</p>
              </div>
            </el-tab-pane>

            <!-- 我的订单标签页 -->
            <el-tab-pane label="我的订单" name="orders">
              <div class="order-module">
                <div class="module-header">
                  <div class="header-left">
                    <div class="header-icon">
                      <i class="el-icon-s-order"></i>
                    </div>
                    <span class="header-title">我的订单</span>
                  </div>
                  <el-button type="primary" size="small" class="view-all-btn" @click="viewAllOrders">
                    查看全部 <i class="el-icon-arrow-right"></i>
                  </el-button>
                </div>

                <div class="order-list" v-if="filteredOrders.length > 0">
                  <div 
                    v-for="order in filteredOrders.slice(0, 3)" 
                    :key="order.id" 
                    class="order-item"
                  >
                    <div class="order-item-header">
                      <span class="order-no">订单号: {{ order.orderId }}</span>
                      <el-tag :type="getOrderStatusType(order.status)" size="mini" effect="light">
                        {{ getOrderStatusText(order.status) }}
                      </el-tag>
                    </div>
                    <div class="order-item-body">
                      <div class="product-info">
                        <div class="product-image">
                          <img v-if="getFirstProduct(order).productImage" :src="getProductImage(getFirstProduct(order).productImage)" />
                          <div v-else class="image-placeholder">
                            <i class="el-icon-goods"></i>
                          </div>
                        </div>
                        <div class="product-detail">
                          <div class="product-name">{{ getFirstProduct(order).productName || getFirstProduct(order).name || '商品' }}</div>
                          <div class="product-meta">
                            <span class="price">¥{{ (getFirstProduct(order).price || 0).toFixed(2) }}</span>
                            <span class="quantity">x{{ getFirstProduct(order).quantity || getFirstProduct(order).count || 1 }}</span>
                          </div>
                        </div>
                      </div>
                      <div class="order-total">
                        <span class="total-label">合计</span>
                        <span class="total-value">¥{{ (order.totalAmount || order.total || 0).toFixed(2) }}</span>
                      </div>
                    </div>
                    <div class="order-item-footer">
                      <div class="action-links">
                        <el-button type="text" size="small" @click="viewOrderDetail(order)">查看</el-button>
                        <template v-if="!isMerchant">
                          <el-button type="text" size="small" class="cancel-btn" v-if="order.status === 'PENDING' || order.status === 'pending'" @click="cancelOrder(order)">取消</el-button>
                          <el-button type="text" size="small" class="pay-btn" v-if="order.status === 'PENDING' || order.status === 'pending'" @click="payOrder(order)">支付</el-button>
                          <el-button type="text" size="small" class="refund-btn" v-if="order.status === 'PAID' || order.status === 'paid' || order.status === 'PROCESSING' || order.status === 'processing'" @click="handleRefund(order)">退款</el-button>
                          <el-button type="text" size="small" class="confirm-btn" v-if="order.status === 'SHIPPING' || order.status === 'shipping' || order.status === 'SHIPPED' || order.status === 'shipped'" @click="confirmReceive(order)">确认收货</el-button>
                          <el-button type="text" size="small" class="reorder-btn" v-if="order.status === 'DELIVERED' || order.status === 'COMPLETED' || order.status === 'delivered' || order.status === 'completed' || order.status === 'REFUNDED' || order.status === 'refunded' || order.status === 'CANCELLED' || order.status === 'cancelled'" @click="handleReorder(order)">再来一单</el-button>
                        </template>
                        <template v-else>
                          <el-button type="text" size="small" class="ship-btn" v-if="order.status === 'PAID' || order.status === 'paid' || order.status === 'PROCESSING' || order.status === 'processing'" @click="handleShip(order)">发货</el-button>
                        </template>
                      </div>
                    </div>
                  </div>
                </div>

                <div v-else class="empty-orders">
                  <i class="el-icon-document"></i>
                  <p>暂无订单记录</p>
                </div>

                <div class="order-stats-bar">
                  <div class="stat-box">
                    <div class="stat-number">{{ orders.length }}</div>
                    <div class="stat-text">全部订单</div>
                  </div>
                  <div class="stat-divider"></div>
                  <div class="stat-box">
                    <div class="stat-number">{{ pendingOrders.length }}</div>
                    <div class="stat-text">待处理</div>
                  </div>
                  <div class="stat-divider"></div>
                  <div class="stat-box">
                    <div class="stat-number">{{ completedOrders.length }}</div>
                    <div class="stat-text">已完成</div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- 我的收藏标签页 -->
            <el-tab-pane label="我的收藏" name="favorites">
              <div class="tab-header">
                <div class="tab-title">
                  <i class="el-icon-star-on"></i>
                  <span>我的收藏</span>
                </div>
                <p class="tab-desc">您收藏的商品会在这里显示</p>
              </div>

              <div class="favorites-grid" v-if="favorites.length > 0">
                <div 
                  v-for="item in favorites" 
                  :key="item.id" 
                  class="favorite-card"
                  @click="goProduct(item.targetId)"
                >
                  <div class="favorite-image">
                    <img v-if="item.product && item.product.image" :src="getProductImage(item.product.image)" :alt="item.targetName" />
                    <div v-else class="image-placeholder">
                      <i class="el-icon-picture"></i>
                    </div>
                    <div class="favorite-overlay">
                      <el-button type="danger" size="mini" icon="el-icon-delete" @click.stop="cancelFavorite(item.id)">取消收藏</el-button>
                    </div>
                  </div>
                  <div class="favorite-info">
                    <div class="favorite-name">{{ item.targetName }}</div>
                    <div class="favorite-price" v-if="item.product">
                      <span class="price-symbol">¥</span>
                      <span class="price-value">{{ item.product.price }}</span>
                      <span class="price-unit">/{{ item.product.unit }}</span>
                    </div>
                    <div class="favorite-meta" v-if="item.product">
                      <span class="meta-item"><i class="el-icon-view"></i> {{ item.product.views || 0 }}</span>
                      <span class="meta-item"><i class="el-icon-goods"></i> {{ item.product.sales || 0 }}已售</span>
                    </div>
                  </div>
                </div>
              </div>
              
              <div v-else class="empty-favorites">
                <i class="el-icon-star-off"></i>
                <p>暂无收藏商品</p>
                <el-button type="primary" size="small" @click="$router.push('/front/products')">去逛逛</el-button>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </div>
    </div>

    <!-- 充值对话框 -->
    <el-dialog title="账户充值" :visible.sync="chargeDialogVisible" width="400px" :append-to-body="true">
      <div class="charge-dialog">
        <div class="charge-amount">
          <div class="amount-label">充值金额</div>
          <el-input-number 
            v-model="chargeForm.amount" 
            :min="1" 
            :max="10000"
            :step="10"
            controls-position="right"
            style="width: 100%"
          />
        </div>
        
        <div class="charge-summary">
          <div class="summary-item">
            <span>当前余额</span>
            <span>¥{{ (user.account || 0).toFixed(2) }}</span>
          </div>
          <div class="summary-item">
            <span>充值金额</span>
            <span>+¥{{ chargeForm.amount.toFixed(2) }}</span>
          </div>
          <div class="summary-divider"></div>
          <div class="summary-item total">
            <span>充值后余额</span>
            <span style="color: #ff9800; font-weight: bold">¥{{ ((user.account || 0) + chargeForm.amount).toFixed(2) }}</span>
          </div>
        </div>
      </div>
      <div slot="footer">
        <el-button @click="chargeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="chargeAccount" :loading="chargeLoading">确认充值</el-button>
      </div>
    </el-dialog>

    <!-- 地址编辑对话框 -->
    <el-dialog :title="addressForm.id ? '编辑地址' : '添加地址'" :visible.sync="addressDialogVisible" width="500px" :append-to-body="true">
      <el-form :model="addressForm" :rules="addressRules" ref="addressFormRef" label-width="80px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="收货人" prop="receiverName">
              <el-input v-model="addressForm.receiverName" placeholder="收货人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="addressForm.phone" placeholder="收货人手机号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="省份" prop="province">
              <el-input v-model="addressForm.province" placeholder="省份" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="城市" prop="city">
              <el-input v-model="addressForm.city" placeholder="城市" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="区县" prop="district">
              <el-input v-model="addressForm.district" placeholder="区县" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input v-model="addressForm.detailAddress" placeholder="街道、门牌号等详细信息" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="addressForm.label" placeholder="如：家、公司、学校" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="addressForm.isDefault" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAddress">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getCurrentUser,
  updateUser,
  changePassword,
  chargeAccount as chargeAccountApi,
  getAddressList,
  addAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress as setDefaultApi,
  getOrderPage,
  getMerchantOrders,
  getUserFavorites,
  removeFavorite
} from '@/api'
import request from '@/utils/request'

export default {
  name: 'UserPerson',
  data() {
    const validatePassword = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }

    return {
      user: {},
      userForm: {},
      activeTab: 'basic',
      basicLoading: false,
      passwordLoading: false,
      chargeLoading: false,
      
      chargeDialogVisible: false,
      chargeForm: {
        amount: 100
      },

      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },

      addresses: [],
      addressDialogVisible: false,
      addressForm: {
        isDefault: false
      },

      orders: [],
      orderSearch: '',

      favorites: [],

      rules: {
        name: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        email: [
          { pattern: /^[\w.-]+@[\w.-]+\.\w+$/, message: '请输入正确的邮箱', trigger: 'blur' }
        ]
      },
      passwordRules: {
        oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码至少6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认密码', trigger: 'blur' },
          { validator: validatePassword, trigger: 'blur' }
        ]
      },
      addressRules: {
        receiverName: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
        city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
        district: [{ required: true, message: '请输入区县', trigger: 'blur' }],
        detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
      }
    }
  },
  computed: {
    isMerchant() {
      try {
        const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
        return user.role === 'MERCHANT'
      } catch (e) {
        return false
      }
    },
    filteredOrders() {
      if (!this.orderSearch) return this.orders
      return this.orders.filter(o => o.orderId.includes(this.orderSearch))
    },
    pendingOrders() {
      return this.orders.filter(o => ['PENDING', 'pending', 'PAID', 'paid', 'PROCESSING', 'processing'].includes(o.status))
    },
    completedOrders() {
      return this.orders.filter(o => ['DELIVERED', 'delivered', 'COMPLETED', 'completed', 'CANCELLED', 'cancelled', 'REFUNDED', 'refunded'].includes(o.status))
    }
  },
  mounted() {
    this.loadUserInfo()
    this.loadAddresses()
    this.loadOrders()
    this.loadFavorites()
  },
  methods: {
    viewAllOrders() {
      if (this.isMerchant) {
        this.$router.push('/front/merchant/orders')
      } else {
        this.$router.push('/front/my-orders')
      }
    },

    async loadUserInfo() {
      try {
        const res = await getCurrentUser()
        this.user = res.data || res
        this.userForm = { ...this.user }
        localStorage.setItem('xm-user', JSON.stringify(this.user))
        sessionStorage.setItem('xm-user', JSON.stringify(this.user))
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    },

    async updateBasicInfo() {
      this.$refs.basicForm.validate(async (valid) => {
        if (!valid) return
        this.basicLoading = true
        try {
          await updateUser(this.userForm.id, this.userForm)
          const freshUserRes = await getCurrentUser()
          const freshUser = freshUserRes.data || freshUserRes
          this.user = { ...this.user, ...freshUser }
          this.userForm = { ...this.userForm, ...freshUser }
          localStorage.setItem('xm-user', JSON.stringify(this.user))
          sessionStorage.setItem('xm-user', JSON.stringify(this.user))
          window.dispatchEvent(new CustomEvent('xm-user-updated', { 
            detail: { user: this.user },
            bubbles: true,
            cancelable: true
          }))
          this.$message.success('保存成功')
        } catch (error) {
          this.$message.error(error.message || '保存失败')
        } finally {
          this.basicLoading = false
        }
      })
    },

    async changePassword() {
      this.$refs.passwordFormRef.validate(async (valid) => {
        if (!valid) return
        this.passwordLoading = true
        try {
          await changePassword({
            oldPassword: this.passwordForm.oldPassword,
            newPassword: this.passwordForm.newPassword
          })
          this.$message.success('密码修改成功')
          this.resetPasswordForm()
          this.activeTab = 'basic'
        } catch (error) {
          this.$message.error(error.message || '密码修改失败')
        } finally {
          this.passwordLoading = false
        }
      })
    },

    resetPasswordForm() {
      this.passwordForm = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
      this.$refs.passwordFormRef && this.$refs.passwordFormRef.clearValidate()
    },

    showChargeDialog() {
      this.chargeForm.amount = 100
      this.chargeDialogVisible = true
    },

    async chargeAccount() {
      this.chargeLoading = true
      try {
        await chargeAccountApi(this.chargeForm.amount)
        await this.loadUserInfo()
        window.dispatchEvent(new CustomEvent('xm-user-updated', { 
          detail: { user: this.user },
          bubbles: true,
          cancelable: true
        }))
        this.$message.success('充值成功')
        this.chargeDialogVisible = false
      } catch (error) {
        this.$message.error(error.message || '充值失败')
      } finally {
        this.chargeLoading = false
      }
    },

    async loadAddresses() {
      try {
        const res = await getAddressList()
        this.addresses = res.data || res || []
      } catch (error) {
        console.warn('获取地址列表失败:', error)
        this.addresses = []
      }
    },

    showAddressDialog(row) {
      if (row) {
        this.addressForm = { ...row }
      } else {
        this.addressForm = { isDefault: false }
      }
      this.addressDialogVisible = true
    },

    async saveAddress() {
      this.$refs.addressFormRef.validate(async (valid) => {
        if (!valid) return
        try {
          if (this.addressForm.id) {
            await updateAddress(this.addressForm.id, this.addressForm)
          } else {
            await addAddress(this.addressForm)
          }
          this.$message.success('保存成功')
          this.addressDialogVisible = false
          this.loadAddresses()
        } catch (error) {
          this.$message.error(error.message || '保存失败')
        }
      })
    },

    async deleteAddress(id) {
      try {
        await this.$confirm('确定要删除该地址吗?', '提示', { type: 'warning' })
        await deleteAddress(id)
        this.$message.success('删除成功')
        this.loadAddresses()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(error.message || '删除失败')
        }
      }
    },

    async setDefaultAddress(id) {
      try {
        await setDefaultApi(id)
        this.$message.success('设置成功')
        this.loadAddresses()
      } catch (error) {
        this.$message.error(error.message || '设置失败')
      }
    },

    async loadOrders() {
      try {
        const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
        const isMerchant = user.role === 'MERCHANT'
        const res = isMerchant 
          ? await getMerchantOrders({ page: 1, pageSize: 50 })
          : await getOrderPage({ page: 1, pageSize: 50 })
        this.orders = (res.data && res.data.list) ? res.data.list : 
                      (res.data && res.data.records) ? res.data.records : 
                      (res && res.list) ? res.list : 
                      (res && res.records) ? res.records : []
      } catch (error) {
        console.warn('获取订单列表失败:', error)
        this.orders = []
      }
    },

    viewOrderDetail(order) {
      const id = order.id || order.orderId
      if (!id) return
      this.$router.push({ name: 'FrontOrderDetail', params: { orderId: String(id) } })
    },

    getFirstProduct(order) {
      if (order.products && order.products.length > 0) {
        return order.products[0]
      }
      return {}
    },

    async payOrder(order) {
      try {
        await this.$confirm('确认支付该订单吗？', '支付确认', { type: 'info' })
        await request({ url: `/order/pay/${order.id || order.orderId}`, method: 'post', data: { paymentMethod: 'alipay' } })
        this.$message.success('支付成功')
        this.loadOrders()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('支付失败')
        }
      }
    },

    handleRefund(order) {
      const id = order.id || order.orderId
      if (!id) return
      this.$router.push({ name: 'FrontOrderDetail', params: { orderId: String(id) }, query: { action: 'refund' } })
    },

    handleShip(order) {
      const id = order.id || order.orderId
      if (!id) return
      this.$router.push({ name: 'FrontOrderDetail', params: { orderId: String(id) }, query: { action: 'ship' } })
    },

    async confirmReceive(order) {
      try {
        await this.$confirm('确认已收到商品吗？', '确认收货', { type: 'info' })
        await request({ url: `/order/confirm/${order.id || order.orderId}`, method: 'post' })
        this.$message.success('已确认收货')
        this.loadOrders()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('确认收货失败')
        }
      }
    },

    handleReorder(order) {
      const id = order.id || order.orderId
      if (!id) return
      this.$router.push({ name: 'FrontOrderDetail', params: { orderId: String(id) }, query: { action: 'reorder' } })
    },

    async cancelOrder(order) {
      const orderId = order.id || order.orderId
      if (!orderId) return
      try {
        await this.$confirm('确定要取消该订单吗?', '提示', { type: 'warning' })
        await request({ url: `/order/cancel/${orderId}`, method: 'post' })
        this.$message.success('订单已取消')
        this.loadOrders()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(error.message || '取消失败')
        }
      }
    },

    getOrderStatusType(status) {
      const map = {
        'PENDING': 'warning',
        'pending': 'warning',
        'PAID': 'warning',
        'paid': 'warning',
        'PROCESSING': 'warning',
        'processing': 'warning',
        'SHIPPING': 'primary',
        'shipping': 'primary',
        'SHIPPED': 'primary',
        'shipped': 'primary',
        'DELIVERED': 'success',
        'delivered': 'success',
        'COMPLETED': 'success',
        'completed': 'success',
        'CANCELLED': 'info',
        'cancelled': 'info',
        'REFUNDED': 'info',
        'refunded': 'info'
      }
      return map[status] || 'info'
    },

    getOrderStatusText(status) {
      const map = {
        'PENDING': '待支付',
        'pending': '待支付',
        'PAID': '待发货',
        'paid': '待发货',
        'PROCESSING': '待发货',
        'processing': '待发货',
        'SHIPPING': '配送中',
        'shipping': '配送中',
        'SHIPPED': '配送中',
        'shipped': '配送中',
        'DELIVERED': '已完成',
        'delivered': '已完成',
        'COMPLETED': '已完成',
        'completed': '已完成',
        'CANCELLED': '已取消',
        'cancelled': '已取消',
        'REFUNDED': '已退款',
        'refunded': '已退款'
      }
      return map[status] || status
    },

    async loadFavorites() {
      try {
        const res = await getUserFavorites()
        this.favorites = res.data || res || []
      } catch (error) {
        console.warn('获取收藏列表失败:', error)
        this.favorites = []
      }
    },

    async cancelFavorite(id) {
      try {
        await removeFavorite(id, false)
        this.$message.success('取消收藏成功')
        this.loadFavorites()
      } catch (error) {
        this.$message.error(error.message || '操作失败')
      }
    },

    goProduct(id) {
      window.location.href = `/front/product/${id}`
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

      try {
        const formData = new FormData()
        formData.append('file', file.raw)
        
        const res = await request({ url: '/upload/avatar', method: 'post', data: formData })
        
        const avatarPath = res.data || res
        if (!avatarPath || typeof avatarPath !== 'string') {
          throw new Error('服务器返回数据格式错误')
        }
        
        await updateUser(this.user.id, { avatar: avatarPath })
        
        this.user.avatar = avatarPath
        this.userForm.avatar = avatarPath
        
        localStorage.setItem('xm-user', JSON.stringify(this.user))
        sessionStorage.setItem('xm-user', JSON.stringify(this.user))
        
        window.dispatchEvent(new CustomEvent('xm-user-updated', { 
          detail: { user: this.user },
          bubbles: true,
          cancelable: true
        }))
        
        this.$message.success('头像更新成功')
      } catch (error) {
        console.error('头像上传失败:', error)
        this.$message.error(error.message || '头像上传失败，请重试')
      }
    },

    getAvatarUrl(url) {
      if (!url || typeof url !== 'string') return ''
      if (url.startsWith('data:')) return url
      if (url.startsWith('http')) return url
      if (url.startsWith('/')) return url
      const baseUrl = (process.env.VUE_APP_BASEURL || '').replace('/api', '') || 'http://localhost:9090'
      return `${baseUrl}/upload/${url}`
    },

    getProductImage(url) {
      if (!url) return ''
      if (url.startsWith('http')) return url
      if (url.startsWith('data:')) return url
      if (url.startsWith('upload') || url.startsWith('upload/')) {
        const baseUrl = (process.env.VUE_APP_BASEURL || '').replace('/api', '') || 'http://localhost:9090'
        return `${baseUrl}/upload/${url.replace('upload/', '')}`
      }
      if (url.startsWith('imgs') || url.startsWith('/imgs')) {
        return url.startsWith('/') ? url : '/' + url
      }
      return `http://localhost:9090/${url}`
    },

    handleAvatarError() {
      // 头像加载失败时使用默认图标
    },

    formatDate(date) {
      if (!date) return '-'
      const d = new Date(date)
      return d.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
    },

    formatShortDate(date) {
      if (!date) return '-'
      const d = new Date(date)
      return d.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' }).replace(/\//g, '-')
    },

    formatMoney(amount) {
      if (amount >= 10000) {
        return (amount / 10000).toFixed(2) + '万'
      }
      return amount.toFixed(2)
    },

    resetBasicForm() {
      this.userForm = { ...this.user }
      this.$refs.basicForm && this.$refs.basicForm.clearValidate()
    }
  }
}
</script>

<style scoped>
.user-center {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8f0 100%);
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 8px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  color: #303133;
  font-weight: 600;
  display: flex;
  align-items: center;
}

.page-title i {
  margin-right: 12px;
  color: #ff9800;
  font-size: 28px;
}

.center-container {
  display: flex;
  gap: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.user-sidebar {
  width: 320px;
  flex-shrink: 0;
}

.user-card {
  border-radius: 16px;
  border: none;
  overflow: hidden;
}

.user-profile {
  padding: 24px 20px;
  background: linear-gradient(135deg, #ff9800 0%, #ff5722 100%);
  color: white;
  border-radius: 16px 16px 0 0;
  margin: -20px -20px 0;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.avatar-wrapper {
  position: relative;
  margin-bottom: 12px;
}

.user-avatar {
  border: 3px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.avatar-edit {
  position: absolute;
  bottom: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 12px;
}

.avatar-edit:hover {
  background: rgba(0, 0, 0, 0.9);
  transform: scale(1.1);
}

.user-info {
  text-align: center;
}

.user-name {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 600;
  color: white;
}

.user-role {
  margin-bottom: 8px;
}

.user-desc {
  margin: 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
  line-height: 1.5;
}

.user-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  margin: 0 10px;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
}

.stat-item {
  flex: 1;
  text-align: center;
  padding: 0 4px;
}

.stat-value {
  font-size: 14px;
  font-weight: 600;
  color: white;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stat-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.7);
}

.stat-divider {
  width: 1px;
  height: 36px;
  background: rgba(255, 255, 255, 0.2);
  flex-shrink: 0;
}

.charge-btn-wrapper {
  text-align: center;
  margin-top: 16px;
}

.charge-btn {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: white;
}

.charge-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
}

.quick-nav {
  padding: 20px;
}

.nav-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 14px;
}

.nav-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 14px 8px;
  background: #f8f9fb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.nav-item:hover {
  background: #fff9e6;
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(255, 152, 0, 0.15);
}

.nav-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  margin-bottom: 8px;
}

.nav-text {
  font-size: 12px;
  color: #606266;
  font-weight: 500;
}

.content-main {
  flex: 1;
  min-width: 0;
}

.content-card {
  border-radius: 16px;
  border: none;
  min-height: calc(100vh - 100px);
}

.user-tabs {
  padding: 0 8px;
}

.tab-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.tab-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.tab-title i {
  margin-right: 10px;
  color: #ff9800;
  font-size: 20px;
}

.tab-desc {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.form-section {
  margin-bottom: 28px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px;
  padding-bottom: 8px;
  border-bottom: 1px dashed #ebeef5;
}

.info-form,
.password-form {
  max-width: 700px;
}

.form-actions {
  display: flex;
  justify-content: flex-start;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  margin-left: 100px;
}

.address-actions {
  margin-bottom: 20px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.address-card {
  border-radius: 12px;
  border: 1px solid #ebeef5;
  transition: all 0.3s;
}

.address-card:hover {
  border-color: #ff9800;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.1);
}

.address-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.address-main {
  flex: 1;
}

.address-header {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
  gap: 10px;
}

.receiver-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.receiver-phone {
  color: #606266;
  font-size: 13px;
}

.default-tag {
  margin-left: 4px;
}

.label-tag {
  background: #f0f2f5;
  color: #909399;
  border: none;
}

.address-detail {
  color: #606266;
  line-height: 1.5;
  font-size: 13px;
}

.address-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.empty-address {
  text-align: center;
  padding: 50px 0;
  color: #c0c4cc;
}

.empty-address i {
  font-size: 56px;
  margin-bottom: 12px;
}

.empty-address p {
  margin: 0;
  font-size: 14px;
}

.order-module {
  padding: 0;
}

.module-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #fff9e6 0%, #fff3cd 100%);
  border-radius: 12px;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #ff9800 0%, #ff5722 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.view-all-btn {
  background: linear-gradient(135deg, #ff9800 0%, #ff5722 100%);
  border: none;
  color: #fff;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(255, 152, 0, 0.3);
}

.view-all-btn:hover {
  background: linear-gradient(135deg, #ff5722 0%, #e64a19 100%);
  color: #fff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.4);
}

.view-all-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(255, 152, 0, 0.3);
}

.view-all-btn i {
  margin-left: 4px;
  transition: transform 0.3s ease;
}

.view-all-btn:hover i {
  transform: translateX(3px);
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.order-item {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
}

.order-item:hover {
  border-color: #ff9800;
  box-shadow: 0 4px 16px rgba(255, 152, 0, 0.1);
}

.order-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #ebeef5;
}

.order-no {
  font-size: 13px;
  color: #606266;
  font-family: 'Courier New', monospace;
}

.order-item-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.product-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f7fa;
  flex-shrink: 0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-image .image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  font-size: 24px;
}

.product-detail {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-meta .price {
  color: #f56c6c;
  font-size: 14px;
  font-weight: 600;
}

.product-meta .quantity {
  color: #909399;
  font-size: 12px;
}

.order-total {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  flex-shrink: 0;
}

.total-label {
  font-size: 12px;
  color: #909399;
}

.total-value {
  font-size: 18px;
  color: #ff5722;
  font-weight: bold;
}

.order-item-footer {
  display: flex;
  justify-content: flex-end;
  padding: 10px 16px;
  border-top: 1px solid #ebeef5;
}

.action-links {
  display: flex;
  gap: 16px;
}

.cancel-btn {
  color: #f56c6c;
}

.cancel-btn:hover {
  color: #f56c6c;
}

.pay-btn {
  color: #409eff;
}

.pay-btn:hover {
  color: #409eff;
}

.refund-btn {
  color: #e6a23c;
}

.refund-btn:hover {
  color: #e6a23c;
}

.confirm-btn {
  color: #67c23a;
}

.confirm-btn:hover {
  color: #67c23a;
}

.reorder-btn {
  color: #909399;
}

.reorder-btn:hover {
  color: #909399;
}

.order-stats-bar {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 16px;
  background: #f8f9fb;
  border-radius: 12px;
}

.stat-box {
  text-align: center;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #ff9800;
  margin-bottom: 4px;
}

.stat-text {
  font-size: 12px;
  color: #909399;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: #ebeef5;
}

.empty-orders {
  text-align: center;
  padding: 40px 0;
  color: #c0c4cc;
}

.empty-orders i {
  font-size: 48px;
  margin-bottom: 10px;
}

.empty-orders p {
  margin: 0;
  font-size: 14px;
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 18px;
  margin-top: 18px;
}

.favorite-card {
  background: white;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.favorite-card:hover {
  border-color: #ff9800;
  box-shadow: 0 8px 24px rgba(255, 152, 0, 0.15);
  transform: translateY(-5px);
}

.favorite-image {
  position: relative;
  width: 100%;
  height: 160px;
  overflow: hidden;
  background: #f5f7fa;
}

.favorite-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.favorite-card:hover .favorite-image img {
  transform: scale(1.05);
}

.favorite-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.favorite-card:hover .favorite-overlay {
  opacity: 1;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  font-size: 36px;
}

.favorite-info {
  padding: 14px;
}

.favorite-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.favorite-price {
  margin-bottom: 6px;
}

.price-symbol {
  color: #f56c6c;
  font-size: 12px;
}

.price-value {
  color: #f56c6c;
  font-size: 16px;
  font-weight: bold;
}

.price-unit {
  color: #909399;
  font-size: 11px;
}

.favorite-meta {
  display: flex;
  gap: 14px;
}

.meta-item {
  font-size: 11px;
  color: #909399;
}

.meta-item i {
  margin-right: 3px;
}

.empty-favorites {
  text-align: center;
  padding: 50px 0;
  color: #c0c4cc;
}

.empty-favorites i {
  font-size: 56px;
  margin-bottom: 12px;
}

.empty-favorites p {
  margin: 0 0 16px;
  font-size: 14px;
}

.charge-dialog {
  padding: 10px 0;
}

.charge-amount {
  margin-bottom: 20px;
}

.amount-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.charge-summary {
  background: #f8f9fb;
  border-radius: 8px;
  padding: 14px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 13px;
  color: #606266;
}

.summary-item.total {
  font-size: 15px;
  color: #303133;
}

.summary-divider {
  height: 1px;
  background: #ebeef5;
  margin: 10px 0;
}

/* 主题色覆盖 */
.user-center ::v-deep .el-button--primary {
  background-color: #ff9800;
  border-color: #ff9800;
}

.user-center ::v-deep .el-button--primary:hover {
  background-color: #fb8c00;
  border-color: #fb8c00;
}

.user-center ::v-deep .el-button--primary:active {
  background-color: #f57c00;
  border-color: #f57c00;
}

.user-center ::v-deep .el-tabs__active-bar {
  background-color: #ff9800;
}

.user-center ::v-deep .el-tabs__item.is-active {
  color: #ff9800;
}

.user-center ::v-deep .el-tabs__item:hover {
  color: #ff9800;
}

.user-center ::v-deep .el-input__inner:focus {
  border-color: #ff9800;
}

.user-center ::v-deep .el-textarea__inner:focus {
  border-color: #ff9800;
}

.user-center ::v-deep .el-radio__input.is-checked .el-radio__inner {
  border-color: #ff9800;
  background: #ff9800;
}

.user-center ::v-deep .el-radio__input.is-checked + .el-radio__label {
  color: #ff9800;
}

.user-center ::v-deep .el-dialog__headerbtn .el-dialog__close:hover {
  color: #ff9800;
}

.user-center ::v-deep .el-table th {
  background-color: #fff9e6;
  color: #d48806;
}

.user-center ::v-deep .el-table--striped .el-table__body tr.el-table__row--striped td {
  background: #fffbf0;
}

/* 响应式 */
@media (max-width: 1200px) {
  .center-container {
    flex-direction: column;
  }
  
  .user-sidebar {
    width: 100%;
  }
  
  .user-card {
    max-width: 400px;
    margin: 0 auto;
  }
  
  .nav-grid {
    grid-template-columns: repeat(4, 1fr);
  }
  
  .favorites-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  }
}

@media (max-width: 992px) {
  .user-profile {
    padding: 20px 16px;
  }
  
  .user-name {
    font-size: 18px;
  }
  
  .nav-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .info-form,
  .password-form {
    max-width: 100%;
  }
  
  .order-item-body {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .order-total {
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .user-center {
    padding: 12px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .page-title i {
    font-size: 24px;
  }
  
  .user-stats {
    flex-direction: column;
    gap: 10px;
    padding: 14px 0;
  }
  
  .stat-divider {
    width: 100%;
    height: 1px;
  }
  
  .stat-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 6px 0;
  }
  
  .stat-label {
    margin-bottom: 0;
  }
  
  .nav-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .favorites-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 12px;
  }
  
  .favorite-image {
    height: 130px;
  }
  
  .form-actions {
    margin-left: 0;
    justify-content: center;
  }
  
  .address-content {
    flex-direction: column;
    gap: 10px;
  }
  
  .address-actions {
    align-self: flex-end;
  }
  
  .module-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .view-all-btn {
    align-self: flex-end;
  }
  
  .order-item-body {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .order-total {
    align-items: flex-start;
  }
  
  .order-item-footer {
    justify-content: center;
  }
  
  .order-stats-bar {
    flex-direction: column;
    gap: 12px;
  }
  
  .stat-divider {
    width: 100%;
    height: 1px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 18px;
  }
  
  .user-name {
    font-size: 16px;
  }
  
  .user-desc {
    font-size: 12px;
  }
  
  .nav-item {
    padding: 10px 6px;
  }
  
  .nav-icon {
    width: 34px;
    height: 34px;
    font-size: 16px;
  }
  
  .nav-text {
    font-size: 11px;
  }
  
  .favorites-grid {
    grid-template-columns: 1fr;
  }
  
  .favorite-image {
    height: 160px;
  }
  
  .tab-header {
    margin-bottom: 14px;
  }
  
  .tab-title {
    font-size: 16px;
  }
  
  .tab-title i {
    font-size: 18px;
  }
  
  .tab-desc {
    font-size: 12px;
  }
  
  .section-title {
    font-size: 14px;
  }
  
  .order-header {
    padding: 10px 14px;
  }
  
  .order-id {
    font-size: 12px;
  }
  
  .order-time {
    font-size: 10px;
  }
  
  .order-body {
    padding: 10px 14px;
  }
  
  .order-amount .value {
    font-size: 16px;
  }
  
  .address-header {
    flex-wrap: wrap;
  }
  
  .receiver-name {
    font-size: 14px;
  }
  
  .receiver-phone {
    font-size: 12px;
  }
}

@media (max-width: 360px) {
  .user-center {
    padding: 8px;
  }
  
  .user-card {
    border-radius: 12px;
  }
  
  .user-profile {
    padding: 16px 12px;
    border-radius: 12px 12px 0 0;
    margin: -16px -16px 0;
  }
  
  .user-avatar {
    border-width: 2px;
  }
  
  .nav-grid {
    gap: 8px;
  }
  
  .nav-item {
    padding: 8px 4px;
  }
  
  .favorites-grid {
    gap: 10px;
  }
  
  .favorite-info {
    padding: 10px;
  }
  
  .favorite-name {
    font-size: 13px;
  }
  
  .price-value {
    font-size: 15px;
  }
}
</style>
