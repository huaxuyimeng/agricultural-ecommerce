/**
 * 商家管理页面
 * 文件路径: src/views/manager/Merchant.vue
 * 功能描述: 管理系统注册商家信息，展示商家总数/营业中/已禁用/待审核四维统计卡片（含增长率），
 *           支持按商家名称、联系人搜索，按商家状态筛选（营业中/已禁用/待审核），日期范围筛选，
 *           商家列表展示（商家名称、联系人、电话、邮箱、状态、注册时间），支持查看/编辑/启用/禁用操作，分页展示
 * 关联文件:
 * - src/api/index.js: 提供商家数据增删改查接口
 * - src/views/manager/Admin.vue: 管理员管理页面
 */
<template>
  <div class="merchant-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <i class="el-icon-shopping-bag-2"></i>
            商家管理
          </h1>
          <p class="page-subtitle">商家信息管理与审核</p>
        </div>
        <div class="header-right">
          <el-tag type="success" effect="dark">
            <i class="el-icon-user"></i>
            商家总数: {{ merchantStats.total }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-content">
        <div class="filter-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索商家名称、联系人"
            @keyup.enter="handleSearch"
            class="search-input"
            size="medium"
          >
            <el-button
              slot="append"
              icon="el-icon-search"
              @click="handleSearch"
            />
          </el-input>
          
          <el-select
            v-model="filterStatus"
            placeholder="商家状态"
            @change="handleFilterChange"
            class="status-select"
            size="medium"
          >
            <el-option label="全部状态" value="all" />
            <el-option label="营业中" value="active" />
            <el-option label="已禁用" value="disabled" />
            <el-option label="待审核" value="pending" />
          </el-select>
          
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
            size="medium"
          />
        </div>
        <div class="filter-right">
          <el-button type="primary" size="medium" @click="refreshData">
            <i class="el-icon-refresh"></i>
            刷新
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 商家统计 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6" :lg="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ merchantStats.total }}</div>
              <div class="stat-label">商家总数</div>
              <div class="stat-trend positive">
                <i class="el-icon-top"></i>
                {{ merchantStats.totalGrowth }}%
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6" :lg="6">
          <el-card shadow="never" class="stat-card success">
            <div class="stat-content">
              <div class="stat-number">{{ merchantStats.active }}</div>
              <div class="stat-label">营业中</div>
              <div class="stat-trend positive">
                <i class="el-icon-top"></i>
                {{ merchantStats.activeGrowth }}%
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6" :lg="6">
          <el-card shadow="never" class="stat-card warning">
            <div class="stat-content">
              <div class="stat-number">{{ merchantStats.pending }}</div>
              <div class="stat-label">待审核</div>
              <div class="stat-trend" :class="merchantStats.pendingGrowth >= 0 ? 'negative' : 'positive'">
                {{ merchantStats.pendingGrowth >= 0 ? '+' : '' }}{{ merchantStats.pendingGrowth }}%
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6" :lg="6">
          <el-card shadow="never" class="stat-card danger">
            <div class="stat-content">
              <div class="stat-number">{{ merchantStats.disabled }}</div>
              <div class="stat-label">已禁用</div>
              <div class="stat-trend" :class="merchantStats.disabledGrowth >= 0 ? 'negative' : 'positive'">
                {{ merchantStats.disabledGrowth >= 0 ? '+' : '' }}{{ merchantStats.disabledGrowth }}%
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 商家列表 -->
    <div class="merchant-list-section">
      <el-card shadow="never" class="merchant-list-card">
        <template #header>
          <div class="merchant-header">
            <h3 class="merchant-title">
              <i class="el-icon-s-shop"></i>
              商家列表
            </h3>
          </div>
        </template>
        <div class="merchant-content">
          <el-table :data="merchantList" stripe style="width: 100%">
            <el-table-column prop="id" label="商家ID" width="100" />
            <el-table-column prop="name" label="商家名称" min-width="180">
              <template #default="scope">
                <div class="merchant-name-cell">
                  <el-avatar :size="40" :src="scope.row.avatar" class="merchant-avatar">
                    {{ scope.row.name.charAt(0) }}
                  </el-avatar>
                  <div class="merchant-info">
                    <div class="merchant-name">{{ scope.row.name }}</div>
                    <div class="merchant-category">{{ scope.row.category }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="contact" label="联系人" width="120" />
            <el-table-column prop="phone" label="联系电话" width="140" />
            <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)" size="small">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="rating" label="评分" width="100">
              <template #default="scope">
                <div class="rating-cell">
                  <el-rate
                    v-model="scope.row.rating"
                    disabled
                    show-score
                    :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                    size="small"
                  />
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="注册时间" width="180" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button type="text" size="small" @click="viewMerchantDetail(scope.row)">
                  详情
                </el-button>
                <el-button
                  v-if="scope.row.status === 'pending'"
                  type="text"
                  size="small"
                  @click="approveMerchant(scope.row)"
                >
                  审核
                </el-button>
                <el-button
                  v-if="scope.row.status === 'active'"
                  type="text"
                  size="small"
                  @click="disableMerchant(scope.row)"
                >
                  禁用
                </el-button>
                <el-button
                  v-if="scope.row.status === 'disabled'"
                  type="text"
                  size="small"
                  @click="enableMerchant(scope.row)"
                >
                  启用
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-section">
            <el-pagination
              v-model="currentPage"
              :page-size="pageSize"
              :total="totalMerchants"
              layout="total, sizes, prev, pager, next, jumper"
              :page-sizes="[10, 20, 50, 100]"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </el-card>
    </div>

    <!-- 商家详情对话框 -->
    <el-dialog
      title="商家详情"
      :visible.sync="detailDialog.visible"
      width="800px"
    >
      <div class="merchant-detail-content" v-if="detailDialog.merchant">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商家ID">
            {{ detailDialog.merchant.id }}
          </el-descriptions-item>
          <el-descriptions-item label="商家名称">
            {{ detailDialog.merchant.name }}
          </el-descriptions-item>
          <el-descriptions-item label="联系人">
            {{ detailDialog.merchant.contact }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ detailDialog.merchant.phone }}
          </el-descriptions-item>
          <el-descriptions-item label="电子邮箱" :span="2">
            {{ detailDialog.merchant.email }}
          </el-descriptions-item>
          <el-descriptions-item label="商家地址" :span="2">
            {{ detailDialog.merchant.address }}
          </el-descriptions-item>
          <el-descriptions-item label="商家类目">
            {{ detailDialog.merchant.category }}
          </el-descriptions-item>
          <el-descriptions-item label="营业执照">
            {{ detailDialog.merchant.license }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(detailDialog.merchant.status)" size="small">
              {{ getStatusText(detailDialog.merchant.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="商家评分">
            <el-rate
              v-model="detailDialog.merchant.rating"
              disabled
              show-score
              :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
              size="small"
            />
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">
            {{ detailDialog.merchant.createdAt }}
          </el-descriptions-item>
          <el-descriptions-item label="最近登录">
            {{ detailDialog.merchant.lastLogin }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailDialog.visible = false">关闭</el-button>
        <el-button
          v-if="detailDialog.merchant && detailDialog.merchant.status === 'pending'"
          type="primary"
          @click="approveMerchant(detailDialog.merchant)"
        >
          审核通过
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getUserPage, updateUser } from '@/api'

export default {
  name: 'Merchant',
  data() {
    return {
      searchKeyword: '',
      filterStatus: 'all',
      dateRange: [],
      currentPage: 1,
      pageSize: 10,
      totalMerchants: 0,
      merchantList: [],
      loading: false,
      merchantStats: {
        total: 0,
        active: 0,
        pending: 0,
        disabled: 0
      },
      detailDialog: {
        visible: false,
        merchant: null
      }
    }
  },
  mounted() {
    this.loadMerchantList()
    this.loadMerchantStats()
  },
  methods: {
    async loadMerchantList() {
      this.loading = true
      try {
        const params = {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          role: 'MERCHANT'
        }
        
        if (this.searchKeyword) {
          params.username = this.searchKeyword
        }
        
        if (this.filterStatus !== 'all') {
          params.status = this.filterStatus === 'active' ? 1 : 0
        }
        
        if (this.dateRange && this.dateRange.length === 2) {
          params.startTime = this.dateRange[0]
          params.endTime = this.dateRange[1]
        }
        
        const res = await getUserPage(params)
        
        let list = []
        if (res?.data?.list) list = res.data.list
        else if (res?.data?.records) list = res.data.records
        else if (res?.data) list = Array.isArray(res.data) ? res.data : []
        
        this.merchantList = list.map(item => ({
          ...item,
          name: item.name || item.username,
          contact: item.name || '-',
          category: item.category || '未分类',
          status: item.status === 1 ? 'active' : 'disabled',
          rating: item.rating || 4.5,
          createdAt: item.createTime,
          lastLogin: item.lastLoginTime || '-'
        }))
        
        this.totalMerchants = res?.data?.total || 0
      } catch (error) {
        console.error('加载商家列表失败:', error)
        this.$message.error('加载商家列表失败')
      } finally {
        this.loading = false
      }
    },

    async loadMerchantStats() {
      try {
        const res = await getUserPage({ pageNum: 1, pageSize: 1, role: 'MERCHANT' })
        const total = res?.data?.total || 0
        
        const activeRes = await getUserPage({ pageNum: 1, pageSize: 1, role: 'MERCHANT', status: 1 })
        const active = activeRes?.data?.total || 0
        
        const pendingRes = await getUserPage({ pageNum: 1, pageSize: 1, role: 'MERCHANT', approvalStatus: 'PENDING' })
        const pending = pendingRes?.data?.total || 0
        
        const disabledRes = await getUserPage({ pageNum: 1, pageSize: 1, role: 'MERCHANT', status: 0 })
        const disabled = disabledRes?.data?.total || 0
        
        this.merchantStats = {
          total,
          active,
          pending,
          disabled
        }
      } catch (error) {
        console.error('加载商家统计失败:', error)
      }
    },

    handleSearch() {
      this.currentPage = 1
      this.loadMerchantList()
    },

    handleFilterChange() {
      this.currentPage = 1
      this.loadMerchantList()
    },

    handleDateChange() {
      this.currentPage = 1
      this.loadMerchantList()
    },

    refreshData() {
      this.loadMerchantList()
      this.loadMerchantStats()
      this.$message.success('数据已刷新')
    },

    getStatusType(status) {
      const typeMap = {
        'active': 'success',
        'pending': 'warning',
        'disabled': 'danger'
      }
      return typeMap[status] || 'info'
    },

    getStatusText(status) {
      const textMap = {
        'active': '营业中',
        'pending': '待审核',
        'disabled': '已禁用'
      }
      return textMap[status] || '未知'
    },

    viewMerchantDetail(merchant) {
      this.detailDialog.merchant = { ...merchant }
      this.detailDialog.visible = true
    },

    async approveMerchant(merchant) {
      try {
        await this.$confirm(`确定要审核通过商家 "${merchant.name}" 吗？`, '审核确认', {
          confirmButtonText: '审核通过',
          cancelButtonText: '取消',
          type: 'success',
          center: true
        })
        
        await updateUser(merchant.id, { approvalStatus: 'APPROVED', status: 1 })
        this.$message.success('商家审核通过！')
        this.detailDialog.visible = false
        this.loadMerchantList()
        this.loadMerchantStats()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('操作失败')
        }
      }
    },

    async disableMerchant(merchant) {
      try {
        await this.$confirm(`确定要禁用商家 "${merchant.name}" 吗？`, '禁用确认', {
          confirmButtonText: '确定禁用',
          cancelButtonText: '取消',
          type: 'warning',
          center: true
        })
        
        await updateUser(merchant.id, { status: 0 })
        this.$message.success('商家已禁用')
        this.loadMerchantList()
        this.loadMerchantStats()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('操作失败')
        }
      }
    },

    async enableMerchant(merchant) {
      try {
        await this.$confirm(`确定要启用商家 "${merchant.name}" 吗？`, '启用确认', {
          confirmButtonText: '确定启用',
          cancelButtonText: '取消',
          type: 'success',
          center: true
        })
        
        await updateUser(merchant.id, { status: 1 })
        this.$message.success('商家已启用')
        this.loadMerchantList()
        this.loadMerchantStats()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('操作失败')
        }
      }
    },

    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadMerchantList()
    },

    handleCurrentChange(current) {
      this.currentPage = current
      this.loadMerchantList()
    }
  }
}
</script>

