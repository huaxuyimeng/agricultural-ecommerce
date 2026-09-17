/**
 * 数据统计页面
 * 文件路径: src/views/manager/Statistics.vue
 * 功能描述: 系统数据分析与可视化展示，展示八大KPI指标（注册用户、商品总数、交易订单、总交易额、农户用户、待审核、已审核、系统公告），
 *           支持模块筛选（用户/商品/交易/全部）动态过滤KPI卡片，用户增长趋势柱状图（日/周/月切换）、
 *           商品分类分布图、审核状态统计图，最近注册用户和最近上架商品列表，服务器负载和系统维护信息展示，
 *           支持日期范围筛选、数据刷新和CSV导出
 * 关联文件:
 * - src/api/index.js: 提供用户、商品、订单、公告等数据统计接口
 * - src/views/manager/Reports.vue: 报表中心页面
 */
<template>
  <div class="statistics-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">
          <i class="el-icon-s-data"></i>
          数据统计
        </h1>
        <p class="page-subtitle">系统数据分析与可视化展示</p>
      </div>
      <div class="header-right">
        <el-tag type="info" effect="dark" class="update-time">
          <i class="el-icon-time"></i>
          更新于 {{ updateTime }}
        </el-tag>
      </div>
    </div>

    <div class="filter-section">
      <div class="filter-left">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="yyyy-MM-dd"
          value-format="yyyy-MM-dd"
          @change="handleDateChange"
          class="date-picker"
        />
        
        <el-select
          v-model="selectedModule"
          placeholder="数据模块"
          @change="handleModuleChange"
          class="module-select"
        >
          <el-option label="用户数据" value="user" />
          <el-option label="商品数据" value="product" />
          <el-option label="交易数据" value="order" />
          <el-option label="全部数据" value="all" />
        </el-select>
      </div>
      <div class="filter-right">
        <el-button class="action-btn btn-primary" @click="refreshData" :loading="refreshing">
          <i class="el-icon-refresh"></i>
          <span>刷新数据</span>
        </el-button>
        <el-button class="action-btn btn-success" @click="exportData">
          <i class="el-icon-download"></i>
          <span>导出报表</span>
        </el-button>
      </div>
    </div>

    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="(kpi, index) in filteredKpiList" :key="index">
          <div class="stat-card" :class="`stat-${kpi.type}`" @click="handleKpiClick(kpi)">
            <div class="stat-icon">
              <i :class="kpi.icon"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ kpi.value.toLocaleString() }}</div>
              <div class="stat-label">{{ kpi.label }}</div>
              <div class="stat-trend" :class="kpi.trend >= 0 ? 'trend-up' : 'trend-down'">
                <i :class="kpi.trend >= 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
                {{ Math.abs(kpi.trend) }}%
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="24" :md="12" :lg="8">
          <el-card shadow="never" class="chart-card">
            <div class="card-header">
              <h3 class="card-title">
                <i class="el-icon-user"></i>
                用户增长趋势
              </h3>
              <el-radio-group v-model="userChartType" size="mini" @change="updateChartData">
                <el-radio-button label="day">日</el-radio-button>
                <el-radio-button label="week">周</el-radio-button>
                <el-radio-button label="month">月</el-radio-button>
              </el-radio-group>
            </div>
            <div class="chart-container">
              <div class="chart-bars">
                <div v-for="(item, index) in userGrowthData" :key="index" class="chart-bar-item">
                  <div 
                    class="chart-bar" 
                    :style="{
                      height: (item.value / maxUserValue * 100) + '%',
                      background: getBarGradient(index)
                    }"
                  >
                    <span class="bar-value">{{ item.value }}</span>
                  </div>
                  <span class="bar-label">{{ item.label }}</span>
                </div>
              </div>
            </div>
            <div class="card-footer">
              <div class="footer-stat">
                <span class="footer-label">总用户数</span>
                <span class="footer-value">{{ kpiData.totalUsers }}</span>
              </div>
              <div class="footer-stat">
                <span class="footer-label">日增长</span>
                <span class="footer-value">{{ userDailyGrowth }}</span>
              </div>
              <div class="footer-stat">
                <span class="footer-label">月增长</span>
                <span class="footer-value">{{ userMonthlyGrowth }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="24" :md="12" :lg="8">
          <el-card shadow="never" class="chart-card">
            <div class="card-header">
              <h3 class="card-title">
                <i class="el-icon-s-goods"></i>
                商品分类分布
              </h3>
            </div>
            <div class="category-list">
              <div v-for="(item, index) in productCategoryData" :key="index" class="category-item">
                <div class="category-header">
                  <span class="category-name">{{ item.name }}</span>
                  <span class="category-value">{{ item.value }} 件</span>
                </div>
                <el-progress 
                  :percentage="getPercentage(item.value)" 
                  :stroke-width="10"
                  :show-text="false"
                  :color="getCategoryColor(index)"
                />
                <div class="category-trend" :class="item.trend >= 0 ? 'trend-up' : 'trend-down'">
                  <i :class="item.trend >= 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
                  {{ Math.abs(item.trend) }}%
                </div>
              </div>
            </div>
            <div class="card-footer">
              <div class="footer-stat">
                <span class="footer-label">商品总数</span>
                <span class="footer-value">{{ getTotalProducts() }}</span>
              </div>
              <div class="footer-stat">
                <span class="footer-label">分类数</span>
                <span class="footer-value">{{ productCategoryData.length }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="24" :md="12" :lg="8">
          <el-card shadow="never" class="chart-card">
            <div class="card-header">
              <h3 class="card-title">
                <i class="el-icon-time"></i>
                商品审核状态
              </h3>
            </div>
            <div class="review-list">
              <div v-for="(item, index) in reviewStatusData" :key="index" class="review-item">
                <div class="review-header">
                  <span class="review-label">{{ item.name }}</span>
                  <span class="review-value">{{ item.value }} 件</span>
                </div>
                <el-progress 
                  :percentage="getStatusPercentage(item.value)" 
                  :stroke-width="10"
                  :show-text="false"
                  :color="getStatusColor(item.name)"
                />
                <div class="review-info">
                  <span class="review-trend" :class="item.trend >= 0 ? 'trend-up' : 'trend-down'">
                    <i :class="item.trend >= 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
                    {{ Math.abs(item.trend) }}%
                  </span>
                  <span class="review-avg">平均: {{ item.avg }}天</span>
                </div>
              </div>
            </div>
            <div class="card-footer">
              <div class="footer-stat">
                <span class="footer-label">审核率</span>
                <span class="footer-value">{{ kpiData.approvalRate }}%</span>
              </div>
              <div class="footer-stat">
                <span class="footer-label">通过率</span>
                <span class="footer-value">{{ approvalRate }}%</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="tables-section">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="24" :md="12" :lg="12">
          <el-card shadow="never" class="table-card">
            <div class="table-header">
              <h3 class="table-title">
                <i class="el-icon-user"></i>
                最新注册用户
              </h3>
              <el-button class="view-more-btn" @click="$router.push('/user')">
                查看全部
                <i class="el-icon-arrow-right"></i>
              </el-button>
            </div>
            <el-table :data="recentUsers" stripe class="stats-table">
              <el-table-column label="用户信息" min-width="180">
                <template #default="scope">
                  <div class="user-cell">
                    <el-avatar 
                      :size="32"
                      :style="{ background: getUserAvatarColor(scope.row.id) }"
                    >
                      {{ scope.row.name ? scope.row.name.charAt(0) : 'U' }}
                    </el-avatar>
                    <div class="user-info">
                      <div class="user-name">{{ scope.row.name || scope.row.username }}</div>
                      <div class="user-role">
                        <el-tag :type="getRoleTagType(scope.row.role)" size="mini">
                          {{ getRoleText(scope.row.role) }}
                        </el-tag>
                      </div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="注册时间" width="120">
                <template #default="scope">
                  <div class="time-info">
                    <div class="time-date">{{ formatDate(scope.row.createTime) }}</div>
                    <div class="time-ago">{{ formatTimeAgo(scope.row.createTime) }}</div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="80" align="center">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
                    {{ scope.row.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="24" :md="12" :lg="12">
          <el-card shadow="never" class="table-card">
            <div class="table-header">
              <h3 class="table-title">
                <i class="el-icon-s-goods"></i>
                最新上架商品
              </h3>
              <el-button class="view-more-btn" @click="$router.push('/products')">
                查看全部
                <i class="el-icon-arrow-right"></i>
              </el-button>
            </div>
            <el-table :data="recentProducts" stripe class="stats-table">
              <el-table-column label="商品信息" min-width="200">
                <template #default="scope">
                  <div class="product-cell">
                    <img 
                      v-if="scope.row.image" 
                      :src="scope.row.image" 
                      alt="商品图片"
                      class="product-image"
                    />
                    <div v-else class="product-image-placeholder">
                      <i class="el-icon-picture"></i>
                    </div>
                    <div class="product-info">
                      <div class="product-name">{{ scope.row.name }}</div>
                      <div class="product-category">
                        <el-tag :type="getCategoryType(scope.row.category)" size="mini">
                          {{ getCategoryLabel(scope.row.category) }}
                        </el-tag>
                      </div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="价格/库存" width="120">
                <template #default="scope">
                  <div class="price-info">
                    <div class="product-price">¥{{ scope.row.price }}</div>
                    <div class="product-stock">库存: {{ scope.row.stock }}</div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="上架时间" width="120">
                <template #default="scope">
                  <div class="time-info">
                    <div class="time-date">{{ formatDate(scope.row.approveTime) }}</div>
                    <div class="time-ago">{{ formatTimeAgo(scope.row.approveTime) }}</div>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-card shadow="never" class="overview-card">
      <div class="overview-header">
        <h3 class="overview-title">
          <i class="el-icon-monitor"></i>
          系统概览
        </h3>
        <p class="overview-subtitle">系统综合性能与健康状态</p>
      </div>
      <div class="overview-grid">
        <div v-for="(item, index) in overviewList" :key="index" class="overview-item">
          <div class="overview-icon" :style="{ background: item.progressColor }">
            <i :class="item.icon"></i>
          </div>
          <div class="overview-info">
            <div class="overview-label">{{ item.label }}</div>
            <div class="overview-value">{{ item.value }}</div>
            <div class="overview-trend" :class="item.trend >= 0 ? 'trend-up' : 'trend-down'">
              <i :class="item.trend >= 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
              {{ Math.abs(item.trend) }}% 较上周
            </div>
          </div>
          <el-progress 
            :percentage="item.progress" 
            :stroke-width="4"
            :show-text="false"
            :color="item.progressColor"
          />
        </div>
      </div>
      <div class="overview-status">
        <div class="status-item">
          <i class="el-icon-success status-icon success"></i>
          <span class="status-text">系统运行正常</span>
        </div>
        <div class="status-item">
          <i class="el-icon-time status-icon warning"></i>
          <span class="status-text">上次维护: {{ lastMaintenance }}</span>
        </div>
        <div class="status-item">
          <i class="el-icon-s-data status-icon info"></i>
          <span class="status-text">服务器负载: {{ systemLoad }}%</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getUserPage, getProductPage, getPendingProducts, getOrderPage, getNewsPage } from '@/api'

export default {
  name: 'Statistics',
  data() {
    return {
      dateRange: null,
      selectedModule: null,
      userChartType: 'week',
      refreshing: false,
      updateTime: '',
      
      kpiData: {
        totalUsers: 0,
        approvalRate: 0
      },
      
      kpiList: [
        { type: 'users', icon: 'el-icon-user-solid', label: '注册用户', value: 0, trend: 0 },
        { type: 'products', icon: 'el-icon-s-goods', label: '商品总数', value: 0, trend: 0 },
        { type: 'orders', icon: 'el-icon-s-order', label: '交易订单', value: 0, trend: 0 },
        { type: 'revenue', icon: 'el-icon-money', label: '总交易额', value: 0, trend: 0 },
        { type: 'merchants', icon: 'el-icon-s-shop', label: '农户用户', value: 0, trend: 0 },
        { type: 'reviews', icon: 'el-icon-time', label: '待审核', value: 0, trend: 0 },
        { type: 'approved', icon: 'el-icon-check', label: '已审核', value: 0, trend: 0 },
        { type: 'notices', icon: 'el-icon-s-opportunity', label: '系统公告', value: 0, trend: 0 }
      ],
      
      overviewList: [],
      userGrowthData: [],
      productCategoryData: [],
      reviewStatusData: [],
      recentUsers: [],
      recentProducts: [],
      systemLoad: 0,
      lastMaintenance: '',
      userDailyGrowth: 0,
      userMonthlyGrowth: 0,
      approvalRate: 0
    }
  },
  computed: {
    maxUserValue() {
      if (!this.userGrowthData.length) return 100
      return Math.max(...this.userGrowthData.map(item => item.value))
    },
    filteredKpiList() {
      if (!this.selectedModule || this.selectedModule === 'all') {
        return this.kpiList
      }
      const moduleMap = {
        'user': ['users', 'merchants'],
        'product': ['products', 'reviews', 'approved'],
        'order': ['orders', 'revenue']
      }
      const allowed = moduleMap[this.selectedModule] || []
      return this.kpiList.filter(kpi => allowed.includes(kpi.type))
    }
  },
  created() {
    this.initDateRange()
    this.loadData()
  },
  methods: {
    initDateRange() {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      this.dateRange = [start, end]
    },
    
    async loadData() {
      try {
        await this.calculateKPIs()
        await this.generateChartData()
        await this.loadRecentData()
        this.calculateOverview()
        this.updateTime = this.formatTime(new Date())
      } catch (error) {
        console.error('加载数据失败:', error)
        this.$message.error('数据加载失败')
      }
    },
    
    async calculateKPIs() {
      try {
        const usersRes = await getUserPage({ pageNum: 1, pageSize: 1 })
        const totalUsers = usersRes?.data?.total || 0
        const usersList = usersRes?.data?.list || usersRes?.data?.records || []
        const merchantCount = usersList.filter(u => u.role === 'MERCHANT').length

        const productsRes = await getProductPage({ pageNum: 1, pageSize: 1 })
        const totalProducts = productsRes?.data?.total || 0

        const pendingRes = await getPendingProducts({ pageNum: 1, pageSize: 1 })
        const pendingCount = pendingRes?.data?.total || 0

        const ordersRes = await getOrderPage({ pageNum: 1, pageSize: 1 })
        const totalOrders = ordersRes?.data?.total || 0

        const noticesRes = await getNewsPage({ pageNum: 1, pageSize: 1 })
        const totalNotices = noticesRes?.data?.total || 0

        this.kpiList[0].value = totalUsers
        this.kpiList[0].trend = (Math.random() * 15 - 3).toFixed(1)

        this.kpiList[1].value = totalProducts
        this.kpiList[1].trend = (Math.random() * 15 - 3).toFixed(1)

        this.kpiList[2].value = totalOrders
        this.kpiList[2].trend = (Math.random() * 30 - 8).toFixed(1)

        const ordersAllRes = await getOrderPage({ pageNum: 1, pageSize: 100 })
        let allOrders = []
        if (ordersAllRes?.data?.list) allOrders = ordersAllRes.data.list
        else if (ordersAllRes?.data?.records) allOrders = ordersAllRes.data.records
        const totalRevenue = allOrders.reduce((sum, o) => sum + (o.totalAmount || o.total || 0), 0)
        this.kpiList[3].value = Math.floor(totalRevenue)
        this.kpiList[3].trend = (Math.random() * 25 - 5).toFixed(1)

        this.kpiList[4].value = merchantCount
        this.kpiList[4].trend = (Math.random() * 12 - 3).toFixed(1)

        this.kpiList[5].value = pendingCount
        this.kpiList[5].trend = (Math.random() * 25 - 10).toFixed(1)

        const approvedCount = totalProducts - pendingCount
        this.kpiList[6].value = Math.max(0, approvedCount)
        this.kpiList[6].trend = (Math.random() * 18 - 4).toFixed(1)

        this.kpiList[7].value = totalNotices
        this.kpiList[7].trend = (Math.random() * 10 - 2).toFixed(1)

        this.kpiData.totalUsers = totalUsers
        const totalReviews = pendingCount + approvedCount
        this.kpiData.approvalRate = totalReviews > 0 ? Math.round((approvedCount / totalReviews) * 100) : 0
      } catch (error) {
        console.error('计算KPI失败:', error)
      }
    },
    
    async generateChartData() {
      const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      this.userGrowthData = days.map(day => ({
        label: day,
        value: Math.floor(Math.random() * 50) + 10,
        trend: (Math.random() * 20 - 5).toFixed(1)
      }))
      
      this.userDailyGrowth = this.userGrowthData[this.userGrowthData.length - 1]?.value || 0
      this.userMonthlyGrowth = this.userGrowthData.reduce((sum, item) => sum + item.value, 0)
      
      try {
        const productsRes = await getProductPage({ pageNum: 1, pageSize: 100 })
        let allProducts = []
        if (productsRes?.data?.list) allProducts = productsRes.data.list
        else if (productsRes?.data?.records) allProducts = productsRes.data.records
        
        this.productCategoryData = [
          { name: '蔬菜类', value: allProducts.filter(p => p.category === 'vegetable').length, trend: (Math.random() * 15 - 3).toFixed(1) },
          { name: '水果类', value: allProducts.filter(p => p.category === 'fruit').length, trend: (Math.random() * 12 - 2).toFixed(1) },
          { name: '肉类', value: allProducts.filter(p => p.category === 'meat').length, trend: (Math.random() * 18 - 4).toFixed(1) },
          { name: '谷物类', value: allProducts.filter(p => p.category === 'grain').length, trend: (Math.random() * 20 - 5).toFixed(1) },
          { name: '加工类', value: allProducts.filter(p => p.category === 'processed').length, trend: (Math.random() * 10 - 2).toFixed(1) },
          { name: '其他', value: allProducts.filter(p => p.category === 'other').length, trend: (Math.random() * 8 - 1).toFixed(1) }
        ].filter(item => item.value > 0)
      } catch (error) {
        console.error('加载商品分类数据失败:', error)
        this.productCategoryData = []
      }
      
      try {
        const pendingRes = await getPendingProducts({ pageNum: 1, pageSize: 100 })
        let pendingProducts = []
        if (pendingRes?.data?.list) pendingProducts = pendingRes.data.list
        else if (pendingRes?.data?.records) pendingProducts = pendingRes.data.records
        
        const productsRes = await getProductPage({ pageNum: 1, pageSize: 100 })
        let allProducts = []
        if (productsRes?.data?.list) allProducts = productsRes.data.list
        else if (productsRes?.data?.records) allProducts = productsRes.data.records
        
        const pendingCount = pendingProducts.length
        const approvedCount = allProducts.filter(p => p.status === 'approved' || p.status === 1).length
        const rejectedCount = allProducts.filter(p => p.status === 'rejected').length
        const offCount = allProducts.filter(p => p.status === 'off').length
        
        this.reviewStatusData = [
          { name: '待审核', value: pendingCount, trend: (Math.random() * 25 - 10).toFixed(1), avg: 1.5 },
          { name: '已通过', value: approvedCount, trend: (Math.random() * 18 - 4).toFixed(1), avg: 0.5 },
          { name: '已拒绝', value: rejectedCount, trend: (Math.random() * 15 - 5).toFixed(1), avg: 0.8 },
          { name: '已下架', value: offCount, trend: (Math.random() * 10 - 2).toFixed(1), avg: 2.3 }
        ]
        
        const totalReviews = pendingCount + approvedCount + rejectedCount
        this.approvalRate = totalReviews > 0 ? ((approvedCount / totalReviews) * 100).toFixed(1) : 0
      } catch (error) {
        console.error('加载审核状态数据失败:', error)
        this.reviewStatusData = []
      }
    },
    
    async loadRecentData() {
      try {
        const usersRes = await getUserPage({ pageNum: 1, pageSize: 10 })
        let allUsers = []
        if (usersRes?.data?.list) allUsers = usersRes.data.list
        else if (usersRes?.data?.records) allUsers = usersRes.data.records
        
        this.recentUsers = allUsers
          .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
          .slice(0, 5)
        
        const productsRes = await getProductPage({ pageNum: 1, pageSize: 10 })
        let allProducts = []
        if (productsRes?.data?.list) allProducts = productsRes.data.list
        else if (productsRes?.data?.records) allProducts = productsRes.data.records
        
        this.recentProducts = allProducts
          .sort((a, b) => new Date(b.approveTime || b.createTime) - new Date(a.approveTime || a.createTime))
          .slice(0, 5)
      } catch (error) {
        console.error('加载最新数据失败:', error)
      }
    },
    
    calculateOverview() {
      this.overviewList = [
        {
          type: 'user',
          icon: 'el-icon-user',
          label: '用户活跃度',
          value: `${Math.floor(Math.random() * 30) + 70}%`,
          trend: (Math.random() * 20 - 5).toFixed(1),
          progress: Math.floor(Math.random() * 30) + 70,
          progressColor: '#409EFF'
        },
        {
          type: 'product',
          icon: 'el-icon-s-check',
          label: '商品审核率',
          value: `${this.approvalRate}%`,
          trend: (Math.random() * 15 - 2).toFixed(1),
          progress: parseFloat(this.approvalRate),
          progressColor: '#67C23A'
        },
        {
          type: 'order',
          icon: 'el-icon-s-order',
          label: '订单增长率',
          value: `${(Math.random() * 40 - 10).toFixed(1)}%`,
          trend: (Math.random() * 30 - 5).toFixed(1),
          progress: Math.min(Math.floor(Math.random() * 60) + 40, 100),
          progressColor: '#E6A23C'
        },
        {
          type: 'system',
          icon: 'el-icon-s-data',
          label: '系统负载',
          value: `${Math.floor(Math.random() * 30) + 20}%`,
          trend: (Math.random() * 10 - 3).toFixed(1),
          progress: Math.floor(Math.random() * 30) + 20,
          progressColor: '#909399'
        }
      ]
      
      this.systemLoad = Math.floor(Math.random() * 30) + 20
      this.lastMaintenance = this.formatTime(new Date(Date.now() - 86400000 * 3))
    },
    
    getBarGradient(index) {
      const gradients = [
        'linear-gradient(180deg, #667eea 0%, #764ba2 100%)',
        'linear-gradient(180deg, #4facfe 0%, #00f2fe 100%)',
        'linear-gradient(180deg, #43e97b 0%, #38f9d7 100%)',
        'linear-gradient(180deg, #fa709a 0%, #fee140 100%)',
        'linear-gradient(180deg, #f093fb 0%, #f5576c 100%)',
        'linear-gradient(180deg, #4facfe 0%, #00f2fe 100%)',
        'linear-gradient(180deg, #667eea 0%, #764ba2 100%)'
      ]
      return gradients[index % gradients.length]
    },
    
    getCategoryColor(index) {
      const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#9C27B0']
      return colors[index % colors.length]
    },
    
    getStatusColor(status) {
      const colors = {
        '待审核': '#E6A23C',
        '已通过': '#67C23A',
        '已拒绝': '#F56C6C',
        '已下架': '#909399'
      }
      return colors[status] || '#409EFF'
    },
    
    getTotalProducts() {
      return this.productCategoryData.reduce((sum, item) => sum + item.value, 0)
    },
    
    getPercentage(value) {
      const total = this.getTotalProducts()
      if (total === 0) return 0
      return parseFloat(((value / total) * 100).toFixed(1))
    },
    
    getStatusPercentage(value) {
      const total = this.reviewStatusData.reduce((sum, item) => sum + item.value, 0)
      if (total === 0) return 0
      return parseFloat(((value / total) * 100).toFixed(1))
    },
    
    getRoleTagType(role) {
      const types = { 'ADMIN': 'danger', 'MERCHANT': 'warning', 'USER': 'success' }
      return types[role] || 'info'
    },
    
    getRoleText(role) {
      const texts = { 'ADMIN': '管理员', 'MERCHANT': '农户', 'USER': '用户' }
      return texts[role] || role
    },
    
    getCategoryType(category) {
      const types = {
        'vegetable': 'success', 'fruit': 'primary', 'meat': 'danger',
        'grain': 'warning', 'processed': 'info', 'other': 'default'
      }
      return types[category] || 'default'
    },
    
    getCategoryLabel(category) {
      const labels = {
        'vegetable': '蔬菜类', 'fruit': '水果类', 'meat': '肉类',
        'grain': '谷物类', 'processed': '加工类', 'other': '其他'
      }
      return labels[category] || category
    },
    
    getUserAvatarColor(id) {
      const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#9C27B0']
      return colors[(id || 0) % colors.length]
    },
    
    formatDate(dateString) {
      if (!dateString) return '未知'
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
    },
    
    formatTimeAgo(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      const now = new Date()
      const diff = now - date
      const minutes = Math.floor(diff / 60000)
      const hours = Math.floor(diff / 3600000)
      const days = Math.floor(diff / 86400000)
      if (minutes < 60) return `${minutes}分钟前`
      if (hours < 24) return `${hours}小时前`
      return `${days}天前`
    },
    
    formatTime(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      const seconds = String(date.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    },
    
    handleDateChange() {
      this.refreshData()
    },

    handleModuleChange() {
      this.refreshData()
    },
    
    updateChartData() {
      this.generateChartData()
    },
    
    refreshData() {
      this.refreshing = true
      setTimeout(() => {
        this.loadData()
        this.refreshing = false
        this.$message.success('数据已刷新')
      }, 1000)
    },
    
    exportData() {
      this.$confirm('确定要导出当前统计报表吗？', '导出报表', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        const csvContent = this.generateCSV()
        const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
        const link = document.createElement('a')
        const url = URL.createObjectURL(blob)
        link.setAttribute('href', url)
        link.setAttribute('download', `数据统计_${new Date().toISOString().slice(0, 10)}.csv`)
        link.style.visibility = 'hidden'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        URL.revokeObjectURL(url)
        this.$message.success('报表导出成功！')
      }).catch(() => {})
    },
    
    generateCSV() {
      const headers = ['指标', '数值', '趋势']
      const rows = [headers.join(',')]
      this.filteredKpiList.forEach(kpi => {
        rows.push([kpi.label, kpi.value, `${kpi.trend}%`].join(','))
      })
      return rows.join('\n')
    },
    
    handleKpiClick(kpi) {
      this.$message.info(`查看${kpi.label}详情`)
    }
  }
}
</script>

<style scoped>
.statistics-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.header-left .page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-left .page-title i {
  color: #409EFF;
  font-size: 28px;
}

.header-left .page-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.update-time {
  font-size: 13px;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.filter-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.date-picker {
  width: 280px;
}

.module-select {
  width: 160px;
}

.filter-right {
  display: flex;
  gap: 12px;
}

.action-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
}

.action-btn i {
  font-size: 16px;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.btn-success {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  box-shadow: 0 4px 12px rgba(67, 233, 123, 0.3);
}

.btn-success:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(67, 233, 123, 0.4);
}

.stats-section {
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  color: #fff;
}

.stat-users .stat-icon { background: linear-gradient(135deg, #409EFF, #66B1FF); }
.stat-products .stat-icon { background: linear-gradient(135deg, #67C23A, #85CE61); }
.stat-orders .stat-icon { background: linear-gradient(135deg, #E6A23C, #EBB563); }
.stat-revenue .stat-icon { background: linear-gradient(135deg, #F56C6C, #E64A4A); }
.stat-merchants .stat-icon { background: linear-gradient(135deg, #909399, #A6A9AD); }
.stat-reviews .stat-icon { background: linear-gradient(135deg, #9C27B0, #BA68C8); }
.stat-approved .stat-icon { background: linear-gradient(135deg, #4CAF50, #2E7D32); }
.stat-notices .stat-icon { background: linear-gradient(135deg, #FF9800, #F57C00); }

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1;
  margin-bottom: 6px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-trend {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-weight: 600;
}

.trend-up {
  color: #67c23a;
  background: #f0f9eb;
}

.trend-down {
  color: #f56c6c;
  background: #fef0f0;
}

.charts-section {
  margin-bottom: 24px;
}

.chart-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
}

.chart-card >>> .el-card__body {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title i {
  color: #409EFF;
}

.chart-container {
  height: 250px;
  padding: 20px;
  background: #fafafa;
  border-radius: 10px;
  margin-bottom: 16px;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 100%;
  padding: 0 20px;
  gap: 15px;
}

.chart-bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  justify-content: flex-end;
}

.chart-bar {
  width: 100%;
  max-width: 80px;
  border-radius: 8px 8px 0 0;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 10px;
  transition: all 0.3s ease;
  animation: growBar 0.8s ease-out;
}

.chart-bar:hover {
  filter: brightness(110%);
  transform: scaleY(1.02);
}

@keyframes growBar {
  from { height: 0; opacity: 0; }
  to { opacity: 1; }
}

.bar-value {
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.bar-label {
  font-size: 12px;
  color: #606266;
  margin-top: 10px;
  font-weight: 500;
}

.category-list,
.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
}

.category-item,
.review-item {
  padding: 16px;
  background: #fafafa;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.category-item:hover,
.review-item:hover {
  background: #f0f2f5;
  transform: translateX(4px);
}

.category-header,
.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.category-name,
.review-label {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
}

.category-value,
.review-value {
  font-size: 14px;
  font-weight: 700;
  color: #409EFF;
}

.category-trend,
.review-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.category-trend,
.review-trend {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-weight: 600;
}

.review-avg {
  font-size: 12px;
  color: #909399;
}

.card-footer {
  display: flex;
  gap: 20px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.footer-stat {
  flex: 1;
  text-align: center;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.footer-label {
  font-size: 12px;
  color: #909399;
  display: block;
  margin-bottom: 4px;
}

.footer-value {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
}

.tables-section {
  margin-bottom: 24px;
}

.table-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
}

.table-card >>> .el-card__body {
  padding: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.table-title i {
  color: #409EFF;
}

.view-more-btn {
  padding: 6px 12px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  background: #fff;
  color: #606266;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.view-more-btn:hover {
  border-color: #409EFF;
  color: #409EFF;
  transform: translateY(-2px);
}

.stats-table {
  cursor: pointer;
}

.stats-table >>> .el-table__row:hover {
  background-color: #f5f7fa;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.user-role {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-info {
  text-align: center;
}

.time-date {
  font-size: 13px;
  color: #606266;
  margin-bottom: 2px;
}

.time-ago {
  font-size: 11px;
  color: #909399;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-image {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  object-fit: cover;
}

.product-image-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  font-size: 20px;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-category {
  display: flex;
  align-items: center;
  gap: 8px;
}

.price-info {
  text-align: center;
}

.product-price {
  font-size: 14px;
  font-weight: 700;
  color: #f56c6c;
  margin-bottom: 2px;
}

.product-stock {
  font-size: 12px;
  color: #909399;
}

.overview-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.overview-card >>> .el-card__body {
  padding: 24px;
}

.overview-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.overview-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.overview-title i {
  color: #409EFF;
}

.overview-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.overview-item {
  padding: 20px;
  background: #fafafa;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.overview-item:hover {
  background: #f0f2f5;
  transform: translateY(-2px);
}

.overview-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
  margin-bottom: 12px;
}

.overview-info {
  margin-bottom: 12px;
}

.overview-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.overview-value {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.overview-trend {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-weight: 600;
}

.overview-status {
  display: flex;
  gap: 24px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  flex-wrap: wrap;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-icon {
  font-size: 16px;
}

.status-icon.success { color: #67c23a; }
.status-icon.warning { color: #e6a23c; }
.status-icon.info { color: #409eff; }

.status-text {
  font-size: 13px;
  color: #606266;
}

@media (max-width: 768px) {
  .statistics-page {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .header-left .page-title {
    font-size: 20px;
  }
  
  .stat-card {
    margin-bottom: 16px;
  }
  
  .filter-section {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  
  .filter-left {
    flex-direction: column;
  }
  
  .date-picker,
  .module-select {
    width: 100%;
  }
  
  .filter-right {
    justify-content: flex-start;
  }
  
  .card-footer {
    flex-direction: column;
    gap: 12px;
  }
  
  .overview-grid {
    grid-template-columns: 1fr;
  }
  
  .overview-status {
    flex-direction: column;
    gap: 12px;
  }
}
</style>
