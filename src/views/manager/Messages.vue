/**
 * 消息中心页面
 * 文件路径: src/views/manager/Messages.vue
 * 功能描述: 管理所有系统消息和通知，展示全部/未读/已读消息统计卡片并支持点击筛选，
 *           消息列表（消息内容、发送时间、状态），支持关键字搜索、按状态筛选（全部/未读/已读），
 *           单条标为已读/删除、全部标为已读、清空消息，消息详情查看
 * 关联文件:
 * - src/api/index.js: 提供消息数据增删改查接口
 */
<template>
  <div class="messages-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <i class="el-icon-message"></i>
            消息中心
          </h1>
          <p class="page-subtitle">管理所有系统消息和通知</p>
        </div>
        <div class="header-right">
          <el-button 
            type="primary" 
            @click="markAllAsRead"
            :disabled="messages.length === 0"
            class="mark-all-btn"
          >
            <i class="el-icon-check"></i>
            全部标为已读
          </el-button>
          <el-button 
            type="danger" 
            plain
            @click="deleteAllMessages"
            :disabled="messages.length === 0"
            class="delete-all-btn"
          >
            <i class="el-icon-delete"></i>
            清空消息
          </el-button>
        </div>
      </div>
    </div>

    <!-- 消息统计 -->
    <el-card shadow="never" class="stats-card">
      <div class="stats-content">
        <div class="stat-item" @click="filterByStatus('all')">
          <i class="el-icon-message stat-icon total"></i>
          <div class="stat-info">
            <div class="stat-number">{{ totalMessages }}</div>
            <div class="stat-label">全部消息</div>
          </div>
        </div>
        <div class="stat-item" @click="filterByStatus('unread')">
          <i class="el-icon-chat-line-round stat-icon unread"></i>
          <div class="stat-info">
            <div class="stat-number">{{ unreadMessages }}</div>
            <div class="stat-label">未读消息</div>
          </div>
        </div>
        <div class="stat-item" @click="filterByStatus('read')">
          <i class="el-icon-chat-dot-round stat-icon read"></i>
          <div class="stat-info">
            <div class="stat-number">{{ readMessages }}</div>
            <div class="stat-label">已读消息</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 消息列表 -->
    <el-card shadow="never" class="messages-card">
      <div class="messages-header">
        <div class="messages-title">
          <i class="el-icon-s-order"></i>
          消息列表
          <span class="message-count">({{ filteredMessages.length }})</span>
        </div>
        <div class="messages-actions">
          <el-input
            placeholder="搜索消息内容"
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
            @change="filterMessages"
            size="medium"
            class="status-filter"
            placeholder="筛选状态"
          >
            <el-option label="全部消息" value="all" />
            <el-option label="未读消息" value="unread" />
            <el-option label="已读消息" value="read" />
          </el-select>
        </div>
      </div>

      <!-- 消息列表 -->
      <div v-if="filteredMessages.length > 0" class="messages-list">
        <div 
          v-for="message in paginatedMessages" 
          :key="message.id"
          class="message-item"
          :class="{ 'unread-item': !message.read }"
        >
          <div class="message-header" @click="toggleMessage(message.id)">
            <div class="message-info">
              <div class="message-icon-container" :class="message.type || 'info'">
                <i :class="message.icon || 'el-icon-message'" class="message-icon"></i>
              </div>
              <div class="message-meta">
                <div class="message-title">{{ message.title }}</div>
                <div class="message-time">{{ formatTime(message.time) }}</div>
              </div>
            </div>
            <div class="message-actions">
              <el-tag v-if="!message.read" type="danger" size="small" class="unread-tag">未读</el-tag>
              <i class="el-icon-arrow-right message-arrow" :class="{ 'rotated': activeNames.includes(message.id) }"></i>
            </div>
          </div>
          <transition name="slide-fade">
            <div v-if="activeNames.includes(message.id)" class="message-content">
              <div class="content-text">{{ message.content }}</div>
              <div class="message-footer">
                <el-button 
                  type="text" 
                  @click="markAsRead(message)"
                  v-if="!message.read"
                  class="action-button"
                >
                  <i class="el-icon-check"></i>
                  标记为已读
                </el-button>
                <el-button 
                  type="text" 
                  @click="deleteMessage(message)"
                  class="action-button delete-button"
                >
                  <i class="el-icon-delete"></i>
                  删除
                </el-button>
              </div>
            </div>
          </transition>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-icon-container">
          <i class="el-icon-message empty-icon"></i>
        </div>
        <h3 class="empty-title">暂无消息</h3>
        <p class="empty-description">您还没有收到任何消息</p>
        <el-button type="primary" @click="goToHome" class="empty-button">
          <i class="el-icon-s-home"></i>
          返回首页
        </el-button>
      </div>

      <!-- 分页 -->
      <div v-if="filteredMessages.length > 0" class="pagination-box">
        <div class="pagination-info">
          共 {{ filteredMessages.length }} 条消息
        </div>
        <el-pagination
          background
          :current-page="pagination.pageNum"
          :page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="sizes, prev, pager, next, jumper"
          :total="filteredMessages.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          class="pagination-control"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Messages',
  data() {
    return {
      // 消息数据
      messages: [],
      // 筛选后的消息
      filteredMessages: [],
      // 搜索关键词
      searchKeyword: '',
      // 状态筛选
      statusFilter: 'all', // all, unread, read
      // 分页
      pagination: {
        pageNum: 1,
        pageSize: 10
      },
      // 展开的消息
      activeNames: []
    }
  },
  computed: {
    // 总消息数
    totalMessages() {
      return this.messages.length
    },
    // 未读消息数
    unreadMessages() {
      return this.messages.filter(msg => !msg.read).length
    },
    // 已读消息数
    readMessages() {
      return this.messages.filter(msg => msg.read).length
    },
    // 分页后的消息
    paginatedMessages() {
      const start = (this.pagination.pageNum - 1) * this.pagination.pageSize
      const end = start + this.pagination.pageSize
      return this.filteredMessages.slice(start, end)
    }
  },
  created() {
    this.loadMessages()
  },
  methods: {
    // 加载消息
    loadMessages() {
      try {
        const messages = JSON.parse(localStorage.getItem('xm-messages') || '[]')
        // 按时间倒序排序
        this.messages = messages.sort((a, b) => new Date(b.time) - new Date(a.time))
        this.filterMessages()
      } catch (error) {
        console.error('加载消息失败:', error)
        this.messages = []
        this.filteredMessages = []
      }
    },

    // 筛选消息
    filterMessages() {
      let filtered = [...this.messages]

      // 状态筛选
      if (this.statusFilter === 'unread') {
        filtered = filtered.filter(msg => !msg.read)
      } else if (this.statusFilter === 'read') {
        filtered = filtered.filter(msg => msg.read)
      }

      // 关键词搜索
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        filtered = filtered.filter(msg => 
          (msg.title && msg.title.toLowerCase().includes(keyword)) ||
          (msg.content && msg.content.toLowerCase().includes(keyword))
        )
      }

      this.filteredMessages = filtered
      this.pagination.pageNum = 1
    },

    // 按状态筛选
    filterByStatus(status) {
      this.statusFilter = status
      this.filterMessages()
    },

    // 搜索
    handleSearch() {
      this.filterMessages()
    },

    // 标记为已读
    markAsRead(message) {
      message.read = true
      this.updateMessages()
      this.$message.success('消息已标记为已读')
    },

    // 全部标为已读
    markAllAsRead() {
      this.messages.forEach(msg => {
        msg.read = true
      })
      this.updateMessages()
      this.$message.success('所有消息已标记为已读')
    },

    // 删除消息
    deleteMessage(message) {
      this.$confirm('确定要删除此消息吗？', '确认删除', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(() => {
        const index = this.messages.findIndex(msg => msg.id === message.id)
        if (index !== -1) {
          this.messages.splice(index, 1)
          this.updateMessages()
          this.$message.success('消息删除成功')
        }
      }).catch(() => {})
    },

    // 清空所有消息
    deleteAllMessages() {
      this.$confirm('确定要清空所有消息吗？此操作不可恢复。', '确认清空', {
        type: 'warning',
        confirmButtonText: '确定清空',
        cancelButtonText: '取消'
      }).then(() => {
        this.messages = []
        this.updateMessages()
        this.$message.success('所有消息已清空')
      }).catch(() => {})
    },

    // 更新消息到本地存储
    updateMessages() {
      try {
        localStorage.setItem('xm-messages', JSON.stringify(this.messages))
        this.filterMessages()
        // 触发消息更新事件
        window.dispatchEvent(new CustomEvent('xm-messages-updated'))
      } catch (error) {
        console.error('更新消息失败:', error)
        this.$message.error('更新消息失败')
      }
    },

    // 格式化时间
    formatTime(time) {
      if (!time) return '未知时间'
      
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
      
      return date.toLocaleString()
    },

    // 分页大小变化
    handleSizeChange(size) {
      this.pagination.pageSize = size
      this.pagination.pageNum = 1
    },

    // 页码变化
    handleCurrentChange(pageNum) {
      this.pagination.pageNum = pageNum
    },

    // 切换消息展开/收起
    toggleMessage(messageId) {
      const index = this.activeNames.indexOf(messageId)
      if (index === -1) {
        // 展开当前消息，同时关闭其他消息
        this.activeNames = [messageId]
        // 自动标记为已读
        const message = this.messages.find(msg => msg.id === messageId)
        if (message && !message.read) {
          this.markAsRead(message)
        }
      } else {
        // 收起当前消息
        this.activeNames = this.activeNames.filter(id => id !== messageId)
      }
    },

    // 返回首页
    goToHome() {
      this.$router.push('/home')
    }
  }
}
</script>