<style scoped>
.merchant-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.page-header {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left .page-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 5px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-left .page-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.filter-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.filter-left {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.search-input {
  width: 300px;
}

.status-select {
  width: 160px;
}

.date-picker {
  width: 280px;
}

.stats-section {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
  border-left: 4px solid #409eff;
}

.stat-card.success {
  border-left-color: #67c23a;
}

.stat-card.warning {
  border-left-color: #e6a23c;
}

.stat-card.danger {
  border-left-color: #f56c6c;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  padding: 20px;
  text-align: center;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.stat-trend {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  display: inline-block;
}

.stat-trend.positive {
  color: #67c23a;
  background-color: #f0f9eb;
}

.stat-trend.negative {
  color: #f56c6c;
  background-color: #fef0f0;
}

.merchant-list-section {
  margin-top: 20px;
}

.merchant-list-card {
  border-radius: 8px;
  overflow: hidden;
}

.merchant-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.merchant-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.merchant-content {
  padding: 20px 0;
}

.merchant-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.merchant-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: bold;
  font-size: 16px;
}

.merchant-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.merchant-name {
  font-weight: 600;
  color: #303133;
}

.merchant-category {
  font-size: 12px;
  color: #909399;
}

.rating-cell {
  display: flex;
  align-items: center;
}

.pagination-section {
  text-align: right;
  margin-top: 20px;
}

.merchant-detail-content {
  padding: 20px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .filter-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-left {
    width: 100%;
  }

  .search-input,
  .status-select,
  .date-picker {
    width: 100%;
  }

  .stats-section .el-col {
    margin-bottom: 15px;
  }
}
</style>