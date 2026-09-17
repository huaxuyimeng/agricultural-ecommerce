/**
 * 服务器状态页面
 * 文件路径: src/views/manager/ServerStatus.vue
 * 功能描述: 实时监控服务器运行状态和性能指标，支持主服务器/备份服务器/数据库服务器三台切换，
 *           展示四大资源指标（CPU使用率、内存使用率、磁盘使用率、网络速度）带进度条和趋势，
 *           服务器信息面板（名称、OS、IP、运行时间、CPU型号、内存/磁盘容量），
 *           服务状态列表（Nginx/MySQL/Redis/Node.js/FTP运行状态监控），磁盘使用详情（系统盘/数据盘/备份盘），
 *           网络流量统计（上行/下行流量、连接数、平均延迟），CPU和内存使用历史趋势柱状图，
 *           CSV报告导出，30秒自动刷新，时间范围筛选（1h/6h/24h/7d）
 * 关联文件:
 * - src/api/index.js: 提供服务器状态数据接口
 */
<template>
  <div class="server-status-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">
          <i class="el-icon-monitor"></i>
          服务器状态
        </h1>
        <p class="page-subtitle">实时监控服务器运行状态和性能指标</p>
      </div>
      <div class="header-right">
        <el-tag :type="serverStatus === 'running' ? 'success' : 'danger'" effect="dark" class="status-tag">
          <i :class="serverStatus === 'running' ? 'el-icon-circle-check' : 'el-icon-circle-close'"></i>
          {{ serverStatus === 'running' ? '运行正常' : '服务异常' }}
        </el-tag>
        <el-tag type="info" effect="dark" class="update-time">
          <i class="el-icon-time"></i>
          更新于 {{ updateTime }}
        </el-tag>
      </div>
    </div>

    <div class="filter-section">
      <div class="filter-left">
        <el-select v-model="selectedServer" placeholder="选择服务器" @change="handleServerChange" class="server-select">
          <el-option label="主服务器" value="main" />
          <el-option label="备份服务器" value="backup" />
          <el-option label="数据库服务器" value="database" />
        </el-select>
        <el-select v-model="timeRange" placeholder="时间范围" @change="refreshData" class="time-select">
          <el-option label="最近1小时" value="1h" />
          <el-option label="最近6小时" value="6h" />
          <el-option label="最近24小时" value="24h" />
          <el-option label="最近7天" value="7d" />
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
            <el-progress 
              :percentage="metric.progress" 
              :stroke-width="6"
              :show-text="false"
              :color="metric.progressColor"
              class="stat-progress"
            />
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="details-section">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="24" :md="12" :lg="8">
          <el-card shadow="never" class="detail-card">
            <div class="card-header">
              <h3 class="card-title">
                <i class="el-icon-s-platform"></i>
                服务器信息
              </h3>
            </div>
            <div class="info-list">
              <div v-for="(item, index) in serverInfoList" :key="index" class="info-item">
                <span class="info-label">{{ item.label }}</span>
                <span class="info-value">{{ item.value }}</span>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="24" :md="12" :lg="8">
          <el-card shadow="never" class="detail-card">
            <div class="card-header">
              <h3 class="card-title">
                <i class="el-icon-s-operation"></i>
                服务状态
              </h3>
            </div>
            <div class="service-list">
              <div v-for="(service, index) in services" :key="index" class="service-item">
                <div class="service-left">
                  <div class="service-icon" :class="`service-${service.type}`">
                    <i :class="service.icon"></i>
                  </div>
                  <div class="service-info">
                    <div class="service-name">{{ service.name }}</div>
                    <div class="service-desc">{{ service.description }}</div>
                  </div>
                </div>
                <el-tag :type="service.status === 'running' ? 'success' : service.status === 'stopped' ? 'danger' : 'warning'" size="small" effect="dark">
                  {{ service.status === 'running' ? '运行中' : service.status === 'stopped' ? '已停止' : '待启动' }}
                </el-tag>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="24" :md="12" :lg="8">
          <el-card shadow="never" class="detail-card">
            <div class="card-header">
              <h3 class="card-title">
                <i class="el-icon-files"></i>
                磁盘使用
              </h3>
            </div>
            <div class="disk-list">
              <div v-for="(disk, index) in diskUsage" :key="index" class="disk-item">
                <div class="disk-header">
                  <span class="disk-name">{{ disk.name }}</span>
                  <span class="disk-value">{{ disk.used }} / {{ disk.total }}</span>
                </div>
                <el-progress 
                  :percentage="disk.percentage" 
                  :stroke-width="10"
                  :show-text="false"
                  :color="disk.color"
                />
                <div class="disk-trend" :class="disk.percentage > 80 ? 'trend-down' : 'trend-up'">
                  <i :class="disk.percentage > 80 ? 'el-icon-warning' : 'el-icon-success'"></i>
                  {{ disk.percentage > 80 ? '空间不足' : '空间充足' }}
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="24" :md="12" :lg="12">
          <el-card shadow="never" class="chart-card">
            <div class="card-header">
              <h3 class="card-title">
                <i class="el-icon-cpu"></i>
                CPU使用率趋势
              </h3>
              <el-radio-group v-model="cpuTimeRange" size="mini" @change="updateCpuChart">
                <el-radio-button label="1h">1小时</el-radio-button>
                <el-radio-button label="6h">6小时</el-radio-button>
                <el-radio-button label="24h">24小时</el-radio-button>
              </el-radio-group>
            </div>
            <div class="chart-container">
              <div class="chart-bars">
                <div v-for="(item, index) in cpuHistory" :key="index" class="chart-bar-item">
                  <div 
                    class="chart-bar" 
                    :style="{
                      height: (item.value / 100 * 100) + '%',
                      background: getBarGradient(item.value)
                    }"
                  >
                    <span class="bar-value">{{ item.value }}%</span>
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
                <i class="el-icon-coin"></i>
                内存使用率趋势
              </h3>
              <el-radio-group v-model="memoryTimeRange" size="mini" @change="updateMemoryChart">
                <el-radio-button label="1h">1小时</el-radio-button>
                <el-radio-button label="6h">6小时</el-radio-button>
                <el-radio-button label="24h">24小时</el-radio-button>
              </el-radio-group>
            </div>
            <div class="chart-container">
              <div class="chart-bars">
                <div v-for="(item, index) in memoryHistory" :key="index" class="chart-bar-item">
                  <div 
                    class="chart-bar" 
                    :style="{
                      height: (item.value / 100 * 100) + '%',
                      background: getMemoryBarGradient(item.value)
                    }"
                  >
                    <span class="bar-value">{{ item.value }}%</span>
                  </div>
                  <span class="bar-label">{{ item.label }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="network-section">
      <el-card shadow="never" class="network-card">
        <div class="card-header">
          <h3 class="card-title">
            <i class="el-icon-connection"></i>
            网络流量监控
          </h3>
        </div>
        <div class="network-grid">
          <div v-for="(item, index) in networkStats" :key="index" class="network-item">
            <div class="network-icon" :style="{ background: item.iconColor }">
              <i :class="item.icon"></i>
            </div>
            <div class="network-info">
              <div class="network-label">{{ item.label }}</div>
              <div class="network-value">{{ item.value }}</div>
              <div class="network-trend" :class="item.trend >= 0 ? 'trend-up' : 'trend-down'">
                <i :class="item.trend >= 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
                {{ Math.abs(item.trend) }}%
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ServerStatus',
  data() {
    return {
      loading: false,
      serverStatus: 'running',
      updateTime: '',
      selectedServer: 'main',
      timeRange: '1h',
      cpuTimeRange: '1h',
      memoryTimeRange: '1h',
      
      metrics: [
        { type: 'cpu', icon: 'el-icon-cpu', label: 'CPU使用率', value: 0, unit: '%', progress: 0, trend: 0, progressColor: '' },
        { type: 'memory', icon: 'el-icon-coin', label: '内存使用率', value: 0, unit: '%', progress: 0, trend: 0, progressColor: '' },
        { type: 'disk', icon: 'el-icon-files', label: '磁盘使用率', value: 0, unit: '%', progress: 0, trend: 0, progressColor: '' },
        { type: 'network', icon: 'el-icon-connection', label: '网络速度', value: 0, unit: ' Mbps', progress: 0, trend: 0, progressColor: '' }
      ],
      
      serverInfoList: [],
      services: [],
      diskUsage: [],
      networkStats: [],
      
      cpuHistory: [],
      memoryHistory: []
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
      this.loadServerInfo()
      this.loadServices()
      this.loadDiskUsage()
      this.loadNetworkStats()
      this.generateChartHistory()
      this.updateTime = this.formatTime(new Date())
      this.startAutoRefresh()
    },
    
    calculateMetrics() {
      const cpuUsage = Math.floor(Math.random() * 30) + 30
      const memoryUsage = Math.floor(Math.random() * 20) + 50
      const diskUsage = Math.floor(Math.random() * 15) + 30
      const networkSpeed = (Math.random() * 5 + 5).toFixed(1)
      
      this.metrics[0].value = cpuUsage
      this.metrics[0].progress = cpuUsage
      this.metrics[0].trend = (Math.random() * 10 - 3).toFixed(1)
      this.metrics[0].progressColor = this.getProgressColor(cpuUsage)
      
      this.metrics[1].value = memoryUsage
      this.metrics[1].progress = memoryUsage
      this.metrics[1].trend = (Math.random() * 8 - 2).toFixed(1)
      this.metrics[1].progressColor = this.getProgressColor(memoryUsage)
      
      this.metrics[2].value = diskUsage
      this.metrics[2].progress = diskUsage
      this.metrics[2].trend = (Math.random() * 5 - 1).toFixed(1)
      this.metrics[2].progressColor = this.getProgressColor(diskUsage)
      
      this.metrics[3].value = networkSpeed
      this.metrics[3].progress = Math.min(networkSpeed / 10 * 100, 100)
      this.metrics[3].trend = (Math.random() * 12 - 4).toFixed(1)
      this.metrics[3].progressColor = this.getProgressColor(networkSpeed / 10 * 100)
    },
    
    loadServerInfo() {
      const serverConfigs = {
        main: [
          { label: '服务器名称', value: '绿源农鲜-主服务器' },
          { label: '操作系统', value: 'CentOS 7.9' },
          { label: 'IP地址', value: '192.168.194.100' },
          { label: '运行时间', value: '15天 8小时 32分钟' },
          { label: 'CPU型号', value: 'Intel Xeon E5-2680 v4' },
          { label: '内存总量', value: '32 GB' },
          { label: '磁盘总量', value: '500 GB' }
        ],
        backup: [
          { label: '服务器名称', value: '绿源农鲜-备份服务器' },
          { label: '操作系统', value: 'CentOS 7.9' },
          { label: 'IP地址', value: '192.168.194.101' },
          { label: '运行时间', value: '7天 3小时 15分钟' },
          { label: 'CPU型号', value: 'Intel Xeon E5-2660 v3' },
          { label: '内存总量', value: '16 GB' },
          { label: '磁盘总量', value: '1 TB' }
        ],
        database: [
          { label: '服务器名称', value: '绿源农鲜-数据库服务器' },
          { label: '操作系统', value: 'Ubuntu 20.04 LTS' },
          { label: 'IP地址', value: '192.168.194.102' },
          { label: '运行时间', value: '30天 12小时 45分钟' },
          { label: 'CPU型号', value: 'AMD EPYC 7643' },
          { label: '内存总量', value: '64 GB' },
          { label: '磁盘总量', value: '2 TB' }
        ]
      }
      this.serverInfoList = serverConfigs[this.selectedServer] || serverConfigs.main
    },
    
    loadServices() {
      this.services = [
        { type: 'nginx', icon: 'el-icon-monitor', name: 'Nginx', description: 'Web服务器', status: 'running' },
        { type: 'mysql', icon: 'el-icon-s-order', name: 'MySQL', description: '数据库服务', status: 'running' },
        { type: 'redis', icon: 'el-icon-cpu', name: 'Redis', description: '缓存服务', status: 'running' },
        { type: 'node', icon: 'el-icon-connection', name: 'Node.js', description: '应用服务', status: 'running' },
        { type: 'ftp', icon: 'el-icon-files', name: 'FTP', description: '文件传输服务', status: 'stopped' }
      ]
    },
    
    loadDiskUsage() {
      this.diskUsage = [
        { name: '系统盘 (C:)', used: '120 GB', total: '200 GB', percentage: 60, color: '#67C23A' },
        { name: '数据盘 (D:)', used: '280 GB', total: '500 GB', percentage: 56, color: '#409EFF' },
        { name: '备份盘 (E:)', used: '180 GB', total: '300 GB', percentage: 60, color: '#E6A23C' }
      ]
    },
    
    loadNetworkStats() {
      this.networkStats = [
        { icon: 'el-icon-top', label: '上行流量', value: '2.5 GB', trend: 12.5, iconColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
        { icon: 'el-icon-bottom', label: '下行流量', value: '8.3 GB', trend: -5.2, iconColor: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
        { icon: 'el-icon-connection', label: '连接数', value: '1,256', trend: 8.7, iconColor: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
        { icon: 'el-icon-time', label: '平均延迟', value: '25 ms', trend: -15.3, iconColor: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' }
      ]
    },
    
    generateChartHistory() {
      const labels = ['10:00', '10:05', '10:10', '10:15', '10:20', '10:25', '10:30', '10:35', '10:40', '10:45', '10:50', '10:55']
      
      this.cpuHistory = labels.map(label => ({
        label,
        value: Math.floor(Math.random() * 30) + 30
      }))
      
      this.memoryHistory = labels.map(label => ({
        label,
        value: Math.floor(Math.random() * 20) + 50
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
    
    getProgressColor(percentage) {
      if (percentage < 60) return '#67C23A'
      if (percentage < 80) return '#E6A23C'
      return '#F56C6C'
    },
    
    getBarGradient(value) {
      if (value < 50) return 'linear-gradient(to top, #667eea, #764ba2)'
      if (value < 70) return 'linear-gradient(to top, #f093fb, #f5576c)'
      return 'linear-gradient(to top, #fa709a, #fee140)'
    },
    
    getMemoryBarGradient(value) {
      if (value < 50) return 'linear-gradient(to top, #4facfe, #00f2fe)'
      if (value < 70) return 'linear-gradient(to top, #43e97b, #38f9d7)'
      return 'linear-gradient(to top, #fa709a, #fee140)'
    },
    
    handleServerChange() {
      this.loadServerInfo()
      this.calculateMetrics()
      this.generateChartHistory()
      this.updateTime = this.formatTime(new Date())
      this.$message.success('已切换到' + this.getServerLabel())
    },

    getServerLabel() {
      const labels = { 'main': '主服务器', 'backup': '备份服务器', 'database': '数据库服务器' }
      return labels[this.selectedServer] || '未知服务器'
    },
    
    handleMetricClick(metric) {
      this.$message.info(`查看 ${metric.label} 详细信息`)
    },
    
    refreshData() {
      this.loading = true
      setTimeout(() => {
        this.calculateMetrics()
        this.generateChartHistory()
        this.updateTime = this.formatTime(new Date())
        this.loading = false
        this.$message.success('数据已刷新')
      }, 1000)
    },
    
    updateCpuChart() {
      this.generateChartHistory()
    },
    
    updateMemoryChart() {
      this.generateChartHistory()
    },
    
    exportReport() {
      const reportData = [
        ['服务器状态报告'],
        ['生成时间', this.updateTime],
        [''],
        ['指标', '当前值', '趋势'],
        ['CPU使用率', this.metrics[0].value + '%', this.metrics[0].trend + '%'],
        ['内存使用率', this.metrics[1].value + '%', this.metrics[1].trend + '%'],
        ['磁盘使用率', this.metrics[2].value + '%', this.metrics[2].trend + '%'],
        ['网络速度', this.metrics[3].value + ' Mbps', this.metrics[3].trend + '%']
      ]
      
      const csvContent = reportData.map(row => row.join(',')).join('\n')
      const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `服务器状态报告_${new Date().getTime()}.csv`
      link.click()
      this.$message.success('报告导出成功')
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
.server-status-page {
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

.server-select, .time-select {
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

.stat-cpu .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-memory .stat-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-disk .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-network .stat-icon {
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

.stat-progress {
  margin-top: 10px;
}

.details-section {
  margin-bottom: 20px;
}

.detail-card {
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

.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.info-item:hover {
  background-color: #ecf5ff;
}

.info-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #303133;
  font-weight: 600;
}

.service-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.service-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.service-item:hover {
  background-color: #ecf5ff;
}

.service-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.service-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: white;
}

.service-nginx {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.service-mysql {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.service-redis {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.service-node {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.service-ftp {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.service-info {
  flex: 1;
}

.service-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.service-desc {
  font-size: 12px;
  color: #909399;
}

.disk-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.disk-item {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.disk-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.disk-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.disk-value {
  font-size: 12px;
  color: #909399;
}

.disk-trend {
  font-size: 12px;
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.disk-trend.trend-up {
  color: #67C23A;
}

.disk-trend.trend-down {
  color: #F56C6C;
}

.charts-section {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 12px;
  margin-bottom: 20px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
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

.network-section {
  margin-bottom: 20px;
}

.network-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.network-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.network-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 12px;
  transition: all 0.3s;
}

.network-item:hover {
  background-color: #ecf5ff;
  transform: translateY(-2px);
}

.network-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.network-info {
  flex: 1;
}

.network-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.network-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 5px;
}

.network-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.network-trend.trend-up {
  color: #67C23A;
}

.network-trend.trend-down {
  color: #F56C6C;
}

@media (max-width: 768px) {
  .server-status-page {
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
  
  .server-select, .time-select {
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
  
  .network-grid {
    grid-template-columns: 1fr;
  }
}
</style>