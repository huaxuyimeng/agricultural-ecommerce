/**
 * 后台管理系统首页
 * 文件路径: src/views/manager/Home.vue
 * 功能描述: 平台核心数据概览页面，展示六大统计指标（待审核商品、已上架商品、总用户数、今日订单、今日销售额、系统公告），
 *           实时加载待审核商品列表并支持快速审核（通过/拒绝），展示最新订单列表，支持点击统计卡片快捷跳转到对应管理页面
 * 关联文件:
 * - src/api/index.js: 提供商品、订单、用户、公告等数据接口
 * - src/store/index.js: 提供当前登录用户信息
 * - src/views/manager/PendingProducts.vue: 商品审核页面
 * - src/views/manager/Products.vue: 商品管理页面
 * - src/views/manager/User.vue: 用户管理页面
 * - src/views/manager/Notice.vue: 公告管理页面
 */
<template>
  <div class="admin-home-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <i class="el-icon-s-management"></i>
            后台管理系统
          </h1>
          <p class="page-subtitle">
            欢迎回来，{{ (user && user.name) || (user && user.username) || '管理员' }}
            <span class="role-badge" v-if="user && user.role">管理员</span>
          </p>
        </div>
        <div class="header-right">
          <span class="current-time">{{ currentTime }}</span>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :xs="12" :sm="8" :md="4">
          <div class="stat-card" @click="$router.push('/pending-products')">
            <div class="stat-icon pending">
              <i class="el-icon-time"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ (stats && stats.pendingProducts) || 0 }}</div>
              <div class="stat-label">待审核</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4">
          <div class="stat-card" @click="$router.push('/products')">
            <div class="stat-icon approved">
              <i class="el-icon-check"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ (stats && stats.approvedProducts) || 0 }}</div>
              <div class="stat-label">已上架</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4">
          <div class="stat-card" @click="$router.push('/user')">
            <div class="stat-icon users">
              <i class="el-icon-user"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ (stats && stats.totalUsers) || 0 }}</div>
              <div class="stat-label">用户数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4">
          <div class="stat-card" @click="$router.push('/log')">
            <div class="stat-icon orders">
              <i class="el-icon-s-order"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ (stats && stats.todayOrders) || 0 }}</div>
              <div class="stat-label">今日订单</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4">
          <div class="stat-card" @click="$router.push('/log')">
            <div class="stat-icon sales">
              <i class="el-icon-goods"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">¥{{ (stats && stats.todaySales) || '0.00' }}</div>
              <div class="stat-label">今日销售</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4">
          <div class="stat-card" @click="$router.push('/notice')">
            <div class="stat-icon notices">
              <i class="el-icon-bell"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ (stats && stats.totalNotices) || 0 }}</div>
              <div class="stat-label">公告数</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 主要内容 -->
    <el-row :gutter="20" class="main-content">
      <!-- 左侧：待审核商品 -->
      <el-col :xs="24" :lg="14">
        <el-card class="content-card">
          <div slot="header" class="card-header">
            <span class="card-title">
              <i class="el-icon-time"></i>
              待审核商品
            </span>
            <el-button type="primary" link @click="$router.push('/pending-products')">
              查看全部 <i class="el-icon-arrow-right"></i>
            </el-button>
          </div>
          
          <div v-loading="loadingPending">
            <el-table v-if="pendingProducts.length > 0" :data="pendingProducts" stripe class="pending-table">
              <el-table-column label="商品信息" min-width="200">
                <template #default="{ row }">
                  <div class="product-cell">
                    <div class="product-image-wrapper">
                      <img
                        :src="getImageUrl(row.image)"
                        :alt="row.name"
                        class="product-image"
                        @error="handleImageError"
                      />
                    </div>
                    <div class="product-info">
                      <div class="product-name">{{ row.name }}</div>
                      <div class="product-category">
                        <el-tag :type="getCategoryTagType(row.category)" size="mini" effect="light">
                          {{ getCategoryText(row.category) }}
                        </el-tag>
                      </div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="price" label="价格" width="100" align="center">
                <template #default="{ row }">
                  <span class="price-text">¥{{ formatPrice(row.price) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="stock" label="库存" width="80" align="center">
                <template #default="{ row }">
                  <el-tag :type="getStockTagType(row.stock)" size="small">{{ row.stock || 0 }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="merchantName" label="商家" width="120" align="center">
                <template #default="{ row }">
                  {{ row.merchantName || (row.merchant && row.merchant.name) || '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="submitTime" label="提交时间" width="150" align="center">
                <template #default="{ row }">
                  {{ formatTime(row.submitTime || row.createdAt || row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="160" align="center">
                <template #default="{ row }">
                  <div class="action-buttons">
                    <el-button type="success" size="small" @click.stop="handleApprove(row)">
                      <i class="el-icon-check"></i> 通过
                    </el-button>
                    <el-button type="danger" size="small" plain @click.stop="handleReject(row)">
                      <i class="el-icon-close"></i> 拒绝
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-else description="暂无待审核商品" />
          </div>
        </el-card>

        <!-- 最新订单 -->
        <el-card class="content-card" style="margin-top: 20px;">
          <div slot="header" class="card-header">
            <span class="card-title">
              <i class="el-icon-s-order"></i>
              最新订单
            </span>
            <el-button type="primary" link @click="$router.push('/log')">
              查看全部 <i class="el-icon-arrow-right"></i>
            </el-button>
          </div>
          
          <div v-loading="loadingOrders">
            <el-table v-if="recentOrders.length > 0" :data="recentOrders" stripe>
              <el-table-column label="订单号" prop="orderId" width="150" />
              <el-table-column label="用户" width="100" align="center">
                <template #default="{ row }">{{ row.userName || row.username || '-' }}</template>
              </el-table-column>
              <el-table-column label="金额" width="100" align="center">
                <template #default="{ row }">
                  <span class="order-price">¥{{ row.totalAmount || row.total || 0 }}</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag size="small" :type="getOrderStatusType(row.status)">
                    {{ getOrderStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="时间" width="150" align="center">
                <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
              </el-table-column>
            </el-table>
            <el-empty v-else description="暂无订单" />
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：公告和快捷操作 -->
      <el-col :xs="24" :lg="10">
        <!-- 系统公告 -->
        <el-card class="content-card">
          <div slot="header" class="card-header">
            <span class="card-title">
              <i class="el-icon-bell"></i>
              系统公告
            </span>
            <el-button type="primary" link @click="$router.push('/notice')">
              管理 <i class="el-icon-arrow-right"></i>
            </el-button>
          </div>
          
          <div v-loading="loadingNotices">
            <div v-if="notices.length > 0" class="notice-list">
              <div
                v-for="notice in notices"
                :key="notice.id"
                class="notice-item"
                @click="viewNotice(notice)"
              >
                <div class="notice-icon-wrapper">
                  <i class="el-icon-bell"></i>
                </div>
                <div class="notice-content">
                  <div class="notice-title-row">
                    <el-tooltip :content="notice.title" placement="top" :disabled="notice.title.length < 30">
                      <div class="notice-title">{{ notice.title }}</div>
                    </el-tooltip>
                    <el-tag v-if="notice.category" :type="getNoticeCategoryTagType(notice.category)" size="mini" effect="light" class="notice-category-tag">
                      {{ getNoticeCategoryLabel(notice.category) }}
                    </el-tag>
                  </div>
                  <div class="notice-meta">
                    <span class="notice-author">
                      <i class="el-icon-user"></i>
                      {{ notice.author || '系统' }}
                    </span>
                    <span class="notice-time">
                      <i class="el-icon-time"></i>
                      {{ formatTime(notice.createTime || notice.createdAt) }}
                    </span>
                    <span v-if="notice.views" class="notice-views">
                      <i class="el-icon-view"></i>
                      {{ notice.views }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无公告" />
          </div>
        </el-card>

        <!-- 快捷操作 -->
        <el-card class="content-card" style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <i class="el-icon-s-operation"></i>
                快捷操作
              </span>
            </div>
          </template>
          
          <div class="quick-actions">
            <div class="action-grid">
              <div class="action-item" @click="$router.push('/products')">
                <div class="action-icon products-icon">
                  <i class="el-icon-goods"></i>
                </div>
                <span>商品管理</span>
              </div>
              <div class="action-item" @click="$router.push('/user')">
                <div class="action-icon users-icon">
                  <i class="el-icon-user"></i>
                </div>
                <span>用户管理</span>
              </div>
              <div class="action-item" @click="$router.push('/pending-products')">
                <div class="action-icon review-icon">
                  <i class="el-icon-s-check"></i>
                </div>
                <span>商品审核</span>
              </div>
              <div class="action-item" @click="$router.push('/publish-notice')">
                <div class="action-icon notice-icon">
                  <i class="el-icon-edit"></i>
                </div>
                <span>发布公告</span>
              </div>
              <div class="action-item" @click="$router.push('/statistics')">
                <div class="action-icon stats-icon">
                  <i class="el-icon-s-data"></i>
                </div>
                <span>数据统计</span>
              </div>
              <div class="action-item" @click="$router.push('/settings')">
                <div class="action-icon settings-icon">
                  <i class="el-icon-setting"></i>
                </div>
                <span>系统设置</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 拒绝原因对话框 -->
    <el-dialog :visible.sync="rejectDialogVisible" title="拒绝原因" width="450px">
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef">
        <el-form-item label="拒绝原因" prop="reason">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
        </el-form-item>
        <div class="common-reasons">
          <span>常用理由：</span>
          <el-tag v-for="reason in commonReasons" :key="reason" size="small" class="reason-tag" @click="selectReason(reason)">
            {{ reason }}
          </el-tag>
        </div>
      </el-form>
      <div slot="footer">
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject" :loading="rejecting">确认拒绝</el-button>
      </div>
    </el-dialog>

    <!-- 公告详情对话框 -->
    <el-dialog :visible.sync="noticeDialogVisible" :title="currentNotice && currentNotice.title" width="600px">
      <div class="notice-detail">
        <div class="notice-meta">
          <span>发布时间：{{ formatTime(currentNotice && currentNotice.createdAt) }}</span>
          <span>发布人：{{ (currentNotice && currentNotice.author) || '管理员' }}</span>
        </div>
        <el-divider />
        <div class="notice-body" v-html="currentNotice && currentNotice.content"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, onUnmounted } from '@vue/composition-api'
import { Message, MessageBox } from 'element-ui'
import { getProductPage, getPendingProducts, adminApproveProduct, adminRejectProduct } from '@/api'
import { getOrderPage, getUserPage } from '@/api'
import { getNewsPage } from '@/api'

export default {
  name: 'AdminHome',
  setup() {
    // 用户信息
    const user = ref(JSON.parse(localStorage.getItem('xm-user') || '{}'))
    const currentTime = ref('')
    let timeTimer = null

    // 统计数据
    const stats = reactive({
      pendingProducts: 0,
      approvedProducts: 0,
      totalUsers: 0,
      todayOrders: 0,
      todaySales: '0.00',
      totalNotices: 0
    })

    // 列表数据
    const pendingProducts = ref([])
    const recentOrders = ref([])
    const notices = ref([])
    const loadingPending = ref(false)
    const loadingOrders = ref(false)
    const loadingNotices = ref(false)

    // 拒绝对话框
    const rejectDialogVisible = ref(false)
    const rejectFormRef = ref(null)
    const rejectForm = reactive({ reason: '', productId: null })
    const rejecting = ref(false)
    const commonReasons = ['图片不清晰', '描述不完整', '价格不合理', '分类错误', '信息不实', '违反规定']

    // 公告对话框
    const noticeDialogVisible = ref(false)
    const currentNotice = ref(null)

    // 拒绝验证规则
    const rejectRules = {
      reason: [
        { required: true, message: '请输入拒绝原因', trigger: 'blur' },
        { min: 5, message: '拒绝原因至少5个字符', trigger: 'blur' }
      ]
    }

    // 更新当前时间
    const updateTime = () => {
      const now = new Date()
      currentTime.value = now.toLocaleString('zh-CN', {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit', second: '2-digit'
      })
    }

    // 获取图片
    const getImageUrl = (image) => {
      if (!image) return ''
      if (image.startsWith('data:image') || image.startsWith('http')) return image
      if (image.startsWith('imgs/') || image.startsWith('/imgs/')) {
        return image.startsWith('/') ? image : `/${image}`
      }
      return `/imgs/${image}`
    }

    const handleImageError = (e) => {
      const img = e.target
      if (!img.dataset.fallback) {
        img.dataset.fallback = 'true'
        img.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjAiIGhlaWdodD0iNjAiIHZpZXdCb3g9IjAgMCA2MCA2MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iNjAiIGhlaWdodD0iNjAiIGZpbGw9IiNGMUY1RjkiLz48cGF0aCBkPSJNMjAgMjVDMjAgMjIuNzkgMjEuNzkgMjEgMjQgMjFIMzZDMzguMjEgMjEgNDAgMjIuNzkgNDAgMjVWMzVDMzggMzUgMjIgMzUgMjAgMzVWMjVaIiBmaWxsPSIjOTRBNEI4Ii8+PGNpcmNsZSBjeD0iMjgiIGN5PSIyOCIgcj0iMyIgZmlsbD0iIzk0QTNCOCIvPjxwYXRoIGQ9Ik0yMCAzNUwyNSAzMEwzMCAzNUwzNSAzMEw0MCAzNVYzN0gyMFYzNVoiIGZpbGw9IiM5NEEzQjgiLz48L3N2Zz4='
      }
    }

    const formatPrice = (price) => {
      const num = Number(price)
      return isNaN(num) ? '0.00' : num.toFixed(2)
    }

    const getCategoryText = (category) => {
      const map = {
        vegetable: '蔬菜', fruit: '水果', meat: '肉类',
        grain: '粮食', processed: '加工产品', other: '其他'
      }
      return map[category] || category || '其他'
    }

    const getCategoryTagType = (category) => {
      const typeMap = {
        vegetable: 'success', fruit: 'danger', meat: 'warning',
        grain: '', processed: 'info', other: ''
      }
      return typeMap[category] || 'info'
    }

    const getNoticeCategoryLabel = (category) => {
      const map = {
        policy: '政策', technology: '科技', activity: '活动',
        notice: '公告', news: '新闻', other: '其他'
      }
      return map[category] || category || '公告'
    }

    const getNoticeCategoryTagType = (category) => {
      const typeMap = {
        policy: 'danger', technology: 'primary', activity: 'success',
        notice: 'warning', news: 'info', other: 'info'
      }
      return typeMap[category] || 'info'
    }

    const getStockTagType = (stock) => {
      const num = Number(stock)
      if (num > 50) return 'success'
      if (num > 10) return ''
      if (num > 0) return 'warning'
      return 'danger'
    }

    // 格式化时间
    const formatTime = (time) => {
      if (!time) return '-'
      const date = new Date(time)
      return date.toLocaleString('zh-CN', {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit'
      })
    }

    // 订单状态
    const getOrderStatusType = (status) => {
      const map = { pending: 'warning', paid: 'info', shipping: 'primary', completed: 'success', cancelled: 'info' }
      return map[status] || 'info'
    }
    const getOrderStatusText = (status) => {
      const map = { pending: '待支付', paid: '待发货', shipping: '配送中', completed: '已完成', cancelled: '已取消' }
      return map[status] || status
    }

    // 加载统计数据
    const loadStats = async () => {
      try {
        // 待审核商品
        const pendingRes = await getPendingProducts({ pageNum: 1, pageSize: 1 })
        stats.pendingProducts = pendingRes?.data?.total || 0

        // 已上架商品
        const approvedRes = await getProductPage({ pageNum: 1, pageSize: 1 })
        stats.approvedProducts = approvedRes?.data?.total || 0

        // 用户数
        const usersRes = await getUserPage({ pageNum: 1, pageSize: 1 })
        stats.totalUsers = usersRes?.data?.total || 0

        // 今日订单
        const ordersRes = await getOrderPage({ pageNum: 1, pageSize: 100 })
        let orders = []
        if (ordersRes?.data?.list) orders = ordersRes.data.list
        else if (ordersRes?.data?.records) orders = ordersRes.data.records
        else if (ordersRes?.data) orders = Array.isArray(ordersRes.data) ? ordersRes.data : []
        else if (Array.isArray(ordersRes)) orders = ordersRes

        const today = new Date().toDateString()
        const todayOrders = orders.filter(o => {
          const orderDate = new Date(o.createTime || o.createdAt).toDateString()
          return orderDate === today
        })
        stats.todayOrders = todayOrders.length
        stats.todaySales = todayOrders.reduce((sum, o) => sum + (o.totalAmount || o.total || 0), 0).toFixed(2)

        // 公告数
        const noticesRes = await getNewsPage({ pageNum: 1, pageSize: 1 })
        stats.totalNotices = noticesRes?.data?.total || 0

      } catch (error) {
        console.error('加载统计失败:', error)
      }
    }

    // 加载待审核商品
    const loadPendingProducts = async () => {
      loadingPending.value = true
      try {
        const res = await getPendingProducts({ pageNum: 1, pageSize: 10 })
        let list = []
        if (res?.data?.list) list = res.data.list
        else if (res?.data?.records) list = res.data.records
        else if (res?.data) list = Array.isArray(res.data) ? res.data : []
        else if (Array.isArray(res)) list = res
        pendingProducts.value = list
      } catch (error) {
        console.error('加载待审核商品失败:', error)
      } finally {
        loadingPending.value = false
      }
    }

    // 加载最新订单
    const loadRecentOrders = async () => {
      loadingOrders.value = true
      try {
        const res = await getOrderPage({ pageNum: 1, pageSize: 10 })
        let list = []
        if (res?.data?.list) list = res.data.list
        else if (res?.data?.records) list = res.data.records
        else if (res?.data) list = Array.isArray(res.data) ? res.data : []
        else if (Array.isArray(res)) list = res
        recentOrders.value = list.slice(0, 5)
      } catch (error) {
        console.error('加载订单失败:', error)
      } finally {
        loadingOrders.value = false
      }
    }

    // 加载公告
    const loadNotices = async () => {
      loadingNotices.value = true
      try {
        const res = await getNewsPage({ pageNum: 1, pageSize: 5 })
        let list = []
        if (res?.data?.list) list = res.data.list
        else if (res?.data?.records) list = res.data.records
        else if (res?.data) list = Array.isArray(res.data) ? res.data : []
        else if (Array.isArray(res)) list = res
        notices.value = list
      } catch (error) {
        console.error('加载公告失败:', error)
      } finally {
        loadingNotices.value = false
      }
    }

    // 加载全部数据
    const loadData = async () => {
      await Promise.all([loadStats(), loadPendingProducts(), loadRecentOrders(), loadNotices()])
    }

    // 行点击
    const handleRowClick = (row) => {
      // 可以跳转到商品详情
    }

    // 审核通过
    const handleApprove = async (row) => {
      try {
        await MessageBox.confirm(`确定通过商品「${row.name}」吗？`, '审核确认', { type: 'success' })
        await adminApproveProduct(row.id)
        Message.success('审核通过')
        loadPendingProducts()
        loadStats()
      } catch (err) {
        if (err === 'cancel' || (err && err.type === 'cancel')) return
        Message.error(err.message || '操作失败')
      }
    }

    // 打开拒绝对话框
    const handleReject = (row) => {
      rejectForm.productId = row.id
      rejectForm.reason = ''
      rejectDialogVisible.value = true
    }

    // 选择常用理由
    const selectReason = (reason) => {
      rejectForm.reason = reason
    }

    // 确认拒绝
    const confirmReject = async () => {
      const valid = await rejectFormRef.value.validate().catch(() => false)
      if (!valid) return

      rejecting.value = true
      try {
        await adminRejectProduct(rejectForm.productId)
        Message.success('已拒绝')
        rejectDialogVisible.value = false
        loadPendingProducts()
        loadStats()
      } catch (err) {
        Message.error(err.message || '操作失败')
      } finally {
        rejecting.value = false
      }
    }

    // 查看公告
    const viewNotice = (notice) => {
      currentNotice.value = notice
      noticeDialogVisible.value = true
    }

    // 初始化
    onMounted(() => {
      updateTime()
      timeTimer = setInterval(updateTime, 1000)
      loadData()
    })

    onUnmounted(() => {
      if (timeTimer) clearInterval(timeTimer)
    })

    return {
      user,
      currentTime,
      stats,
      pendingProducts,
      recentOrders,
      notices,
      loadingPending,
      loadingOrders,
      loadingNotices,
      rejectDialogVisible,
      rejectForm,
      rejectRules,
      rejectFormRef,
      rejecting,
      commonReasons,
      noticeDialogVisible,
      currentNotice,
      getImageUrl,
      handleImageError,
      formatPrice,
      getCategoryText,
      getCategoryTagType,
      getNoticeCategoryLabel,
      getNoticeCategoryTagType,
      getStockTagType,
      formatTime,
      getOrderStatusType,
      getOrderStatusText,
      handleRowClick,
      handleApprove,
      handleReject,
      selectReason,
      confirmReject,
      viewNotice
    }
  }
}
</script>

<style scoped>
.admin-home-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 24px 30px;
  margin-bottom: 20px;
  color: white;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 26px;
  font-weight: 700;
  margin: 0 0 6px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-subtitle {
  font-size: 14px;
  margin: 0;
  opacity: 0.9;
}

.role-badge {
  background: rgba(255,255,255,0.2);
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 12px;
  margin-left: 8px;
}

.current-time {
  font-size: 14px;
  opacity: 0.9;
}

.stats-section {
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 10px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  margin-bottom: 15px;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.1);
}

.stat-icon {
  width: 54px;
  height: 54px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  color: white;
  flex-shrink: 0;
}

.stat-icon.pending { background: linear-gradient(135deg, #E6A23C, #f5a623); }
.stat-icon.approved { background: linear-gradient(135deg, #67C23A, #5daf34); }
.stat-icon.users { background: linear-gradient(135deg, #409EFF, #66b1ff); }
.stat-icon.orders { background: linear-gradient(135deg, #909399, #a6a9ad); }
.stat-icon.sales { background: linear-gradient(135deg, #F56C6C, #e64242); }
.stat-icon.notices { background: linear-gradient(135deg, #9C27B0, #ba68c8); }

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-number {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

.main-content {
  margin-top: 0;
}

.content-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title i {
  color: #909399;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-image-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f5f5f5;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  min-width: 0;
  flex: 1;
}

.product-name {
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}

.product-category {
  display: inline-block;
}

.price-text {
  color: #ff5722;
  font-weight: 600;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.action-buttons .el-button {
  padding: 6px 14px;
  font-size: 12px;
}

.order-price {
  color: #ff5722;
  font-weight: 600;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
  background: #fafbfc;
  border: 1px solid transparent;
}

.notice-item:hover {
  background: #f0f7ff;
  border-color: #e1eaff;
  transform: translateX(2px);
}

.notice-icon-wrapper {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #fff;
  font-size: 16px;
}

.notice-content {
  flex: 1;
  min-width: 0;
}

.notice-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.notice-title {
  font-weight: 500;
  color: #303133;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.5;
  font-size: 14px;
  flex: 1;
  min-width: 0;
}

.notice-category-tag {
  flex-shrink: 0;
}

.notice-meta {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 12px;
  color: #a8abb2;
}

.notice-meta i {
  margin-right: 3px;
  font-size: 12px;
}

.notice-author {
  display: flex;
  align-items: center;
}

.notice-time {
  display: flex;
  align-items: center;
}

.notice-views {
  display: flex;
  align-items: center;
}

.quick-actions {
  padding: 10px 0;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 18px 10px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.action-item:hover {
  background: #f5f7fa;
  border-color: #e4e7ed;
  transform: translateY(-2px);
}

.action-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  background: #f0f2f5;
  color: #909399;
  transition: all 0.3s;
}

.action-item:hover .action-icon {
  transform: scale(1.08);
}

.action-item:hover .products-icon { background: #ecf5ff; color: #409eff; }
.action-item:hover .users-icon { background: #f0f9eb; color: #67c23a; }
.action-item:hover .review-icon { background: #fdf6ec; color: #e6a23c; }
.action-item:hover .notice-icon { background: #f4f4f5; color: #606266; }
.action-item:hover .stats-icon { background: #fef0f0; color: #f56c6c; }
.action-item:hover .settings-icon { background: #f4f0fe; color: #9c27b0; }

.action-item span {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.common-reasons {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.common-reasons > span {
  font-size: 13px;
  color: #909399;
}

.reason-tag {
  cursor: pointer;
}

.notice-detail {
  padding: 10px 0;
}

.notice-meta {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 13px;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .action-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
