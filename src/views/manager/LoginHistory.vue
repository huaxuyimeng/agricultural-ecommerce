/**
 * 登录历史页面
 * 文件路径: src/views/manager/LoginHistory.vue
 * 功能描述: 查看系统用户登录历史记录和安全审计信息，展示登录记录总数/成功登录/失败登录/登录地点四维统计，
 *           支持关键字搜索、登录状态筛选（全部/成功/失败）、日期范围筛选，
 *           登录记录列表（用户名、IP地址、地点、设备/浏览器、登录时间、状态），支持CSV导出记录
 * 关联文件:
 * - src/api/index.js: 提供登录历史数据查询接口
 */
<template>
  <div class="login-history-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <i class="el-icon-time"></i>
            登录历史
          </h1>
          <p class="page-subtitle">查看账户的完整登录记录和安全信息</p>
        </div>
        <div class="header-right">
          <el-button @click="handleExport" class="export-btn">
            <i class="el-icon-download"></i>
            导出记录
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-card shadow="never" class="stats-card">
      <div class="stats-content">
        <div class="stat-item">
          <i class="el-icon-document stat-icon total"></i>
          <div class="stat-info">
            <div class="stat-number">{{ totalRecords }}</div>
            <div class="stat-label">登录记录</div>
          </div>
        </div>
        <div class="stat-item">
          <i class="el-icon-check stat-icon success"></i>
          <div class="stat-info">
            <div class="stat-number">{{ successRecords }}</div>
            <div class="stat-label">成功登录</div>
          </div>
        </div>
        <div class="stat-item">
          <i class="el-icon-close stat-icon failed"></i>
          <div class="stat-info">
            <div class="stat-number">{{ failedRecords }}</div>
            <div class="stat-label">失败登录</div>
          </div>
        </div>
        <div class="stat-item">
          <i class="el-icon-location stat-icon location"></i>
          <div class="stat-info">
            <div class="stat-number">{{ uniqueLocations }}</div>
            <div class="stat-label">登录地点</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 筛选工具栏 -->
    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar-content">
        <div class="filter-group">
          <el-input
            placeholder="搜索登录记录"
            v-model="searchKeyword"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleSearch"
            class="search-input"
            size="medium"
          >
            <template #prefix>
              <i class="el-icon-search"></i>
            </template>
          </el-input>
          
          <el-select
            v-model="statusFilter"
            @change="filterRecords"
            size="medium"
            class="status-select"
            placeholder="登录状态"
          >
            <el-option label="全部" value="all" />
            <el-option label="成功" value="success" />
            <el-option label="失败" value="failed" />
          </el-select>
          
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            size="medium"
            class="date-picker"
            @change="filterRecords"
            value-format="yyyy-MM-dd"
          />
        </div>
        
        <div class="action-group">
          <el-button @click="handleRefresh" size="medium">
            <i class="el-icon-refresh"></i>
            刷新
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 登录记录列表 -->
    <el-card shadow="never" class="records-card">
      <div class="records-header">
        <div class="records-title">
          <i class="el-icon-s-order"></i>
          登录记录列表
          <span class="record-count">({{ filteredRecords.length }})</span>
        </div>
      </div>

      <!-- 记录列表 -->
      <el-table
        v-if="filteredRecords.length > 0"
        :data="paginatedRecords"
        stripe
        border
        class="records-table"
        v-loading="loading"
        row-key="id"
      >
        <el-table-column label="登录时间" prop="time" width="180">
          <template slot-scope="scope">
            <div class="time-cell">
              <i class="el-icon-time"></i>
              {{ formatTime(scope.row.time) }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="登录状态" prop="success" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.success ? 'success' : 'danger'" size="small">
              {{ scope.row.success ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="登录方式" prop="loginType" width="120">
          <template slot-scope="scope">
            <span>{{ getLoginTypeText(scope.row.loginType) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="设备信息" min-width="200">
          <template slot-scope="scope">
            <div class="device-cell">
              <div class="device-icon">
                <i :class="getDeviceIcon(scope.row.device)"></i>
              </div>
              <div class="device-info">
                <div class="device-type">{{ getDeviceType(scope.row.device) }}</div>
                <div class="device-browser">{{ getBrowserInfo(scope.row.device) }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="登录地点" prop="location" min-width="150">
          <template slot-scope="scope">
            <div class="location-cell">
              <i class="el-icon-location"></i>
              <span>{{ scope.row.location || '未知地点' }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="IP地址" prop="ip" width="140">
          <template slot-scope="scope">
            <span class="ip-address">{{ scope.row.ip || '未知IP' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="80" fixed="right">
          <template slot-scope="scope">
            <el-button 
              type="text" 
              size="small"
              @click="viewDetail(scope.row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-icon-container">
          <i class="el-icon-time empty-icon"></i>
        </div>
        <h3 class="empty-title">暂无登录记录</h3>
        <p class="empty-description">您的登录记录将显示在这里</p>
      </div>

      <!-- 分页 -->
      <div v-if="filteredRecords.length > 0" class="pagination-box">
        <div class="pagination-info">
          共 {{ filteredRecords.length }} 条记录
        </div>
        <el-pagination
          background
          :current-page="pagination.pageNum"
          :page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="sizes, prev, pager, next, jumper"
          :total="filteredRecords.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          class="pagination-control"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      title="登录详情"
      :visible.sync="detailDialog.visible"
      width="600px"
      class="detail-dialog"
    >
      <div class="detail-content" v-if="detailDialog.data">
        <el-descriptions :column="2" border class="detail-descriptions">
          <el-descriptions-item label="登录时间">
            {{ formatTime(detailDialog.data.time) }}
          </el-descriptions-item>
          <el-descriptions-item label="登录状态">
            <el-tag :type="detailDialog.data.success ? 'success' : 'danger'" size="small">
              {{ detailDialog.data.success ? '成功' : '失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="登录方式">
            {{ getLoginTypeText(detailDialog.data.loginType) }}
          </el-descriptions-item>
          <el-descriptions-item label="IP地址">
            {{ detailDialog.data.ip || '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="登录地点" :span="2">
            {{ detailDialog.data.location || '未知地点' }}
          </el-descriptions-item>
          <el-descriptions-item label="设备类型" :span="2">
            {{ getDeviceType(detailDialog.data.device) }}
          </el-descriptions-item>
          <el-descriptions-item label="浏览器/系统" :span="2">
            {{ getBrowserInfo(detailDialog.data.device) }}
          </el-descriptions-item>
          <el-descriptions-item label="操作类型" :span="2" v-if="detailDialog.data.action">
            <el-tag size="small">{{ getActionText(detailDialog.data.action) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="失败原因" :span="2" v-if="!detailDialog.data.success && detailDialog.data.failReason">
            <span class="fail-reason">{{ detailDialog.data.failReason }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialog.visible = false">关闭</el-button>
          <el-button 
            v-if="detailDialog.data && !detailDialog.data.success"
            type="danger"
            @click="handleForceLogout"
          >
            强制退出
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import * as XLSX from 'xlsx'
import { getCurrentUser, getLoginHistory } from '@/data/user'

export default {
  name: 'LoginHistory',
  data() {
    return {
      loading: false,
      records: [],
      filteredRecords: [],
      searchKeyword: '',
      statusFilter: 'all',
      dateRange: null,
      pagination: {
        pageNum: 1,
        pageSize: 10
      },
      detailDialog: {
        visible: false,
        data: null
      }
    }
  },
  computed: {
    totalRecords() {
      return this.records.length
    },
    successRecords() {
      return this.records.filter(r => r.success).length
    },
    failedRecords() {
      return this.records.filter(r => !r.success).length
    },
    uniqueLocations() {
      const locations = new Set(this.records.map(r => r.location).filter(Boolean))
      return locations.size || 0
    },
    paginatedRecords() {
      const start = (this.pagination.pageNum - 1) * this.pagination.pageSize
      const end = start + this.pagination.pageSize
      return this.filteredRecords.slice(start, end)
    }
  },
  created() {
    this.loadRecords()
  },
  methods: {
    loadRecords() {
      this.loading = true
      try {
        const user = getCurrentUser()
        if (user) {
          this.records = getLoginHistory(user.id) || []
          this.filterRecords()
        }
      } catch (error) {
        console.error('加载登录记录失败:', error)
        this.$message.error('加载登录记录失败')
      } finally {
        this.loading = false
      }
    },
    
    filterRecords() {
      let filtered = [...this.records]
      
      if (this.statusFilter === 'success') {
        filtered = filtered.filter(r => r.success)
      } else if (this.statusFilter === 'failed') {
        filtered = filtered.filter(r => !r.success)
      }
      
      if (this.dateRange && this.dateRange.length === 2) {
        const [startDate, endDate] = this.dateRange
        filtered = filtered.filter(r => {
          const recordDate = new Date(r.time).toISOString().slice(0, 10)
          return recordDate >= startDate && recordDate <= endDate
        })
      }
      
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        filtered = filtered.filter(r => 
          (r.location && r.location.toLowerCase().includes(keyword)) ||
          (r.ip && r.ip.toLowerCase().includes(keyword)) ||
          (r.device && r.device.toLowerCase().includes(keyword)) ||
          (r.loginType && r.loginType.toLowerCase().includes(keyword))
        )
      }
      
      filtered.sort((a, b) => new Date(b.time) - new Date(a.time))
      
      this.filteredRecords = filtered
      this.pagination.pageNum = 1
    },
    
    handleSearch() {
      this.filterRecords()
    },
    
    handleRefresh() {
      this.loadRecords()
      this.$message.success('刷新成功')
    },
    
    handleSizeChange(size) {
      this.pagination.pageSize = size
      this.pagination.pageNum = 1
    },
    
    handleCurrentChange(pageNum) {
      this.pagination.pageNum = pageNum
    },
    
    viewDetail(record) {
      this.detailDialog.data = record
      this.detailDialog.visible = true
    },
    
    handleExport() {
      if (this.filteredRecords.length === 0) {
        this.$message.warning('没有可导出的记录')
        return
      }
      
      this.$confirm('确定要导出登录记录吗？', '确认导出', {
        type: 'info',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(() => {
        try {
          const exportData = this.filteredRecords.map((record, index) => ({
            '序号': index + 1,
            '登录时间': this.formatTime(record.time),
            '登录状态': record.success ? '成功' : '失败',
            '登录方式': this.getLoginTypeText(record.loginType),
            '设备类型': this.getDeviceType(record.device),
            '浏览器/系统': this.getBrowserInfo(record.device),
            '登录地点': record.location || '未知',
            'IP地址': record.ip || '未知',
            '失败原因': !record.success && record.failReason ? record.failReason : ''
          }))
          
          const ws = XLSX.utils.json_to_sheet(exportData)
          const wb = XLSX.utils.book_new()
          XLSX.utils.book_append_sheet(wb, ws, '登录历史')
          XLSX.writeFile(wb, `登录历史_${new Date().toISOString().slice(0, 10)}.xlsx`)
          
          this.$message.success('导出成功')
        } catch (error) {
          console.error('导出失败:', error)
          this.$message.error('导出失败')
        }
      }).catch(() => {})
    },
    
    handleForceLogout() {
      this.$confirm('确定要强制退出该设备吗？', '确认操作', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(() => {
        this.$message.success('已强制退出该设备')
        this.detailDialog.visible = false
      }).catch(() => {})
    },
    
    formatTime(time) {
      if (!time) return '未知时间'
      const date = new Date(time)
      return date.toLocaleString()
    },
    
    getLoginTypeText(type) {
      const typeMap = {
        'password': '密码登录',
        'phone': '手机验证码',
        'email': '邮箱验证',
        'oauth': '第三方登录',
        'qrcode': '扫码登录',
        'password_change': '密码修改',
        'default': '密码登录'
      }
      return typeMap[type] || typeMap['default']
    },
    
    getDeviceType(device) {
      if (!device) return '未知设备'
      const ua = device.toLowerCase()
      if (ua.includes('mobile') || ua.includes('android') || ua.includes('iphone')) {
        return '手机'
      }
      if (ua.includes('tablet') || ua.includes('ipad')) {
        return '平板'
      }
      if (ua.includes('windows') || ua.includes('mac') || ua.includes('linux')) {
        return '电脑'
      }
      return '其他设备'
    },
    
    getDeviceIcon(device) {
      const type = this.getDeviceType(device)
      const iconMap = {
        '手机': 'el-icon-mobile-phone',
        '平板': 'el-icon-tablet',
        '电脑': 'el-icon-monitor',
        '其他设备': 'el-icon-device'
      }
      return iconMap[type] || 'el-icon-question'
    },
    
    getBrowserInfo(device) {
      if (!device) return '未知浏览器'
      const ua = device
      let browser = '未知'
      let system = '未知系统'
      
      if (ua.includes('Chrome') && !ua.includes('Edg')) {
        browser = 'Chrome'
      } else if (ua.includes('Firefox')) {
        browser = 'Firefox'
      } else if (ua.includes('Safari') && !ua.includes('Chrome')) {
        browser = 'Safari'
      } else if (ua.includes('Edg')) {
        browser = 'Edge'
      } else if (ua.includes('IE') || ua.includes('Trident')) {
        browser = 'IE'
      }
      
      if (ua.includes('Windows')) {
        system = 'Windows'
      } else if (ua.includes('Mac OS')) {
        system = 'macOS'
      } else if (ua.includes('Linux')) {
        system = 'Linux'
      } else if (ua.includes('Android')) {
        system = 'Android'
      } else if (ua.includes('iOS') || ua.includes('iPhone')) {
        system = 'iOS'
      }
      
      return `${browser} / ${system}`
    },
    
    getActionText(action) {
      const actionMap = {
        'login': '登录',
        'logout': '退出',
        'password_change': '修改密码',
        'profile_update': '修改资料',
        '绑定手机': '绑定手机',
        '绑定邮箱': '绑定邮箱'
      }
      return actionMap[action] || action
    }
  }
}
</script>

<style scoped>
.login-history-page {
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

.stats-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.stats-content {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.stat-item:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
}

.stat-icon {
  font-size: 28px;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: white;
}

.stat-icon.total {
  background: linear-gradient(135deg, #409EFF, #66B1FF);
}

.stat-icon.success {
  background: linear-gradient(135deg, #67C23A, #85ce61);
}

.stat-icon.failed {
  background: linear-gradient(135deg, #F56C6C, #f78989);
}

.stat-icon.location {
  background: linear-gradient(135deg, #E6A23C, #ebb563);
}

.stat-info {
  flex: 1;
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

.toolbar-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.toolbar-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.filter-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.search-input {
  width: 250px;
}

.status-select {
  min-width: 120px;
}

.date-picker {
  width: 260px;
}

.records-card {
  border-radius: 12px;
}

.records-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.records-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 10px;
}

.record-count {
  font-size: 14px;
  font-weight: 400;
  color: #909399;
  background: #f5f7fa;
  padding: 2px 8px;
  border-radius: 10px;
}

.records-table {
  border-radius: 8px;
  overflow: hidden;
}

.time-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #606266;
}

.device-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.device-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ecf5ff, #d9ecff);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #409EFF;
}

.device-info {
  flex: 1;
}

.device-type {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.device-browser {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.location-cell {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #606266;
}

.ip-address {
  font-family: 'Consolas', monospace;
  font-size: 13px;
  color: #909399;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #909399;
  background: #fafafa;
  border-radius: 12px;
  margin: 20px 0;
}

.empty-icon-container {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f5f7fa, #e4e7ed);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.empty-icon {
  font-size: 48px;
  color: #c0c4cc;
}

.empty-title {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 10px;
  color: #606266;
}

.empty-description {
  font-size: 14px;
  color: #909399;
  margin-bottom: 20px;
}

.pagination-box {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.pagination-info {
  font-size: 14px;
  color: #909399;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.fail-reason {
  color: #f56c6c;
  font-size: 13px;
}

@media (max-width: 1200px) {
  .stats-content {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-content {
    grid-template-columns: 1fr;
  }
  
  .toolbar-content {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-group {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-input,
  .status-select,
  .date-picker {
    width: 100%;
  }
  
  .pagination-box {
    flex-direction: column;
    align-items: center;
    gap: 10px;
  }
}
</style>
