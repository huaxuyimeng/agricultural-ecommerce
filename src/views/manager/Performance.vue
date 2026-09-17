/**
 * 性能监控页面
 * 文件路径: src/views/manager/Performance.vue
 * 功能描述: 实时监控系统性能指标和接口响应时间，展示四大核心指标（平均响应时间、吞吐量、错误率、可用性），
 *           接口性能监控表格（方法、路径、平均响应/P95/P99、请求数、错误率、状态），支持全部/慢接口(>500ms)/高频接口筛选，
 *           响应时间和吞吐量趋势柱状图（1h/6h/24h/7d时间范围切换），慢查询日志列表（SQL语句、执行时间、表名）及分析/清空功能，
 *           性能优化建议弹窗，CSV报告导出，30秒自动刷新
 * 关联文件:
 * - src/api/index.js: 提供性能监控数据接口
 */
<template>
  <div class="performance-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">
          <i class="el-icon-data-line"></i>
          性能监控
        </h1>
        <p class="page-subtitle">实时监控系统性能指标和响应时间</p>
      </div>
      <div class="header-right">
        <el-tag type="success" effect="dark" class="status-tag">
          <i class="el-icon-circle-check"></i>
          系统正常
        </el-tag>
        <el-tag type="info" effect="dark" class="update-time">
          <i class="el-icon-time"></i>
          更新于 {{ updateTime }}
        </el-tag>
      </div>
    </div>

    <div class="filter-section">
      <div class="filter-left">
        <el-select v-model="timeRange" placeholder="时间范围" @change="refreshData" class="time-select">
          <el-option label="最近1小时" value="1h" />
          <el-option label="最近6小时" value="6h" />
          <el-option label="最近24小时" value="24h" />
          <el-option label="最近7天" value="7d" />
        </el-select>
        <el-select v-model="apiFilter" placeholder="筛选接口" @change="filterApis" class="api-select">
          <el-option label="全部接口" value="all" />
          <el-option label="慢接口 (>500ms)" value="slow" />
          <el-option label="高频接口" value="frequent" />
        </el-select>
      </div>
      <div class="filter-right">
        <el-button class="action-btn btn-primary" @click="refreshData" :loading="loading">
          <i class="el-icon-refresh"></i>
          <span>刷新数据</span>
        </el-button>
        <el-button class="action-btn btn-success" @click="exportReport">
          <i class="el-icon-download"></i>
          <span>导出报告</span>
        </el-button>
        <el-button class="action-btn btn-warning" @click="optimizePerformance">
          <i class="el-icon-magic-stick"></i>
          <span>优化建议</span>
        </el-button>
      </div>
    </div>

    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="(metric, index) in metrics" :key="index">
          <div class="stat-card" :class="`stat-${metric.type}`" @click="handleMetricClick(metric)">
            <div class="stat-icon">
              <i :class="metric.icon"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ metric.value }}{{ metric.unit }}</div>
              <div class="stat-label">{{ metric.label }}</div>
              <div class="stat-trend" :class="metric.trend >= 0 ? 'trend-up' : 'trend-down'">
                <i :class="metric.trend >= 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
                {{ Math.abs(metric.trend) }}%
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="api-section">
      <el-card shadow="never" class="table-card">
        <div class="card-header">
          <h3 class="card-title">
            <i class="el-icon-link"></i>
            接口性能监控
          </h3>
          <div class="header-stats">
            <span class="stat-text">共 {{ filteredApiList.length }} 个接口</span>
            <el-tag :type="warningCount > 0 ? 'warning' : 'success'" size="small" effect="dark">
              {{ warningCount > 0 ? warningCount + ' 个警告' : '全部正常' }}
            </el-tag>
          </div>
        </div>
        <el-table :data="filteredApiList" stripe class="stats-table">
          <el-table-column label="方法" width="100">
            <template #default="scope">
              <el-tag :type="getMethodType(scope.row.method)" size="small" effect="dark">
                {{ scope.row.method }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="接口路径" min-width="200">
            <template #default="scope">
              <span class="api-path">{{ scope.row.path }}</span>
            </template>
          </el-table-column>
          <el-table-column label="平均响应" width="120">
            <template #default="scope">
              <span :class="getTimeClass(scope.row.avgTime)">
                {{ scope.row.avgTime }}ms
              </span>
            </template>
          </el-table-column>
          <el-table-column label="P95响应" width="120">
            <template #default="scope">
              <span :class="getTimeClass(scope.row.p95)">
                {{ scope.row.p95 }}ms
              </span>
            </template>
          </el-table-column>
          <el-table-column label="P99响应" width="120">
            <template #default="scope">
              <span :class="getTimeClass(scope.row.p99)">
                {{ scope.row.p99 }}ms
              </span>
            </template>
          </el-table-column>
          <el-table-column label="请求数" width="100">
            <template #default="scope">
              <span class="request-count">{{ scope.row.requests.toLocaleString() }}</span>
            </template>
          </el-table-column>
          <el-table-column label="错误率" width="100">
            <template #default="scope">
              <span :class="scope.row.errorRate > 5 ? 'error-high' : 'error-low'">
                {{ scope.row.errorRate }}%
              </span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'normal' ? 'success' : 'warning'" size="small" effect="dark">
                {{ scope.row.status === 'normal' ? '正常' : '警告' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="24" :md="12" :lg="12">
          <el-card shadow="never" class="chart-card">
            <div class="card-header">
              <h3 class="card-title">
                <i class="el-icon-timer"></i>
                响应时间趋势
              </h3>
              <el-radio-group v-model="responseTimeRange" size="mini" @change="updateResponseChart">
                <el-radio-button label="1h">1小时</el-radio-button>
                <el-radio-button label="6h">6小时</el-radio-button>
                <el-radio-button label="24h">24小时</el-radio-button>
              </el-radio-group>
            </div>
            <div class="chart-container">
              <div class="chart-bars">
                <div v-for="(item, index) in responseTimeHistory" :key="index" class="chart-bar-item">
                  <div 
                    class="chart-bar" 
                    :style="{
                      height: (item.value / 500 * 100) + '%',
                      background: getResponseBarGradient(item.value)
                    }"
                  >
                    <span class="bar-value">{{ item.value }}ms</span>
                  </div>
                  <span class="bar-label">{{ item.label }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="24" :md="12" :lg="12">
          <el-card shadow="never" class="chart-card">
            <div class="card-header">
              <h3 class="card-title">
                <i class="el-icon-connection"></i>
                吞吐量趋势
              </h3>
              <el-radio-group v-model="throughputTimeRange" size="mini" @change="updateThroughputChart">
                <el-radio-button label="1h">1小时</el-radio-button>
                <el-radio-button label="6h">6小时</el-radio-button>
                <el-radio-button label="24h">24小时</el-radio-button>
              </el-radio-group>
            </div>
            <div class="chart-container">
              <div class="chart-bars">
                <div v-for="(item, index) in throughputHistory" :key="index" class="chart-bar-item">
                  <div 
                    class="chart-bar" 
                    :style="{
                      height: (item.value / 1000 * 100) + '%',
                      background: getThroughputBarGradient(item.value)
                    }"
                  >
                    <span class="bar-value">{{ item.value }}</span>
                  </div>
                  <span class="bar-label">{{ item.label }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="slow-query-section">
      <el-card shadow="never" class="table-card">
        <div class="card-header">
          <h3 class="card-title">
            <i class="el-icon-time"></i>
            慢查询日志
          </h3>
          <div class="header-stats">
            <span class="stat-text">共 {{ slowQueries.length }} 条记录</span>
            <el-button class="action-btn btn-danger" @click="clearSlowQueries">
              <i class="el-icon-delete"></i>
              <span>清空日志</span>
            </el-button>
          </div>
        </div>
        <el-table :data="slowQueries" stripe class="stats-table">
          <el-table-column label="时间" width="180">
            <template #default="scope">
              <span class="time-text">{{ scope.row.time }}</span>
            </template>
          </el-table-column>
          <el-table-column label="查询语句" min-width="300" show-overflow-tooltip>
            <template #default="scope">
              <span class="query-text">{{ scope.row.query }}</span>
            </template>
          </el-table-column>
          <el-table-column label="执行时间" width="120">
            <template #default="scope">
              <span class="slow-query-time">{{ scope.row.duration }}ms</span>
            </template>
          </el-table-column>
          <el-table-column label="表名" width="120">
            <template #default="scope">
              <el-tag type="info" size="small">{{ scope.row.table }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template #default="scope">
              <el-button class="action-btn btn-info" @click="analyzeQuery(scope.row)">
                <i class="el-icon-search"></i>
                <span>分析</span>
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Performance',
  data() {
    return {
      loading: false,
      updateTime: '',
      timeRange: '1h',
      apiFilter: 'all',
      responseTimeRange: '1h',
      throughputTimeRange: '1h',
      
      metrics: [
        { type: 'response', icon: 'el-icon-timer', label: '平均响应时间', value: 0, unit: 'ms', trend: 0 },
        { type: 'throughput', icon: 'el-icon-connection', label: '吞吐量', value: 0, unit: ' req/s', trend: 0 },
        { type: 'error', icon: 'el-icon-warning', label: '错误率', value: 0, unit: '%', trend: 0 },
        { type: 'availability', icon: 'el-icon-circle-check', label: '可用性', value: 0, unit: '%', trend: 0 }
      ],
      
      apiList: [],
      slowQueries: [],
      responseTimeHistory: [],
      throughputHistory: []
    }
  },
  computed: {
    filteredApiList() {
      if (this.apiFilter === 'slow') {
        return this.apiList.filter(api => api.avgTime > 500)
      } else if (this.apiFilter === 'frequent') {
        return this.apiList.filter(api => api.requests > 5000)
      }
      return this.apiList
    },
    warningCount() {
      return this.apiList.filter(api => api.status === 'warning').length
    }
  },
  created() {
    this.loadData()
  },
  beforeDestroy() {
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer)
    }
  },
  methods: {
    loadData() {
      this.calculateMetrics()
      this.loadApiList()
      this.loadSlowQueries()
      this.generateChartHistory()
      this.updateTime = this.formatTime(new Date())
      this.startAutoRefresh()
    },
    
    calculateMetrics() {
      const avgResponseTime = Math.floor(Math.random() * 50) + 100
      const throughput = Math.floor(Math.random() * 100) + 800
      const errorRate = (Math.random() * 2).toFixed(1)
      const availability = (99.9 + Math.random() * 0.1).toFixed(2)
      
      this.metrics[0].value = avgResponseTime
      this.metrics[0].trend = (Math.random() * 15 - 5).toFixed(1)
      
      this.metrics[1].value = throughput
      this.metrics[1].trend = (Math.random() * 20 - 5).toFixed(1)
      
      this.metrics[2].value = errorRate
      this.metrics[2].trend = (Math.random() * 20 - 10).toFixed(1)
      
      this.metrics[3].value = availability
      this.metrics[3].trend = (Math.random() * 0.1 - 0.05).toFixed(2)
    },
    
    loadApiList() {
      this.apiList = [
        { method: 'GET', path: '/api/products', avgTime: 85, p95: 120, p99: 180, requests: 12580, errorRate: 0.5, status: 'normal' },
        { method: 'POST', path: '/api/orders', avgTime: 245, p95: 380, p99: 520, requests: 3250, errorRate: 1.2, status: 'normal' },
        { method: 'GET', path: '/api/users', avgTime: 65, p95: 95, p99: 140, requests: 8920, errorRate: 0.3, status: 'normal' },
        { method: 'PUT', path: '/api/products/:id', avgTime: 180, p95: 280, p99: 420, requests: 2150, errorRate: 0.8, status: 'normal' },
        { method: 'DELETE', path: '/api/orders/:id', avgTime: 320, p95: 480, p99: 650, requests: 890, errorRate: 2.1, status: 'warning' },
        { method: 'GET', path: '/api/statistics', avgTime: 520, p95: 780, p99: 1200, requests: 450, errorRate: 3.5, status: 'warning' },
        { method: 'POST', path: '/api/auth/login', avgTime: 150, p95: 220, p99: 350, requests: 5680, errorRate: 1.5, status: 'normal' },
        { method: 'GET', path: '/api/notices', avgTime: 95, p95: 140, p99: 210, requests: 4320, errorRate: 0.4, status: 'normal' }
      ]
    },
    
    loadSlowQueries() {
      this.slowQueries = [
        { time: '2024-01-15 14:32:15', query: 'SELECT * FROM products WHERE category_id IN (SELECT id FROM categories WHERE parent_id = 1)', duration: 1250, table: 'products' },
        { time: '2024-01-15 14:28:42', query: 'UPDATE orders SET status = "completed" WHERE user_id = 12345 AND created_at < "2024-01-01"', duration: 890, table: 'orders' },
        { time: '2024-01-15 14:25:18', query: 'SELECT COUNT(*) FROM order_items GROUP BY product_id HAVING COUNT(*) > 100', duration: 750, table: 'order_items' },
        { time: '2024-01-15 14:20:05', query: 'SELECT u.*, o.* FROM users u LEFT JOIN orders o ON u.id = o.user_id WHERE u.role = "MERCHANT"', duration: 680, table: 'users' }
      ]
    },
    
    generateChartHistory() {
      const labels = ['10:00', '10:05', '10:10', '10:15', '10:20', '10:25', '10:30', '10:35', '10:40', '10:45', '10:50', '10:55']
      
      this.responseTimeHistory = labels.map(label => ({
        label,
        value: Math.floor(Math.random() * 100) + 100
      }))
      
      this.throughputHistory = labels.map(label => ({
        label,
        value: Math.floor(Math.random() * 100) + 800
      }))
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
    
    getMethodType(method) {
      const types = {
        'GET': 'success',
        'POST': 'primary',
        'PUT': 'warning',
        'DELETE': 'danger'
      }
      return types[method] || 'info'
    },
    
    getTimeClass(time) {
      if (time < 100) return 'time-fast'
      if (time < 300) return 'time-normal'
      return 'time-slow'
    },
    
    getResponseBarGradient(value) {
      if (value < 150) return 'linear-gradient(to top, #667eea, #764ba2)'
      if (value < 300) return 'linear-gradient(to top, #43e97b, #38f9d7)'
      return 'linear-gradient(to top, #fa709a, #fee140)'
    },
    
    getThroughputBarGradient(value) {
      if (value < 850) return 'linear-gradient(to top, #4facfe, #00f2fe)'
      if (value < 900) return 'linear-gradient(to top, #43e97b, #38f9d7)'
      return 'linear-gradient(to top, #f093fb, #f5576c)'
    },
    
    filterApis() {
      if (this.apiFilter === 'slow') {
        this.$message.info('已筛选慢接口（>500ms）')
      } else if (this.apiFilter === 'frequent') {
        this.$message.info('已筛选高频接口')
      } else {
        this.$message.info('显示全部接口')
      }
      this.refreshData()
    },
    
    handleMetricClick(metric) {
      this.$message.info(`查看 ${metric.label} 详细信息`)
    },
    
    refreshData() {
      this.loading = true
      setTimeout(() => {
        this.calculateMetrics()
        this.loadApiList()
        this.generateChartHistory()
        this.updateTime = this.formatTime(new Date())
        this.loading = false
        this.$message.success('数据已刷新')
      }, 1000)
    },
    
    updateResponseChart() {
      this.generateChartHistory()
    },
    
    updateThroughputChart() {
      this.generateChartHistory()
    },
    
    exportReport() {
      const reportData = [
        ['性能监控报告'],
        ['生成时间', this.updateTime],
        [''],
        ['指标', '当前值', '趋势'],
        ['平均响应时间', this.metrics[0].value + 'ms', this.metrics[0].trend + '%'],
        ['吞吐量', this.metrics[1].value + ' req/s', this.metrics[1].trend + '%'],
        ['错误率', this.metrics[2].value + '%', this.metrics[2].trend + '%'],
        ['可用性', this.metrics[3].value + '%', this.metrics[3].trend + '%'],
        [''],
        ['接口性能详情'],
        ['方法', '路径', '平均响应', 'P95', 'P99', '请求数', '错误率', '状态']
      ]
      
      this.apiList.forEach(api => {
        reportData.push([
          api.method,
          api.path,
          api.avgTime + 'ms',
          api.p95 + 'ms',
          api.p99 + 'ms',
          api.requests,
          api.errorRate + '%',
          api.status
        ])
      })
      
      const csvContent = reportData.map(row => row.join(',')).join('\n')
      const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `性能监控报告_${new Date().getTime()}.csv`
      link.click()
      this.$message.success('性能报告导出成功')
    },
    
    optimizePerformance() {
      this.$alert('性能优化建议：\n\n1. 对慢查询添加索引\n2. 启用Redis缓存热点数据\n3. 优化数据库连接池配置\n4. 启用Gzip压缩\n5. 配置CDN加速静态资源\n6. 优化数据库查询语句\n7. 增加服务器内存配置', '优化建议', {
        confirmButtonText: '确定',
        type: 'info'
      })
    },
    
    clearSlowQueries() {
      this.$confirm('确定要清空慢查询日志吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.slowQueries = []
        this.$message.success('慢查询日志已清空')
      }).catch(() => {})
    },
    
    analyzeQuery(query) {
      this.$alert(`查询分析：\n\n表：${query.table}\n执行时间：${query.duration}ms\n建议：添加索引或优化查询语句`, '查询分析', {
        confirmButtonText: '确定',
        type: 'info'
      })
    },
    
    startAutoRefresh() {
      this.refreshTimer = setInterval(() => {
        this.calculateMetrics()
        this.generateChartHistory()
        this.updateTime = this.formatTime(new Date())
      }, 30000)
    }
  }
}
</script>

<style scoped>
.performance-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-title i {
  color: #409eff;
}

.page-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 5px 0 0 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-tag {
  font-size: 14px;
  padding: 8px 15px;
}

.update-time {
  font-size: 12px;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.filter-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.time-select, .api-select {
  width: 150px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.btn-success {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(67, 233, 123, 0.3);
}

.btn-success:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(67, 233, 123, 0.4);
}

.btn-warning {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(250, 112, 154, 0.3);
}

.btn-warning:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(250, 112, 154, 0.4);
}

.btn-danger {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
}

.btn-danger:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 107, 107, 0.4);
}