<style scoped>
.messages-page {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  min-height: calc(100vh - 60px);
  box-sizing: border-box;
}

/* 页面头部 */
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

.header-right {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

/* 统计卡片 */
.stats-card {
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
}

.stats-content {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.stat-item {
  flex: 1;
  min-width: 150px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-item:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  font-size: 32px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: white;
}

.stat-icon.total {
  background: linear-gradient(135deg, #409EFF, #66B1FF);
}

.stat-icon.unread {
  background: linear-gradient(135deg, #F56C6C, #F78989);
}

.stat-icon.read {
  background: linear-gradient(135deg, #67C23A, #85ce61);
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

/* 消息卡片 */
.messages-card {
  border-radius: 12px;
  overflow: hidden;
}

.messages-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
  flex-wrap: wrap;
  gap: 15px;
}

.messages-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 10px;
}

.messages-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 10px;
}

.message-count {
  font-size: 14px;
  font-weight: 400;
  color: #909399;
  background: #f5f7fa;
  padding: 2px 8px;
  border-radius: 10px;
}

.messages-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.search-input {
  width: 300px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.search-input:focus {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.status-filter {
  min-width: 120px;
  border-radius: 8px;
}

/* 消息列表 */
.messages-list {
  margin: 20px 0;
}

.message-item {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  margin-bottom: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.message-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.unread-item {
  border-left: 4px solid #f56c6c;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.message-header:hover {
  background: #f5f7fa;
}

.message-info {
  display: flex;
  align-items: center;
  gap: 15px;
  flex: 1;
}

.message-icon-container {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message-icon-container.info {
  background: linear-gradient(135deg, #409EFF, #66B1FF);
}

.message-icon-container.success {
  background: linear-gradient(135deg, #67C23A, #85ce61);
}

.message-icon-container.warning {
  background: linear-gradient(135deg, #E6A23C, #ebb563);
}

.message-icon-container.error {
  background: linear-gradient(135deg, #F56C6C, #f78989);
}

.message-icon {
  font-size: 20px;
  color: white;
}

.message-meta {
  flex: 1;
  min-width: 0;
}

.message-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}

.message-time {
  font-size: 12px;
  color: #909399;
}

.message-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.unread-tag {
  font-weight: 600;
  border-radius: 10px;
}

.message-arrow {
  font-size: 16px;
  color: #909399;
  transition: transform 0.3s ease;
}

.message-arrow.rotated {
  transform: rotate(90deg);
}

/* 消息内容 */
.message-content {
  padding: 0 20px 20px;
  background: #fafafa;
  border-top: 1px solid #ebeef5;
}

.content-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 15px;
  padding-top: 20px;
}

.message-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.action-button {
  font-size: 13px;
  padding: 4px 12px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.action-button:hover {
  background: #ecf5ff;
  color: #409EFF;
}

.delete-button {
  color: #f56c6c;
}

.delete-button:hover {
  background: #fef0f0;
  color: #f56c6c;
}

/* 动画效果 */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter,
.slide-fade-leave-to {
  transform: translateY(-10px);
  opacity: 0;
}

/* 空状态 */
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

.empty-button {
  border-radius: 8px;
  padding: 8px 24px;
  transition: all 0.3s ease;
}

.empty-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

/* 分页 */
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

.pagination-control {
  display: flex;
  align-items: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-right {
    width: 100%;
    justify-content: flex-start;
  }
  
  .stats-content {
    flex-direction: column;
  }
  
  .stat-item {
    width: 100%;
  }
  
  .messages-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .messages-actions {
    width: 100%;
    justify-content: space-between;
  }
  
  .search-input {
    flex: 1;
    min-width: 0;
  }
  
  .status-filter {
    min-width: 100px;
  }
  
  .pagination-box {
    flex-direction: column;
    align-items: center;
    gap: 10px;
  }
  
  .pagination-info {
    order: 2;
  }
  
  .pagination-control {
    order: 1;
  }
}
</style>