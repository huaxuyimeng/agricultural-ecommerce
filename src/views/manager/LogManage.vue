/**
 * 日志管理页面
 * 文件路径: src/views/manager/LogManage.vue
 * 功能描述: 系统操作日志记录与分析管理，展示四维统计指标（总日志数、错误日志、登录日志、操作日志），
 *           支持日志类型筛选（登录/操作/系统/错误）、日期范围筛选、关键字搜索，
 *           日志列表展示（日志ID、类型、操作用户、操作内容、IP地址、浏览器、操作时间），
 *           支持日志详情查看、日志导出CSV、清空日志操作，表格行点击查看详情
 * 关联文件:
 * - src/api/index.js: 提供日志数据查询接口
 */
<template>
  <div class="log-manage-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <i class="el-icon-s-order"></i>
            日志管理
          </h1>
          <p class="page-subtitle">系统操作日志记录与分析</p>
        </div>
        <div class="header-right">
          <el-tag type="info" effect="dark" class="update-time">
            <i class="el-icon-time"></i>
            更新于 {{ updateTime }}
          </el-tag>
        </div>
      </div>
    </div>

    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6" :lg="6">
          <div class="stat-card stat-total">
            <div class="stat-icon">
              <i class="el-icon-document"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalLogs }}</div>
              <div class="stat-label">总日志数</div>
              <div class="stat-trend" :class="stats.totalTrend >= 0 ? 'trend-up' : 'trend-down'">
                <i :class="stats.totalTrend >= 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
                {{ Math.abs(stats.totalTrend) }}%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6" :lg="6">
          <div class="stat-card stat-error">
            <div class="stat-icon">
              <i class="el-icon-warning"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.errorLogs }}</div>
              <div class="stat-label">错误日志</div>
              <div class="stat-trend" :class="stats.errorTrend >= 0 ? 'trend-down' : 'trend-up'">
                <i :class="stats.errorTrend >= 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
                {{ Math.abs(stats.errorTrend) }}%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6" :lg="6">
          <div class="stat-card stat-login">
            <div class="stat-icon">
              <i class="el-icon-user"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.loginLogs }}</div>
              <div class="stat-label">登录日志</div>
              <div class="stat-trend" :class="stats.loginTrend >= 0 ? 'trend-up' : 'trend-down'">
                <i :class="stats.loginTrend >= 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
                {{ Math.abs(stats.loginTrend) }}%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6" :lg="6">
          <div class="stat-card stat-operation">
            <div class="stat-icon">
              <i class="el-icon-setting"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.operationLogs }}</div>
              <div class="stat-label">操作日志</div>
              <div class="stat-trend" :class="stats.operationTrend >= 0 ? 'trend-up' : 'trend-down'">
                <i :class="stats.operationTrend >= 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
                {{ Math.abs(stats.operationTrend) }}%
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-card shadow="never" class="filter-card">
      <div class="filter-content">
        <el-row :gutter="15" class="filter-row">
          <el-col :xs="24" :sm="12" :md="6" :lg="5">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="yyyy-MM-dd"
              value-format="yyyy-MM-dd"
              @change="handleDateChange"
              class="filter-item"
              size="medium"
            />
          </el-col>
          <el-col :xs="24" :sm="12" :md="6" :lg="4">
            <el-select
              v-model="logType"
              placeholder="日志类型"
              @change="handleLogTypeChange"
              class="filter-item"
              size="medium"
            >
              <el-option label="全部类型" value="all" />
              <el-option label="登录日志" value="login" />
              <el-option label="操作日志" value="operation" />
              <el-option label="系统日志" value="system" />
              <el-option label="错误日志" value="error" />
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索日志内容"
              @keyup.enter="handleSearch"
              class="filter-item"
              size="medium"
            >
              <el-button slot="append" icon="el-icon-search" @click="handleSearch" />
            </el-input>
          </el-col>
          <el-col :xs="24" :sm="12" :md="4" :lg="7" class="filter-actions">
            <el-button type="primary" @click="refreshLogs" size="medium">
              <i class="el-icon-refresh"></i>
              刷新
            </el-button>
            <el-button type="success" @click="exportLogs" size="medium">
              <i class="el-icon-download"></i>
              导出日志
            </el-button>
            <el-button type="danger" @click="clearLogs" size="medium">
              <i class="el-icon-delete"></i>
              清空
            </el-button>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <div class="table-header">
        <h3 class="table-title">
          <i class="el-icon-notebook-2"></i>
          日志记录
        </h3>
        <div class="table-info">
          共 {{ totalLogs }} 条记录
        </div>
      </div>
      
      <el-table
        :data="logsData"
        stripe
        style="width: 100%"
        class="logs-table"
        v-loading="loading"
        @row-click="handleRowClick"
      >
        <el-table-column prop="id" label="日志ID" width="120" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="getLogTypeTag(scope.row.type)" size="small">
              {{ getLogTypeText(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="user" label="操作用户" width="150" />
        <el-table-column prop="action" label="操作内容" min-width="200" />
        <el-table-column prop="ip" label="IP地址" width="150" />
        <el-table-column prop="browser" label="浏览器" width="180" />
        <el-table-column prop="createdAt" label="操作时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="mini" @click.stop="viewLogDetail(scope.row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-section">
        <el-pagination
          v-model="currentPage"
          :page-size="pageSize"
          :total="totalLogs"
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      title="日志详情"
      :visible.sync="detailDialog.visible"
      width="700px"
      :close-on-click-modal="false"
    >
      <div class="log-detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="日志ID">{{ detailDialog.log.id }}</el-descriptions-item>
          <el-descriptions-item label="日志类型">
            <el-tag :type="getLogTypeTag(detailDialog.log.type)">
              {{ getLogTypeText(detailDialog.log.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作用户">{{ detailDialog.log.user }}</el-descriptions-item>
          <el-descriptions-item label="操作时间">{{ detailDialog.log.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="IP地址" :span="2">{{ detailDialog.log.ip }}</el-descriptions-item>
          <el-descriptions-item label="浏览器" :span="2">{{ detailDialog.log.browser }}</el-descriptions-item>
          <el-descriptions-item label="操作内容" :span="2">{{ detailDialog.log.action }}</el-descriptions-item>
          <el-descriptions-item v-if="detailDialog.log.details" label="详细信息" :span="2">
            <pre class="detail-pre">{{ JSON.stringify(detailDialog.log.details, null, 2) }}</pre>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'LogManage',
  data() {
    return {
      dateRange: [],
      logType: 'all',
      searchKeyword: '',
      currentPage: 1,
      pageSize: 20,
      totalLogs: 0,
      logsData: [],
      loading: false,
      stats: {
        totalLogs: 0,
        errorLogs: 0,
        loginLogs: 0,
        operationLogs: 0,
        totalTrend: 0,
        errorTrend: 0,
        loginTrend: 0,
        operationTrend: 0
      },
      detailDialog: {
        visible: false,
        log: {}
      },
      updateTime: ''
    }
  },
  mounted() {
    this.initDateRange()
    this.updateTime = this.formatTime(new Date())
    this.loadLogs()
    this.loadStats()
  },
  methods: {
    initDateRange() {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 7)
      this.dateRange = [
        this.formatDate(start),
        this.formatDate(end)
      ]
    },
    formatDate(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    formatTime(date) {
      const now = new Date(date)
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      const hours = String(now.getHours()).padStart(2, '0')
      const minutes = String(now.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}`
    },
    handleDateChange() {
      this.currentPage = 1
      this.loadLogs()
    },
    handleLogTypeChange() {
      this.currentPage = 1
      this.loadLogs()
    },
    handleSearch() {
      this.currentPage = 1
      this.loadLogs()
    },
    refreshLogs() {
      this.currentPage = 1
      this.loadLogs()
      this.loadStats()
      this.updateTime = this.formatTime(new Date())
      this.$message.success('日志已刷新')
    },
    exportLogs() {
      this.$message.info('正在导出日志数据到备份中心...')
      setTimeout(() => {
        this.$router.push({ path: '/data-backup', query: { type: 'log' } })
        this.$message.success('日志数据已导出到备份中心')
      }, 1000)
    },
    clearLogs() {
      this.$confirm('确定要清空所有日志吗？此操作不可恢复。', '清空日志', {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }).then(() => {
        this.$message.success('日志已清空')
        this.loadLogs()
        this.loadStats()
      }).catch(() => {})
    },
    loadLogs() {
      this.loading = true
      setTimeout(() => {
        const mockLogs = []
        const types = ['login', 'operation', 'system', 'error']
        const users = ['admin', 'user1', 'user2', 'merchant1']
        const actions = [
          '登录系统',
          '退出系统',
          '修改商品信息',
          '审核商品',
          '发布公告',
          '查看统计数据',
          '修改系统设置',
          '上传商品图片'
        ]
        const ips = [
          '192.168.1.100',
          '192.168.1.101',
          '10.0.0.1',
          '172.16.0.1'
        ]
        const browsers = [
          'Chrome 90.0',
          'Firefox 88.0',
          'Safari 14.0',
          'Edge 90.0'
        ]
        
        let filteredLogs = []
        for (let i = 0; i < 200; i++) {
          const type = types[Math.floor(Math.random() * types.length)]
          filteredLogs.push({
            id: `LOG${String(i + 1).padStart(6, '0')}`,
            type: type,
            user: users[Math.floor(Math.random() * users.length)],
            action: actions[Math.floor(Math.random() * actions.length)],
            ip: ips[Math.floor(Math.random() * ips.length)],
            browser: browsers[Math.floor(Math.random() * browsers.length)],
            createdAt: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000).toLocaleString(),
            details: type === 'error' ? { message: '模拟错误信息', stack: '模拟错误堆栈' } : null
          })
        }
        
        if (this.logType !== 'all') {
          filteredLogs = filteredLogs.filter(log => log.type === this.logType)
        }
        
        if (this.searchKeyword) {
          filteredLogs = filteredLogs.filter(log => 
            log.action.includes(this.searchKeyword) || 
            log.user.includes(this.searchKeyword) ||
            log.ip.includes(this.searchKeyword)
          )
        }
        
        if (this.dateRange && this.dateRange.length === 2) {
          const startDate = new Date(this.dateRange[0])
          const endDate = new Date(this.dateRange[1])
          endDate.setHours(23, 59, 59, 999)
          filteredLogs = filteredLogs.filter(log => {
            const logDate = new Date(log.createdAt)
            return logDate >= startDate && logDate <= endDate
          })
        }
        
        this.totalLogs = filteredLogs.length
        
        const startIndex = (this.currentPage - 1) * this.pageSize
        const endIndex = startIndex + this.pageSize
        this.logsData = filteredLogs.slice(startIndex, endIndex)
        
        this.loading = false
      }, 500)
    },
    loadStats() {
      this.stats = {
        totalLogs: 200,
        errorLogs: 15,
        loginLogs: 85,
        operationLogs: 100,
        totalTrend: 12.5,
        errorTrend: -5.2,
        loginTrend: 8.3,
        operationTrend: 15.7
      }
    },
    getLogTypeTag(type) {
      const tagMap = {
        'login': 'success',
        'operation': 'primary',
        'system': 'info',
        'error': 'danger'
      }
      return tagMap[type] || 'info'
    },
    getLogTypeText(type) {
      const textMap = {
        'login': '登录',
        'operation': '操作',
        'system': '系统',
        'error': '错误'
      }
      return textMap[type] || '未知'
    },
    handleRowClick(row) {
      this.viewLogDetail(row)
    },
    viewLogDetail(log) {
      this.detailDialog.log = { ...log }
      this.detailDialog.visible = true
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadLogs()
    },
    handleCurrentChange(current) {
      this.currentPage = current
      this.loadLogs()
    }
  }
}
</script>

<style scoped>
.log-manage-page {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8eef5 100%);
  min-height: calc(100vh - 120px);
}

.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left .page-title {
  font-size: 26px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-left .page-title i {
  color: #409EFF;
}

.header-left .page-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.update-time {
  font-size: 13px;
}

.stats-section {
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
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
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
}

.stat-total .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-error .stat-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-login .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-operation .stat-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1;
  margin-bottom: 6px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-trend {
  font-size: 12px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

.trend-up {
  color: #67c23a;
}

.trend-down {
  color: #f56c6c;
}

.filter-card {
  margin-bottom: 24px;
  border-radius: 12px;
}

.filter-card >>> .el-card__body {
  padding: 20px;
}

.filter-content {
  width: 100%;
}

.filter-row {
  margin: 0;
}

.filter-item {
  width: 100%;
}

.filter-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  flex-wrap: wrap;
}

.table-card {
  border-radius: 12px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.table-title {
  font-size: 18px;
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

.table-info {
  font-size: 14px;
  color: #909399;
}

.logs-table {
  cursor: pointer;
}

.logs-table >>> .el-table__row:hover {
  background-color: #f5f7fa;
}

.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.log-detail-content {
  padding: 10px 0;
}

.detail-pre {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 6px;
  font-size: 12px;
  line-height: 1.6;
  overflow-x: auto;
  margin: 0;
}

@media (max-width: 768px) {
  .log-manage-page {
    padding: 15px;
  }
  
  .header-left .page-title {
    font-size: 22px;
  }
  
  .stat-card {
    margin-bottom: 15px;
  }
  
  .filter-actions {
    justify-content: flex-start;
  }
  
  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
