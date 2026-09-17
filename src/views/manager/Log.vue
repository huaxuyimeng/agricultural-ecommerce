/**
 * 系统日志管理页面
 * 文件路径: src/views/manager/Log.vue
 * 功能描述: 查看和管理系统操作日志，追踪系统运行状态，展示总日志数/错误日志/今日日志三统计指标，
 *           支持关键字搜索、日志级别筛选（信息/警告/错误/系统）、操作类型筛选（登录退出/用户/商品/订单/系统），
 *           高级筛选（时间范围、操作用户、排序方式），日志列表分页展示，
 *           支持日志详情查看弹窗、CSV日志导出、日志清理，表格内嵌操作按钮（查看详情/标记/删除）
 * 关联文件:
 * - src/utils/log.js: 提供日志管理功能
 * - src/api/index.js: 提供日志数据查询接口
 */
<template>
  <div class="log-manage-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-main">
        <h1 class="page-title">系统日志</h1>
        <p class="page-subtitle">查看和管理系统操作日志，追踪系统运行状态</p>
      </div>
      <div class="header-stats">
        <div class="stat-item">
          <i class="el-icon-message stat-icon total"></i>
          <div class="stat-info">
            <div class="stat-number">{{ stats.totalLogs }}</div>
            <div class="stat-label">总日志数</div>
          </div>
        </div>
        <div class="stat-item">
          <i class="el-icon-warning stat-icon error"></i>
          <div class="stat-info">
            <div class="stat-number">{{ stats.errorLogs }}</div>
            <div class="stat-label">错误日志</div>
          </div>
        </div>
        <div class="stat-item">
          <i class="el-icon-time stat-icon today"></i>
          <div class="stat-info">
            <div class="stat-number">{{ stats.todayLogs }}</div>
            <div class="stat-label">今日日志</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选工具栏 -->
    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar-content">
        <!-- 搜索框 -->
        <div class="search-section">
          <div class="search-group">
            <el-input
              placeholder="请输入关键词查询日志"
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
              <template #append>
                <el-button 
                  type="primary" 
                  @click="handleSearch"
                  class="search-btn"
                >
                  搜索
                </el-button>
              </template>
            </el-input>
          </div>
          
          <div class="filter-group">
            <el-select
              v-model="filterLevel"
              placeholder="日志级别"
              clearable
              @change="handleSearch"
              class="filter-select"
              size="medium"
            >
              <el-option label="全部" value=""></el-option>
              <el-option label="信息" value="info"></el-option>
              <el-option label="警告" value="warning"></el-option>
              <el-option label="错误" value="error"></el-option>
              <el-option label="系统" value="system"></el-option>
            </el-select>
            
            <el-select
              v-model="filterType"
              placeholder="操作类型"
              clearable
              @change="handleSearch"
              class="filter-select"
              size="medium"
            >
              <el-option label="全部" value=""></el-option>
              <el-option label="登录/退出" value="auth"></el-option>
              <el-option label="用户管理" value="user"></el-option>
              <el-option label="商品管理" value="product"></el-option>
              <el-option label="订单管理" value="order"></el-option>
              <el-option label="系统操作" value="system"></el-option>
            </el-select>
            
            <el-button 
              type="text" 
              @click="toggleAdvancedFilter"
              class="filter-toggle"
            >
              <i class="el-icon-filter"></i>
              {{ showAdvancedFilter ? '收起筛选' : '高级筛选' }}
            </el-button>
          </div>
        </div>
        
        <!-- 高级筛选 -->
        <div v-if="showAdvancedFilter" class="advanced-filter">
          <el-form :model="advancedFilter" label-width="100px" class="advanced-form">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="时间范围">
                  <el-date-picker
                    v-model="advancedFilter.timeRange"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    value-format="yyyy-MM-dd"
                    @change="handleSearch"
                    class="date-picker"
                    size="medium"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="操作用户">
                  <el-input
                    v-model="advancedFilter.username"
                    placeholder="请输入操作用户"
                    clearable
                    @change="handleSearch"
                    class="user-input"
                    size="medium"
                  >
                    <template #prefix>
                      <i class="el-icon-user"></i>
                    </template>
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="排序方式">
                  <el-select
                    v-model="advancedFilter.sortBy"
                    placeholder="请选择排序字段"
                    @change="handleSearch"
                    class="sort-select"
                    size="medium"
                  >
                    <el-option label="时间（最新）" value="time:desc"></el-option>
                    <el-option label="时间（最早）" value="time:asc"></el-option>
                    <el-option label="级别" value="level:desc"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
        
        <!-- 操作按钮 -->
        <div class="operation-section">
          <div class="operation-left">
            <el-button 
              type="primary" 
              @click="handleExport"
              class="export-btn"
              size="medium"
            >
              <i class="el-icon-download"></i>
              导出日志
            </el-button>
            
            <el-button 
              type="warning" 
              @click="handleClear"
              class="clear-btn"
              size="medium"
            >
              <i class="el-icon-delete"></i>
              清理日志
            </el-button>
          </div>
          
          <div class="operation-right">
            <el-button 
              type="info" 
              @click="loadData"
              class="refresh-btn"
              size="medium"
            >
              <i class="el-icon-refresh"></i>
              刷新
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 日志表格 -->
    <el-card shadow="never" class="table-card">
      <!-- 表格头部 -->
      <div class="table-header">
        <div class="table-title">
          <i class="el-icon-notebook-2"></i>
          日志列表
        </div>
      </div>
      
      <!-- 数据表格 -->
      <el-table 
        :data="tableData" 
        v-loading="loading"
        stripe
        border
        class="log-table"
        :row-class-name="tableRowClassName"
      >
        <el-table-column 
          prop="id" 
          label="ID" 
          width="80" 
          align="center"
        />
        
        <el-table-column 
          label="日志信息" 
          min-width="300"
        >
          <template #default="scope">
            <div class="log-info">
              <div class="log-header">
                <el-tag 
                  :type="getLevelTagType(scope.row.level)"
                  class="level-tag"
                >
                  {{ getLevelText(scope.row.level) }}
                </el-tag>
                <el-tag 
                  :type="getTypeTagType(scope.row.type)"
                  class="type-tag"
                >
                  {{ getTypeText(scope.row.type) }}
                </el-tag>
                <span class="log-user">{{ scope.row.username || '系统' }}</span>
              </div>
              <div class="log-content">{{ scope.row.content }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column 
          prop="ip" 
          label="IP地址" 
          width="150"
        />
        
        <el-table-column 
          prop="time" 
          label="操作时间" 
          width="180"
          sortable
        >
          <template #default="scope">
            <div class="log-time">
              {{ formatTime(scope.row.time) }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column 
          label="操作" 
          width="120" 
          align="center"
        >
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="viewDetail(scope.row)"
              class="detail-btn"
            >
              <i class="el-icon-view"></i>
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-box">
        <el-pagination
          background
          :current-page="pagination.pageNum"
          :page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog
      title="日志详情"
      :visible.sync="detailDialog.visible"
      width="600px"
      class="detail-dialog"
    >
      <div class="detail-content" v-if="detailDialog.log">
        <div class="detail-item">
          <span class="detail-label">日志ID：</span>
          <span class="detail-value">{{ detailDialog.log.id }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">日志级别：</span>
          <el-tag :type="getLevelTagType(detailDialog.log.level)">{{ getLevelText(detailDialog.log.level) }}</el-tag>
        </div>
        <div class="detail-item">
          <span class="detail-label">操作类型：</span>
          <el-tag :type="getTypeTagType(detailDialog.log.type)">{{ getTypeText(detailDialog.log.type) }}</el-tag>
        </div>
        <div class="detail-item">
          <span class="detail-label">操作用户：</span>
          <span class="detail-value">{{ detailDialog.log.username || '系统' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">IP地址：</span>
          <span class="detail-value">{{ detailDialog.log.ip || '未知' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">操作时间：</span>
          <span class="detail-value">{{ formatFullTime(detailDialog.log.time) }}</span>
        </div>
        <div class="detail-item detail-content-item">
          <span class="detail-label">日志内容：</span>
          <div class="detail-content-text">{{ detailDialog.log.content }}</div>
        </div>
        <div class="detail-item" v-if="detailDialog.log.extra">
          <span class="detail-label">附加信息：</span>
          <div class="detail-extra">
            <pre>{{ JSON.stringify(detailDialog.log.extra, null, 2) }}</pre>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialog.visible = false" size="medium">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 清理日志对话框 -->
    <el-dialog
      title="清理日志"
      :visible.sync="clearDialog.visible"
      width="400px"
      center
      class="clear-dialog"
    >
      <div class="clear-content">
        <div class="clear-warning">
          <i class="el-icon-warning-outline"></i>
          <p>确定要清理系统日志吗？此操作不可恢复。</p>
        </div>
        
        <el-form :model="clearForm" class="clear-form">
          <el-form-item label="清理范围">
            <el-radio-group v-model="clearForm.range">
              <el-radio label="all">全部日志</el-radio>
              <el-radio label="error">仅错误日志</el-radio>
              <el-radio label="old">30天前的日志</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="clearDialog.visible = false">取消</el-button>
          <el-button 
            type="danger" 
            @click="confirmClear"
            :loading="clearing"
          >
            确认清理
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import * as XLSX from 'xlsx'

export default {
  name: "Log",
  data() {
    return {
      // 统计数据
      stats: {
        totalLogs: 0,
        errorLogs: 0,
        todayLogs: 0
      },
      
      // 表格数据
      tableData: [],
      loading: false,
      
      // 分页
      pagination: {
        pageNum: 1,
        pageSize: 10,
        total: 0
      },
      
      // 搜索和筛选
      searchKeyword: '',
      filterLevel: '',
      filterType: '',
      showAdvancedFilter: false,
      advancedFilter: {
        timeRange: [],
        username: '',
        sortBy: 'time:desc'
      },
      
      // 详情对话框
      detailDialog: {
        visible: false,
        log: null
      },
      
      // 清理对话框
      clearDialog: {
        visible: false
      },
      clearForm: {
        range: 'all'
      },
      clearing: false
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    // 加载数据
    async loadData() {
      this.loading = true
      try {
        // 获取所有日志
        const allLogs = this.getAllLogs()
        
        // 计算统计
        this.calculateStats(allLogs)
        
        // 筛选数据
        const filteredData = this.filterData(allLogs)
        
        // 排序数据
        const sortedData = this.sortData(filteredData)
        
        // 分页
        this.pagination.total = sortedData.length
        const start = (this.pagination.pageNum - 1) * this.pagination.pageSize
        const end = start + this.pagination.pageSize
        this.tableData = sortedData.slice(start, end)
        
      } catch (error) {
        console.error('加载日志数据失败:', error)
        this.$message.error('加载日志数据失败')
      } finally {
        this.loading = false
      }
    },
    
    // 获取所有日志
    getAllLogs() {
      try {
        const logs = JSON.parse(localStorage.getItem('xm-system-logs') || '[]')
        return logs
      } catch (error) {
        console.error('获取日志失败:', error)
        return []
      }
    },
    
    // 计算统计
    calculateStats(logs) {
      const today = new Date().toLocaleDateString()
      
      this.stats.totalLogs = logs.length
      this.stats.errorLogs = logs.filter(log => log.level === 'error').length
      this.stats.todayLogs = logs.filter(log => 
        log.time && 
        new Date(log.time).toLocaleDateString() === today
      ).length
    },
    
    // 筛选数据
    filterData(logs) {
      let filtered = logs.slice()
      
      // 关键词搜索
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        filtered = filtered.filter(log => 
          (log.content && log.content.toLowerCase().includes(keyword)) ||
          (log.username && log.username.toLowerCase().includes(keyword)) ||
          (log.ip && log.ip.includes(keyword))
        )
      }
      
      // 级别筛选
      if (this.filterLevel) {
        filtered = filtered.filter(log => log.level === this.filterLevel)
      }
      
      // 类型筛选
      if (this.filterType) {
        filtered = filtered.filter(log => log.type === this.filterType)
      }
      
      // 时间范围筛选
      if (this.advancedFilter.timeRange && this.advancedFilter.timeRange.length === 2) {
        const [start, end] = this.advancedFilter.timeRange
        filtered = filtered.filter(log => {
          if (!log.time) return false
          const logDate = new Date(log.time).toLocaleDateString()
          return logDate >= start && logDate <= end
        })
      }
      
      // 用户名筛选
      if (this.advancedFilter.username) {
        const username = this.advancedFilter.username.toLowerCase()
        filtered = filtered.filter(log => 
          log.username && log.username.toLowerCase().includes(username)
        )
      }
      
      return filtered
    },
    
    // 排序数据
    sortData(logs) {
      if (!this.advancedFilter.sortBy) return logs
      
      const [field, order] = this.advancedFilter.sortBy.split(':')
      
      return logs.sort((a, b) => {
        let valueA = a[field]
        let valueB = b[field]
        
        // 处理时间字段
        if (field === 'time') {
          valueA = valueA ? new Date(valueA).getTime() : 0
          valueB = valueB ? new Date(valueB).getTime() : 0
        }
        
        // 处理级别字段
        if (field === 'level') {
          const levelOrder = { 'error': 3, 'warning': 2, 'info': 1, 'system': 0 }
          valueA = levelOrder[valueA] || 0
          valueB = levelOrder[valueB] || 0
        }
        
        if (order === 'desc') {
          return valueB - valueA
        } else {
          return valueA - valueB
        }
      })
    },
    
    // 获取级别文本
    getLevelText(level) {
      const levelMap = {
        'info': '信息',
        'warning': '警告',
        'error': '错误',
        'system': '系统'
      }
      return levelMap[level] || level
    },
    
    // 获取级别标签类型
    getLevelTagType(level) {
      const types = {
        'info': 'info',
        'warning': 'warning',
        'error': 'danger',
        'system': 'primary'
      }
      return types[level] || 'info'
    },
    
    // 获取类型文本
    getTypeText(type) {
      const typeMap = {
        'auth': '登录/退出',
        'user': '用户管理',
        'product': '商品管理',
        'order': '订单管理',
        'system': '系统操作'
      }
      return typeMap[type] || type
    },
    
    // 获取类型标签类型
    getTypeTagType(type) {
      const types = {
        'auth': 'primary',
        'user': 'success',
        'product': 'warning',
        'order': 'info',
        'system': 'danger'
      }
      return types[type] || 'info'
    },
    
    // 格式化时间
    formatTime(time) {
      if (!time) return '未知'
      
      const date = new Date(time)
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
    
    // 格式化完整时间
    formatFullTime(time) {
      if (!time) return '未知'
      return new Date(time).toLocaleString()
    },
    
    // 表格行类名
    tableRowClassName({ rowIndex }) {
      if (rowIndex % 2 === 0) {
        return 'even-row'
      } else {
        return 'odd-row'
      }
    },
    
    // 搜索处理
    handleSearch() {
      this.pagination.pageNum = 1
      this.loadData()
    },
    
    // 切换高级筛选
    toggleAdvancedFilter() {
      this.showAdvancedFilter = !this.showAdvancedFilter
    },
    
    // 查看详情
    viewDetail(log) {
      this.detailDialog.log = log
      this.detailDialog.visible = true
    },
    
    // 导出日志
    handleExport() {
      this.$confirm('确定要导出日志数据吗？', '确认导出', {
        type: 'info',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(() => {
        try {
          // 获取所有日志
          const allLogs = this.getAllLogs()
          
          // 转换数据格式
          const exportData = allLogs.map(log => {
            return {
              'ID': log.id,
              '级别': this.getLevelText(log.level),
              '类型': this.getTypeText(log.type),
              '用户': log.username || '系统',
              'IP地址': log.ip || '未知',
              '内容': log.content,
              '时间': log.time ? new Date(log.time).toLocaleString() : ''
            }
          })
          
          // 创建工作表
          const ws = XLSX.utils.json_to_sheet(exportData)
          
          // 创建工作簿
          const wb = XLSX.utils.book_new()
          XLSX.utils.book_append_sheet(wb, ws, '系统日志')
          
          // 导出文件
          XLSX.writeFile(wb, `系统日志_${new Date().toISOString().slice(0, 10)}.xlsx`)
          
          this.$message.success('导出成功')
        } catch (error) {
          console.error('导出失败:', error)
          this.$message.error('导出失败')
        }
      }).catch(() => {})
    },
    
    // 清理日志
    handleClear() {
      this.clearDialog.visible = true
    },
    
    // 确认清理
    confirmClear() {
      this.$confirm('确定要清理日志吗？此操作不可恢复。', '确认清理', {
        type: 'warning',
        confirmButtonText: '确定清理',
        cancelButtonText: '取消',
        confirmButtonClass: 'delete-confirm-btn'
      }).then(async () => {
        this.clearing = true
        try {
          const allLogs = this.getAllLogs()
          let filteredLogs = []
          
          if (this.clearForm.range === 'error') {
            // 保留非错误日志
            filteredLogs = allLogs.filter(log => log.level !== 'error')
          } else if (this.clearForm.range === 'old') {
            // 保留30天内的日志
            const thirtyDaysAgo = new Date()
            thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30)
            filteredLogs = allLogs.filter(log => {
              if (!log.time) return false
              return new Date(log.time) >= thirtyDaysAgo
            })
          }
          
          // 保存过滤后的日志
          localStorage.setItem('xm-system-logs', JSON.stringify(filteredLogs))
          
          this.$message.success('日志清理成功')
          this.clearDialog.visible = false
          this.loadData()
        } catch (error) {
          console.error('清理日志失败:', error)
          this.$message.error('清理日志失败')
        } finally {
          this.clearing = false
        }
      }).catch(() => {})
    },
    
    // 分页大小变化
    handleSizeChange(size) {
      this.pagination.pageSize = size
      this.pagination.pageNum = 1
      this.loadData()
    },
    
    // 页码变化
    handleCurrentChange(pageNum) {
      this.pagination.pageNum = pageNum
      this.loadData()
    }
  }
}
</script>

<style scoped>
/* 页面整体布局 */
.log-manage-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
  box-sizing: border-box;
}

/* 页面头部样式 */
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 20px;
  color: white;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
  position: relative;
  overflow: hidden;
}

.page-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 320"><path fill="rgba(255,255,255,0.1)" fill-opacity="1" d="M0,224L48,213.3C96,203,192,181,288,181.3C384,181,480,203,576,202.7C672,203,768,181,864,176C960,171,1056,181,1152,176C1248,171,1344,149,1392,138.7L1440,128L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path></svg>');
  background-size: cover;
  background-position: center;
  opacity: 0.1;
}

.header-main {
  margin-bottom: 20px;
  position: relative;
  z-index: 1;
}

.page-title {
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 8px 0;
  line-height: 1.4;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.page-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  font-weight: 400;
}

/* 统计卡片样式 */
.header-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  position: relative;
  z-index: 1;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  cursor: pointer;
}

.stat-item:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
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
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.stat-icon.total {
  background: linear-gradient(135deg, #409EFF, #66b1ff);
}

.stat-icon.error {
  background: linear-gradient(135deg, #F56C6C, #f78989);
}

.stat-icon.today {
  background: linear-gradient(135deg, #E6A23C, #ebb563);
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-number {
  font-size: 24px;
  font-weight: 800;
  color: white;
  line-height: 1.2;
  margin-bottom: 4px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* 工具栏卡片样式 */
.toolbar-card {
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e4e7ed;
  background: white;
  transition: all 0.3s ease;
}

.toolbar-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
}

:deep(.toolbar-card .el-card__body) {
  padding: 20px;
}

/* 搜索区域样式 */
.search-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 20px;
}

.search-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-input {
  flex: 1;
}

.search-input :deep(.el-input-group__append) {
  background: linear-gradient(135deg, #409EFF, #66b1ff);
  border-color: #409EFF;
  color: white;
  font-weight: 600;
  transition: all 0.3s ease;
}

.search-input :deep(.el-input-group__append .el-button) {
  color: white;
  border: none;
  background: transparent;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.filter-select {
  width: 150px;
}

.filter-toggle {
  color: #409EFF;
  font-weight: 500;
}

/* 高级筛选样式 */
.advanced-filter {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.advanced-form {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.date-picker,
.user-input,
.sort-select {
  width: 100%;
}

/* 操作按钮区域 */
.operation-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.operation-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.operation-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 表格卡片样式 */
.table-card {
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e4e7ed;
  background: white;
  transition: all 0.3s ease;
}

.table-card:hover {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
}

:deep(.table-card .el-card__body) {
  padding: 0;
}

/* 表格头部 */
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.table-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* 日志表格样式 */
.log-table {
  width: 100%;
}

:deep(.log-table .el-table__row) {
  cursor: pointer;
  transition: all 0.2s ease;
}

:deep(.log-table .el-table__row:hover) {
  background: #f0f9ff !important;
}

:deep(.even-row) {
  background: #fafafa;
}

:deep(.odd-row) {
  background: white;
}

.log-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.log-header {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.level-tag,
.type-tag {
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 10px;
}

.log-user {
  font-size: 12px;
  color: #606266;
  background: #f5f7fa;
  padding: 2px 8px;
  border-radius: 10px;
}

.log-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.4;
  word-break: break-word;
}

.log-time {
  font-size: 12px;
  color: #909399;
}

/* 分页样式 */
.pagination-box {
  padding: 20px;
  border-top: 1px solid #e4e7ed;
  display: flex;
  justify-content: flex-end;
}

/* 详情对话框样式 */
.detail-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.detail-label {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  width: 80px;
  flex-shrink: 0;
}

.detail-value {
  font-size: 14px;
  color: #606266;
  flex: 1;
}

.detail-content-item {
  align-items: flex-start;
}

.detail-content-text {
  font-size: 14px;
  color: #303133;
  line-height: 1.4;
  word-break: break-word;
  flex: 1;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
}

.detail-extra {
  flex: 1;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  overflow: auto;
  max-height: 200px;
}

.detail-extra pre {
  margin: 0;
  font-size: 12px;
  color: #606266;
  white-space: pre-wrap;
}

/* 清理对话框样式 */
.clear-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.clear-warning {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px;
  background: #fdf6ec;
  border: 1px solid #fde2cc;
  border-radius: 4px;
  color: #e6a23c;
}

.clear-warning i {
  font-size: 24px;
}

.clear-form {
  margin-top: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .log-manage-page {
    padding: 10px;
  }
  
  .page-header {
    padding: 20px;
  }
  
  .header-stats {
    grid-template-columns: 1fr;
  }
  
  .search-group {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-group {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-select {
    width: 100%;
  }
  
  .operation-section {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  
  .operation-left,
  .operation-right {
    justify-content: center;
  }
  
  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .pagination-box {
    justify-content: center;
  }
}
</style>