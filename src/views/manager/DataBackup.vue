/**
 * 数据备份页面
 * 文件路径: src/views/manager/DataBackup.vue
 * 功能描述: 管理系统数据备份与恢复，展示四维统计（备份总数、备份总大小、自动备份数、成功率），
 *           支持立即备份、自动备份设置（频率/时间/保留数量）、数据恢复（选择备份文件并确认覆盖），
 *           备份记录列表展示（全量/自动/手动/日志类型筛选）、分页查看，清理旧备份功能，
 *           支持通过路由参数 type=log 触发日志数据备份
 * 关联文件:
 * - src/api/index.js: 提供备份数据增删查接口
 */
<template>
  <div class="data-backup-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">
          <i class="el-icon-document-copy"></i>
          数据备份
        </h1>
        <p class="page-subtitle">系统数据管理与备份恢复</p>
      </div>
      <div class="header-right">
        <el-tag type="info" effect="dark" class="update-time">
          <i class="el-icon-time"></i>
          最后备份: {{ lastBackupTime }}
        </el-tag>
      </div>
    </div>

    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6" :lg="6">
          <div class="stat-card stat-total">
            <div class="stat-icon">
              <i class="el-icon-folder-opened"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalBackups }}</div>
              <div class="stat-label">备份总数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6" :lg="6">
          <div class="stat-card stat-size">
            <div class="stat-icon">
              <i class="el-icon-document"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalSize }}</div>
              <div class="stat-label">备份总大小</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6" :lg="6">
          <div class="stat-card stat-auto">
            <div class="stat-icon">
              <i class="el-icon-files"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.autoBackupCount }}</div>
              <div class="stat-label">自动备份</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6" :lg="6">
          <div class="stat-card stat-success">
            <div class="stat-icon">
              <i class="el-icon-check"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.successRate }}%</div>
              <div class="stat-label">成功率</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="action-section">
      <div class="action-buttons">
        <el-button class="action-btn btn-primary" @click="createBackup" :loading="backupLoading">
          <i class="el-icon-download"></i>
          <span>立即备份</span>
        </el-button>
        <el-button class="action-btn btn-success" @click="openAutoBackupDialog">
          <i class="el-icon-s-tools"></i>
          <span>自动备份设置</span>
        </el-button>
        <el-button class="action-btn btn-warning" @click="openRestoreDialog">
          <i class="el-icon-upload2"></i>
          <span>数据恢复</span>
        </el-button>
        <el-button class="action-btn btn-danger" @click="cleanOldBackups">
          <i class="el-icon-delete"></i>
          <span>清理旧备份</span>
        </el-button>
      </div>
      <el-button class="refresh-btn" @click="refreshData">
        <i class="el-icon-refresh"></i>
        <span>刷新</span>
      </el-button>
    </div>

    <el-card shadow="never" class="backup-list-card">
      <div class="list-header">
        <h3 class="list-title">
          <i class="el-icon-notebook-2"></i>
          备份记录
        </h3>
        <div class="list-actions">
          <el-select
            v-model="filterType"
            placeholder="备份类型"
            class="filter-select"
            @change="handleFilterChange"
          >
            <el-option label="全部" value="all" />
            <el-option label="手动备份" value="manual" />
            <el-option label="自动备份" value="auto" />
            <el-option label="日志备份" value="log" />
          </el-select>
        </div>
      </div>
      
      <el-table 
        :data="paginatedBackupList" 
        stripe 
        style="width: 100%"
        v-loading="loading"
        class="backup-table"
      >
        <el-table-column prop="id" label="编号" width="80" align="center" />
        <el-table-column prop="name" label="备份名称" min-width="220" />
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getBackupTypeTag(scope.row.type)" size="small">
              {{ getBackupTypeText(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="size" label="大小" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'COMPLETED' ? 'success' : 'warning'" size="small">
              {{ scope.row.status === 'COMPLETED' ? '完成' : '进行中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="备份时间" width="170" align="center" />
        <el-table-column prop="duration" label="耗时" width="100" align="center" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <div class="action-btns">
              <el-button class="table-btn btn-download" size="mini" @click="downloadBackup(scope.row)">
                <i class="el-icon-download"></i>
                下载
              </el-button>
              <el-button class="table-btn btn-restore" size="mini" @click="restoreBackup(scope.row)">
                <i class="el-icon-refresh-left"></i>
                恢复
              </el-button>
              <el-button class="table-btn btn-delete" size="mini" @click="deleteBackup(scope.row)">
                <i class="el-icon-delete"></i>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-section">
        <el-pagination
          v-model="currentPage"
          :page-size="pageSize"
          :total="filteredBackupList.length"
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      title="自动备份设置"
      :visible.sync="autoBackupDialog.visible"
      width="550px"
      :close-on-click-modal="false"
    >
      <div class="dialog-content">
        <el-alert
          title="提示"
          type="info"
          :closable="false"
          description="设置自动备份后，系统将按照您指定的周期自动备份数据。"
          show-icon
          style="margin-bottom: 20px"
        />
        <el-form :model="autoBackupForm" label-width="100px" class="backup-form">
          <el-form-item label="自动备份">
            <el-switch v-model="autoBackupForm.enabled" />
          </el-form-item>
          <el-form-item label="备份周期">
            <el-select v-model="autoBackupForm.frequency" style="width: 100%">
              <el-option label="每天" value="daily" />
              <el-option label="每周" value="weekly" />
              <el-option label="每月" value="monthly" />
            </el-select>
          </el-form-item>
          <el-form-item label="备份时间">
            <el-time-picker
              v-model="autoBackupForm.time"
              format="HH:mm"
              value-format="HH:mm"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="保留份数">
            <el-input-number
              v-model="autoBackupForm.keepCount"
              :min="1"
              :max="30"
              style="width: 100%"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="autoBackupDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="saveAutoBackupSettings">保存设置</el-button>
      </template>
    </el-dialog>

    <el-dialog
      title="数据恢复"
      :visible.sync="restoreDialog.visible"
      width="650px"
      :close-on-click-modal="false"
    >
      <div class="dialog-content">
        <el-alert
          title="警告"
          type="warning"
          :closable="false"
          description="恢复操作会覆盖当前数据，请确保已备份当前数据！"
          show-icon
          style="margin-bottom: 20px"
        />
        <el-form :model="restoreForm" label-width="120px" class="restore-form">
          <el-form-item label="选择备份文件">
            <el-select v-model="restoreForm.selectedBackup" placeholder="请选择备份文件" style="width: 100%">
              <el-option
                v-for="backup in backupList"
                :key="backup.id"
                :label="`${backup.name} (${backup.createdAt})`"
                :value="backup.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="恢复确认">
            <el-checkbox v-model="restoreForm.confirmed">
              我已确认备份文件，恢复后当前数据将被覆盖
            </el-checkbox>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="restoreDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="confirmRestore" :disabled="!restoreForm.confirmed || !restoreForm.selectedBackup">
          确认恢复
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'DataBackup',
  data() {
    return {
      lastBackupTime: '',
      backupLoading: false,
      loading: false,
      stats: {
        totalBackups: 0,
        totalSize: '0MB',
        autoBackupCount: 0,
        successRate: 0
      },
      filterType: 'all',
      backupList: [],
      currentPage: 1,
      pageSize: 10,
      autoBackupDialog: {
        visible: false
      },
      autoBackupForm: {
        enabled: true,
        frequency: 'daily',
        time: '02:00',
        keepCount: 7
      },
      restoreDialog: {
        visible: false
      },
      restoreForm: {
        selectedBackup: '',
        confirmed: false
      }
    }
  },
  computed: {
    filteredBackupList() {
      if (this.filterType === 'all') {
        return this.backupList
      }
      return this.backupList.filter(backup => backup.type === this.filterType)
    },
    paginatedBackupList() {
      const startIndex = (this.currentPage - 1) * this.pageSize
      const endIndex = startIndex + this.pageSize
      return this.filteredBackupList.slice(startIndex, endIndex)
    }
  },
  mounted() {
    this.initLastBackupTime()
    this.loadBackupList()
  },
  methods: {
    initLastBackupTime() {
      const now = new Date()
      this.lastBackupTime = this.formatDateTime(now)
    },
    formatDateTime(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}`
    },
    loadBackupList() {
      this.loading = true
      setTimeout(() => {
        const backupData = [
          {
            id: 1,
            backup_name: "系统全量备份_20231215",
            backup_type: "FULL",
            backup_path: "/backups/20231215_full_backup.sql",
            backup_size: "1024MB",
            status: "COMPLETED",
            created_by: "admin",
            create_time: "2023-12-15 00:00:00",
            duration: "00:30:00"
          },
          {
            id: 2,
            backup_name: "系统全量备份_20231216",
            backup_type: "FULL",
            backup_path: "/backups/20231216_full_backup.sql",
            backup_size: "1056MB",
            status: "COMPLETED",
            created_by: "admin",
            create_time: "2023-12-16 00:00:00",
            duration: "00:32:00"
          },
          {
            id: 3,
            backup_name: "系统全量备份_20231217",
            backup_type: "FULL",
            backup_path: "/backups/20231217_full_backup.sql",
            backup_size: "1088MB",
            status: "COMPLETED",
            created_by: "admin",
            create_time: "2023-12-17 00:00:00",
            duration: "00:35:00"
          },
          {
            id: 4,
            backup_name: "系统全量备份_20231218",
            backup_type: "FULL",
            backup_path: "/backups/20231218_full_backup.sql",
            backup_size: "1120MB",
            status: "COMPLETED",
            created_by: "admin",
            create_time: "2023-12-18 00:00:00",
            duration: "00:38:00"
          }
        ]
        
        const query = this.$route.query
        if (query.type === 'log') {
          const today = new Date().toISOString().split('T')[0].replace(/-/g, '')
          const logBackup = {
            id: backupData.length + 1,
            backup_name: `日志数据备份_${today}`,
            backup_type: 'LOG',
            backup_path: `/backups/log_${today}.csv`,
            backup_size: '2MB',
            status: 'COMPLETED',
            created_by: 'admin',
            create_time: new Date().toLocaleString(),
            duration: '00:01:00'
          }
          backupData.push(logBackup)
        }
        
        this.backupList = backupData.map(backup => {
          let type = 'manual'
          if (backup.backup_type === 'AUTO') {
            type = 'auto'
          } else if (backup.backup_type === 'LOG') {
            type = 'log'
          }
          
          return {
            id: backup.id,
            name: backup.backup_name,
            type: type,
            size: backup.backup_size,
            status: backup.status,
            createdAt: backup.create_time,
            createdBy: backup.created_by,
            duration: backup.duration,
            path: backup.backup_path
          }
        })
        
        this.loading = false
        this.loadStats()
      }, 500)
    },
    loadStats() {
      const totalBackups = this.backupList.length
      const autoBackupCount = this.backupList.filter(b => b.type === 'auto').length
      const completedBackups = this.backupList.filter(b => b.status === 'COMPLETED').length
      const successRate = totalBackups > 0 ? Math.round((completedBackups / totalBackups) * 100) : 0
      
      let totalSizeMB = 0
      this.backupList.forEach(backup => {
        const sizeMatch = backup.size.match(/(\d+)MB/)
        if (sizeMatch) {
          totalSizeMB += parseInt(sizeMatch[1])
        }
      })
      
      let totalSize
      if (totalSizeMB >= 1024) {
        totalSize = `${(totalSizeMB / 1024).toFixed(1)}GB`
      } else {
        totalSize = `${totalSizeMB}MB`
      }
      
      this.stats = {
        totalBackups: totalBackups,
        totalSize: totalSize,
        autoBackupCount: autoBackupCount,
        successRate: successRate
      }
    },
    createBackup() {
      this.backupLoading = true
      this.$message.info('正在创建备份，请稍候...')
      
      setTimeout(() => {
        const now = new Date()
        const dateStr = now.toISOString().split('T')[0].replace(/-/g, '')
        const newBackup = {
          id: this.backupList.length + 1,
          name: `手动备份_${dateStr}`,
          type: 'manual',
          size: `${Math.floor(Math.random() * 500 + 1000)}MB`,
          status: 'COMPLETED',
          createdAt: this.formatDateTime(now),
          createdBy: 'admin',
          duration: '00:35:00',
          path: `/backups/${dateStr}_full_backup.sql`
        }
        
        this.backupList.unshift(newBackup)
        this.lastBackupTime = this.formatDateTime(now)
        
        this.backupLoading = false
        this.loadStats()
        this.$message.success('备份创建成功！')
      }, 2000)
    },
    refreshData() {
      this.loadBackupList()
      this.loadStats()
      this.$message.success('数据已刷新')
    },
    openAutoBackupDialog() {
      this.autoBackupDialog.visible = true
    },
    saveAutoBackupSettings() {
      this.$message.success('自动备份设置已保存')
      this.autoBackupDialog.visible = false
    },
    openRestoreDialog() {
      this.restoreForm.selectedBackup = ''
      this.restoreForm.confirmed = false
      this.restoreDialog.visible = true
    },
    downloadBackup(backup) {
      this.$message.info(`正在下载: ${backup.name}`)
      
      const logData = this.generateLogData()
      const csvContent = this.convertToCSV(logData)
      
      const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      const url = URL.createObjectURL(blob)
      link.setAttribute('href', url)
      link.setAttribute('download', `${backup.name}.csv`)
      link.style.visibility = 'hidden'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
      
      this.$message.success('备份数据下载成功！')
    },
    generateLogData() {
      const logs = []
      const types = ['登录', '操作', '系统', '错误']
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
      
      for (let i = 0; i < 200; i++) {
        const type = types[Math.floor(Math.random() * types.length)]
        logs.push({
          id: `LOG${String(i + 1).padStart(6, '0')}`,
          type: type,
          user: users[Math.floor(Math.random() * users.length)],
          action: actions[Math.floor(Math.random() * actions.length)],
          ip: ips[Math.floor(Math.random() * ips.length)],
          browser: browsers[Math.floor(Math.random() * browsers.length)],
          createdAt: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toLocaleString()
        })
      }
      
      return logs
    },
    convertToCSV(data) {
      const headers = ['日志ID', '类型', '操作用户', '操作内容', 'IP地址', '浏览器', '操作时间']
      const csvRows = [headers.join(',')]
      
      data.forEach(row => {
        const values = [
          row.id,
          row.type,
          row.user,
          `"${row.action}"`,
          row.ip,
          row.browser,
          row.createdAt
        ]
        csvRows.push(values.join(','))
      })
      
      return csvRows.join('\n')
    },
    restoreBackup(backup) {
      this.restoreForm.selectedBackup = backup.id
      this.restoreForm.confirmed = false
      this.restoreDialog.visible = true
    },
    confirmRestore() {
      if (!this.restoreForm.confirmed) {
        this.$message.warning('请勾选恢复确认')
        return
      }
      
      if (!this.restoreForm.selectedBackup) {
        this.$message.warning('请选择备份文件')
        return
      }
      
      this.$message.info('正在恢复数据，请稍候...')
      setTimeout(() => {
        this.restoreDialog.visible = false
        this.$message.success('数据恢复成功！')
      }, 2000)
    },
    deleteBackup(backup) {
      this.$confirm(`确定要删除备份 "${backup.name}" 吗？`, '删除备份', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }).then(() => {
        const index = this.backupList.findIndex(b => b.id === backup.id)
        if (index !== -1) {
          this.backupList.splice(index, 1)
        }
        this.loadStats()
        this.$message.success('备份已删除')
      }).catch(() => {})
    },
    cleanOldBackups() {
      this.$confirm('确定要清理7天前的旧备份吗？', '清理备份', {
        confirmButtonText: '确定清理',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }).then(() => {
        const sevenDaysAgo = new Date()
        sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7)
        
        this.backupList = this.backupList.filter(backup => {
          const backupDate = new Date(backup.createdAt)
          return backupDate >= sevenDaysAgo
        })
        
        this.loadStats()
        this.$message.success('旧备份已清理')
      }).catch(() => {})
    },
    handleFilterChange() {
      this.currentPage = 1
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
    },
    handleCurrentChange(current) {
      this.currentPage = current
    },
    getBackupTypeTag(type) {
      const tagMap = {
        'manual': 'primary',
        'auto': 'success',
        'log': 'warning'
      }
      return tagMap[type] || 'info'
    },
    getBackupTypeText(type) {
      const textMap = {
        'manual': '手动',
        'auto': '自动',
        'log': '日志'
      }
      return textMap[type] || '未知'
    }
  }
}
</script>

<style scoped>
.data-backup-page {
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

.stat-total .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-size .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-auto .stat-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-success .stat-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
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
}

.action-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
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

.btn-warning {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  box-shadow: 0 4px 12px rgba(250, 112, 154, 0.3);
}

.btn-warning:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(250, 112, 154, 0.4);
}

.btn-danger {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.3);
}

.btn-danger:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(245, 87, 108, 0.4);
}

.refresh-btn {
  padding: 12px 24px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  background: #fff;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.refresh-btn:hover {
  border-color: #409EFF;
  color: #409EFF;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.refresh-btn i {
  font-size: 16px;
}

.backup-list-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.backup-list-card >>> .el-card__body {
  padding: 24px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.list-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.list-title i {
  color: #409EFF;
}

.list-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.filter-select {
  width: 150px;
}

.backup-table {
  cursor: pointer;
}

.backup-table >>> .el-table__row:hover {
  background-color: #f5f7fa;
}

.action-btns {
  display: flex;
  gap: 6px;
  justify-content: center;
  flex-wrap: wrap;
}

.table-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 4px;
  color: #fff;
}

.table-btn i {
  font-size: 14px;
}

.btn-download {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 6px rgba(102, 126, 234, 0.3);
}

.btn-download:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(102, 126, 234, 0.4);
}

.btn-restore {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  box-shadow: 0 2px 6px rgba(67, 233, 123, 0.3);
}

.btn-restore:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(67, 233, 123, 0.4);
}

.btn-delete {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  box-shadow: 0 2px 6px rgba(245, 87, 108, 0.3);
}

.btn-delete:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(245, 87, 108, 0.4);
}

.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-content {
  padding: 10px 0;
}

.backup-form,
.restore-form {
  padding: 10px 0;
}

@media (max-width: 768px) {
  .data-backup-page {
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
  
  .action-section {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  
  .action-buttons {
    justify-content: flex-start;
  }
  
  .list-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
