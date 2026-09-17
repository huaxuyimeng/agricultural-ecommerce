/**
 * 公告管理页面
 * 文件路径: src/views/manager/Notice.vue
 * 功能描述: 管理系统公告与通知（对应数据库 news 表），展示公告列表，
 *           支持按标题搜索和类型筛选，新增公告（标题、内容、类型、发布范围）、
 *           编辑/删除公告、查看公告详情，分页展示，对应数据库 news 表
 * 关联文件:
 * - src/api/index.js: 提供公告增删改查接口
 * - src/views/manager/PublishNotice.vue: 发布公告页面
 */
<template>
  <div class="notice-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">
          <i class="el-icon-bell"></i>
          公告管理
        </h2>
        <p class="page-subtitle">管理系统公告，及时发布重要信息</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAdd">
          <i class="el-icon-plus"></i>
          发布公告
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card shadow="hover" class="stat-card" :class="stat.class">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: stat.color }">
              <i :class="stat.icon"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选栏 -->
    <el-card class="filter-card" shadow="never">
      <div class="filter-bar">
        <div class="filter-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索标题、作者或标签"
            clearable
            prefix-icon="el-icon-search"
            class="search-input"
            @keyup.enter.native="handleSearch"
            @clear="handleSearch"
          />
          <el-select v-model="categoryFilter" placeholder="全部分类" clearable class="category-select" @change="handleSearch">
            <el-option label="政策" value="policy" />
            <el-option label="科技" value="technology" />
            <el-option label="活动" value="activity" />
            <el-option label="公告" value="notice" />
            <el-option label="新闻" value="news" />
            <el-option label="其他" value="other" />
          </el-select>
        </div>
        <div class="filter-right">
          <el-button icon="el-icon-refresh" @click="loadData" circle title="刷新"></el-button>
        </div>
      </div>
    </el-card>

    <!-- 批量操作栏 -->
    <el-card class="batch-card" shadow="never" v-if="selectedIds.length > 0">
      <div class="batch-bar">
        <span class="batch-info">已选择 <strong>{{ selectedIds.length }}</strong> 条公告</span>
        <div class="batch-actions">
          <el-button type="primary" size="mini" plain @click="handleBatchDelete">
            <i class="el-icon-delete"></i> 批量删除
          </el-button>
          <el-button size="mini" plain @click="clearSelection">
            <i class="el-icon-refresh-left"></i> 取消选择
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 公告列表 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        ref="table"
        class="notice-table"
        :header-cell-style="{ background: '#fafafa', color: '#606266', fontWeight: '600' }"
        empty-text="暂无公告数据"
        @selection-change="handleSelectionChange"
        @row-click="handleView"
      >
        <el-table-column type="selection" width="50" align="center" fixed="left" />
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="标题" min-width="200">
          <template #default="{ row }">
            <div class="title-cell">
              <div class="title-text">{{ row.title }}</div>
              <el-tag v-if="row.category" :type="getCategoryTagType(row.category)" size="mini" effect="light">
                {{ getCategoryLabel(row.category) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="作者" width="100" align="center">
          <template #default="{ row }">{{ row.author || '系统' }}</template>
        </el-table-column>
        <el-table-column prop="views" label="浏览" width="80" align="center">
          <template #default="{ row }">
            <span class="views-text">
              <i class="el-icon-view"></i>
              {{ row.views || 0 }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="likes" label="点赞" width="80" align="center">
          <template #default="{ row }">
            <span class="likes-text">
              <i class="el-icon-thumb"></i>
              {{ row.likes || 0 }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="160" align="center">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button type="text" size="mini" @click.stop="handleView(row)">
                <i class="el-icon-view"></i> 查看
              </el-button>
              <el-button type="text" size="mini" @click.stop="handleEdit(row)">
                <i class="el-icon-edit"></i> 编辑
              </el-button>
              <el-button type="text" size="mini" class="danger-text" @click.stop="handleDelete(row)">
                <i class="el-icon-delete"></i> 删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          :current-page.sync="pageNum"
          :page-size.sync="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="total"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 公告详情对话框 -->
    <el-dialog :visible.sync="detailVisible" title="公告详情" width="700px" append-to-body class="detail-dialog">
      <div v-if="currentNotice" class="detail-content">
        <div class="detail-header">
          <h3 class="detail-title">{{ currentNotice.title }}</h3>
          <div class="detail-tags">
            <el-tag v-if="currentNotice.category" :type="getCategoryTagType(currentNotice.category)" size="small">
              {{ getCategoryLabel(currentNotice.category) }}
            </el-tag>
          </div>
        </div>
        <div class="detail-meta">
          <span><i class="el-icon-user"></i> {{ currentNotice.author || '系统' }}</span>
          <span><i class="el-icon-time"></i> {{ formatFullTime(currentNotice.createTime) }}</span>
          <span><i class="el-icon-view"></i> {{ currentNotice.views || 0 }} 浏览</span>
          <span><i class="el-icon-thumb"></i> {{ currentNotice.likes || 0 }} 点赞</span>
        </div>
        <el-divider />
        <div class="detail-body" v-html="currentNotice.content"></div>
        <div v-if="currentNotice.tags" class="detail-tags-section">
          <span class="tags-label">标签：</span>
          <el-tag v-for="tag in currentNotice.tags.split(',')" :key="tag" size="mini" style="margin-right: 6px;">
            {{ tag.trim() }}
          </el-tag>
        </div>
      </div>
      <div slot="footer">
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleEdit(currentNotice)">编辑</el-button>
      </div>
    </el-dialog>

    <!-- 新增/编辑公告对话框 -->
    <el-dialog :visible.sync="editVisible" :title="editMode === 'add' ? '发布公告' : '编辑公告'" width="700px" append-to-body class="edit-dialog">
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入公告标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="editForm.category" placeholder="请选择分类" style="width: 100%;">
            <el-option label="政策" value="policy" />
            <el-option label="科技" value="technology" />
            <el-option label="活动" value="activity" />
            <el-option label="公告" value="notice" />
            <el-option label="新闻" value="news" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="editForm.author" placeholder="请输入作者" maxlength="50" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="editForm.content" type="textarea" :rows="8" placeholder="请输入公告内容" maxlength="5000" show-word-limit />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="editForm.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getNewsPage, createNews, updateNews, deleteNews } from '@/api'

export default {
  name: 'Notice',
  data() {
    return {
      loading: false,
      saving: false,
      searchKeyword: '',
      categoryFilter: '',
      pageNum: 1,
      pageSize: 10,
      total: 0,
      tableData: [],
      selectedIds: [],
      detailVisible: false,
      currentNotice: null,
      editVisible: false,
      editMode: 'add',
      editForm: {
        id: null,
        title: '',
        category: '',
        author: '',
        content: '',
        tags: ''
      },
      editRules: {
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
        content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
      },
      stats: {
        total: 0,
        policy: 0,
        technology: 0,
        activity: 0
      }
    }
  },
  computed: {
    statsList() {
      return [
        { key: 'total', icon: 'el-icon-s-comment', label: '公告总数', value: this.stats.total, color: '#409eff', class: '' },
        { key: 'policy', icon: 'el-icon-s-flag', label: '政策公告', value: this.stats.policy, color: '#f56c6c', class: '' },
        { key: 'technology', icon: 'el-icon-cpu', label: '科技资讯', value: this.stats.technology, color: '#67c23a', class: '' },
        { key: 'activity', icon: 'el-icon-present', label: '活动公告', value: this.stats.activity, color: '#e6a23c', class: '' }
      ]
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const params = {
          page: this.pageNum,
          pageSize: this.pageSize
        }
        if (this.categoryFilter) {
          params.category = this.categoryFilter
        }
        const res = await getNewsPage(params)
        let list = []
        if (res?.data?.list) list = res.data.list
        else if (res?.data?.records) list = res.data.records
        else if (res?.data) list = Array.isArray(res.data) ? res.data : []
        this.tableData = list
        this.total = res?.data?.total || 0
        this.calcStats(list)
      } catch (err) {
        this.$message.error('加载公告失败')
      } finally {
        this.loading = false
      }
    },

    calcStats(list) {
      this.stats.total = this.total
      this.stats.policy = list.filter(n => n.category === 'policy').length
      this.stats.technology = list.filter(n => n.category === 'technology').length
      this.stats.activity = list.filter(n => n.category === 'activity').length
    },

    handleSearch() {
      this.pageNum = 1
      this.loadData()
    },

    handleSelectionChange(selection) {
      this.selectedIds = selection.map(item => item.id)
    },

    clearSelection() {
      this.selectedIds = []
      this.$refs.table && this.$refs.table.clearSelection()
    },

    handleAdd() {
      this.$router.push('/publish-notice')
    },

    handleEdit(row) {
      this.editMode = 'edit'
      this.editForm = {
        id: row.id,
        title: row.title || '',
        category: row.category || '',
        author: row.author || '',
        content: row.content || '',
        tags: row.tags || ''
      }
      this.editVisible = true
    },

    handleView(row) {
      this.currentNotice = row
      this.detailVisible = true
    },

    async handleSave() {
      try {
        await this.$refs.editFormRef.validate()
        this.saving = true
        if (this.editMode === 'add') {
          await createNews(this.editForm)
          this.$message.success('发布成功')
        } else {
          await updateNews(this.editForm.id, this.editForm)
          this.$message.success('更新成功')
        }
        this.editVisible = false
        this.loadData()
      } catch (err) {
        if (err !== false) {
          this.$message.error('操作失败')
        }
      } finally {
        this.saving = false
      }
    },

    handleDelete(row) {
      this.$confirm(`确定删除公告「${row.title}」吗？`, '确认删除', { type: 'warning' }).then(async () => {
        try {
          await deleteNews(row.id)
          this.$message.success('删除成功')
          this.loadData()
        } catch (err) {
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },

    async handleBatchDelete() {
      this.$confirm(`确定删除选中的 ${this.selectedIds.length} 条公告吗？`, '批量删除', { type: 'warning' }).then(async () => {
        try {
          for (const id of this.selectedIds) {
            await deleteNews(id)
          }
          this.$message.success('批量删除成功')
          this.selectedIds = []
          this.loadData()
        } catch (err) {
          this.$message.error('批量删除失败')
        }
      }).catch(() => {})
    },

    formatTime(time) {
      if (!time) return '-'
      const date = new Date(time)
      return date.toLocaleString('zh-CN', {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit'
      })
    },

    formatFullTime(time) {
      if (!time) return '-'
      return new Date(time).toLocaleString('zh-CN')
    },

    getCategoryLabel(category) {
      const map = {
        policy: '政策', technology: '科技', activity: '活动',
        notice: '公告', news: '新闻', other: '其他'
      }
      return map[category] || category || '公告'
    },

    getCategoryTagType(category) {
      const typeMap = {
        policy: 'danger', technology: 'primary', activity: 'success',
        notice: 'warning', news: 'info', other: 'info'
      }
      return typeMap[category] || 'info'
    }
  }
}
</script>

<style scoped>
.notice-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 4px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-title i {
  color: #409eff;
}

.page-subtitle {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  border-radius: 12px;
  border: none;
}

.stat-card ::v-deep .el-card__body {
  padding: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.filter-card,
.batch-card,
.table-card {
  border-radius: 12px;
  margin-bottom: 16px;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 280px;
}

.category-select {
  width: 140px;
}

.batch-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.batch-info {
  font-size: 14px;
  color: #606266;
}

.batch-info strong {
  color: #409eff;
}

.batch-actions {
  display: flex;
  gap: 8px;
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-text {
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.views-text,
.likes-text {
  color: #909399;
  font-size: 13px;
}

.views-text i,
.likes-text i {
  margin-right: 3px;
}

.action-btns {
  display: flex;
  gap: 4px;
  justify-content: center;
}

.danger-text {
  color: #f56c6c;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 详情对话框 */
.detail-dialog ::v-deep .el-dialog__header {
  padding-bottom: 0;
}

.detail-content {
  padding: 0 10px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.detail-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  flex: 1;
}

.detail-tags {
  flex-shrink: 0;
  margin-left: 12px;
}

.detail-meta {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #909399;
  margin-bottom: 16px;
}

.detail-meta i {
  margin-right: 4px;
}

.detail-body {
  line-height: 1.8;
  color: #606266;
  font-size: 14px;
}

.detail-body ::v-deep h2 {
  font-size: 16px;
  color: #303133;
  margin: 16px 0 8px;
}

.detail-body ::v-deep p {
  margin: 8px 0;
}

.detail-tags-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.tags-label {
  font-size: 13px;
  color: #909399;
  margin-right: 8px;
}
</style>