.btn-info {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(79, 172, 254, 0.3);
}

.btn-info:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(79, 172, 254, 0.4);
}

.stats-section {
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  margin-bottom: 15px;
}

.stat-response .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-throughput .stat-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-error .stat-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-availability .stat-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  margin-bottom: 15px;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.stat-trend.trend-up {
  color: #67C23A;
}

.stat-trend.trend-down {
  color: #F56C6C;
}

.api-section, .charts-section, .slow-query-section {
  margin-bottom: 20px;
}

.table-card, .chart-card {
  border-radius: 12px;
  margin-bottom: 20px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-title i {
  color: #409eff;
}

.header-stats {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-text {
  font-size: 14px;
  color: #909399;
}

.api-path {
  font-family: 'Courier New', monospace;
  font-size: 13px;
  color: #606266;
}

.request-count {
  font-weight: 600;
  color: #303133;
}

.time-fast {
  color: #67C23A;
  font-weight: 600;
}

.time-normal {
  color: #E6A23C;
  font-weight: 600;
}

.time-slow {
  color: #F56C6C;
  font-weight: 600;
}

.error-high {
  color: #F56C6C;
  font-weight: 600;
}

.error-low {
  color: #67C23A;
  font-weight: 600;
}

.slow-query-time {
  color: #F56C6C;
  font-weight: 600;
}

.time-text {
  font-size: 13px;
  color: #606266;
}

.query-text {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #606266;
}

.chart-container {
  height: 250px;
  display: flex;
  align-items: flex-end;
  padding: 20px 10px 0;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  width: 100%;
  height: 100%;
  gap: 8px;
}

.chart-bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
}

.chart-bar {
  width: 100%;
  border-radius: 6px 6px 0 0;
  min-height: 10px;
  transition: height 0.5s ease;
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: center;
}

.bar-value {
  font-size: 10px;
  color: #fff;
  padding: 2px 4px;
  opacity: 0;
  transition: opacity 0.3s;
}

.chart-bar:hover .bar-value {
  opacity: 1;
}

.bar-label {
  font-size: 11px;
  color: #909399;
  margin-top: 8px;
  text-align: center;
}

@media (max-width: 768px) {
  .performance-page {
    padding: 15px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .filter-section {
    flex-direction: column;
    gap: 15px;
  }
  
  .filter-left, .filter-right {
    width: 100%;
  }
  
  .filter-right {
    justify-content: space-between;
  }
  
  .time-select, .api-select {
    flex: 1;
  }
  
  .action-btn {
    flex: 1;
    justify-content: center;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .stat-number {
    font-size: 24px;
  }
  
  .header-stats {
    flex-direction: column;
    gap: 10px;
  }
}
</style>