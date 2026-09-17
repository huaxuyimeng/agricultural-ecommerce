/**
 * 管理员管理页面
 * 文件路径: src/views/manager/Admin.vue
 * 功能描述: 管理系统管理员账号，包括管理员总数/活跃数/超级管理员数量统计展示，支持添加、编辑、删除管理员，
 *           按用户名、姓名、邮箱、手机号搜索筛选，按角色和状态过滤，支持管理员启用/禁用状态切换，分页展示
 * 关联文件:
 * - src/api/index.js: 提供管理员用户的增删改查接口
 * - src/views/manager/AdminPerson.vue: 管理员个人中心页面
 */
<template>
  <div class="admin-manage-page">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <i class="el-icon-user"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">管理员总数</div>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
            <i class="el-icon-circle-check"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.active }}</div>
            <div class="stat-label">活跃管理员</div>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
            <i class="el-icon-medal"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.superAdmin }}</div>
            <div class="stat-label">超级管理员</div>
          </div>
        </div>
      </el-card>
    </div>

    <el-card class="admin-card" shadow="never">
      <!-- 卡片头部 -->
      <div class="card-header">
        <div class="header-left">
          <div class="title-icon">
            <i class="el-icon-user-solid"></i>
          </div>
          <div>
            <h3 class="header-title">管理员管理</h3>
            <p class="header-subtitle">管理系统用户和权限</p>
          </div>
        </div>
        <el-button type="primary" size="medium" @click="openAddDialog" class="add-btn">
          <i class="el-icon-plus"></i> 添加管理员
        </el-button>
      </div>

      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="searchQuery"
            placeholder="搜索管理员姓名、邮箱或手机号"
            clearable
            class="search-input"
            @keyup.enter="loadData"
            @clear="loadData"
          >
            <template #prefix>
              <i class="el-icon-search"></i>
            </template>
          </el-input>

          <el-select v-model="roleFilter" placeholder="角色筛选" clearable class="filter-select" @change="loadData">
            <el-option label="全部角色" value="" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>

          <el-select v-model="statusFilter" placeholder="状态筛选" clearable class="filter-select" @change="loadData">
            <el-option label="全部状态" value="" />
            <el-option label="活跃" :value="1" />
            <el-option label="非活跃" :value="0" />
          </el-select>
        </div>
        <div class="toolbar-right">
          <el-button icon="el-icon-refresh" @click="loadData" :loading="loading" circle title="刷新数据"></el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        class="admin-table"
        :header-cell-style="{ background: '#fafafa', color: '#606266', fontWeight: '600' }"
        empty-text="暂无管理员数据"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" min-width="130" align="center">
          <template #default="{ row }">
            <div class="username-cell">
              <el-avatar :size="32" :style="{ background: getAvatarColor(row.username) }">
                {{ row.username.charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="username-text">{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" min-width="110" align="center">
          <template #default="{ row }">
            {{ row.name || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180" align="center">
          <template #default="{ row }">
            {{ row.email || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" align="center">
          <template #default="{ row }">
            {{ row.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'" size="small" effect="light">
              {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <div class="status-cell">
              <span class="status-dot" :class="row.status === 1 ? 'active' : 'inactive'"></span>
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '活跃' : '非活跃' }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-tooltip content="编辑" placement="top" :enterable="false">
                <el-button type="primary" size="mini" icon="el-icon-edit" circle @click="openEditDialog(row)" class="action-btn" />
              </el-tooltip>
              <el-tooltip :content="row.status === 1 ? '禁用' : '启用'" placement="top" :enterable="false">
                <el-button
                  :type="row.status === 1 ? 'warning' : 'success'"
                  size="mini"
                  :icon="row.status === 1 ? 'el-icon-close' : 'el-icon-check'"
                  circle
                  @click="toggleStatus(row)"
                  class="action-btn"
                />
              </el-tooltip>
              <el-tooltip content="删除" placement="top" :enterable="false" v-if="row.role !== 'ADMIN'">
                <el-button type="danger" size="mini" icon="el-icon-delete" circle @click="handleDelete(row)" class="action-btn" />
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          :current-page.sync="currentPage"
          :page-size.sync="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      :visible.sync="dialogVisible"
      :title="isAdd ? '添加管理员' : '编辑管理员信息'"
      width="520px"
      :close-on-click-modal="false"
      class="admin-dialog"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px" class="admin-form">
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="请输入用户名" 
            :disabled="!isAdd"
            prefix-icon="el-icon-user"
          />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input 
            v-model="form.name" 
            placeholder="请输入真实姓名"
            prefix-icon="el-icon-s-custom"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input 
            v-model="form.email" 
            placeholder="请输入邮箱地址"
            prefix-icon="el-icon-message"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input 
            v-model="form.phone" 
            placeholder="请输入手机号码"
            prefix-icon="el-icon-phone"
          />
        </el-form-item>
        <el-form-item v-if="isAdd" label="密码" prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请设置登录密码" 
            show-password
            prefix-icon="el-icon-lock"
          />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="ADMIN">
              <span style="float: left">👑</span>
              <span style="float: right; color: #8492a6; font-size: 13px">拥有所有权限</span>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false" size="medium">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving" size="medium">
          {{ isAdd ? '立即添加' : '保存修改' }}
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getUserPage, createUser, updateUser, deleteUser } from '@/api'

export default {
  name: 'Admin',
  data() {
    const validatePhone = (rule, value, callback) => {
      if (value && !/^1\d{10}$/.test(value)) {
        callback(new Error('请输入正确的手机号'))
      } else {
        callback()
      }
    }

    return {
      loading: false,
      saving: false,
      tableData: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      searchQuery: '',
      roleFilter: '',
      statusFilter: '',
      dialogVisible: false,
      isAdd: true,
      stats: {
        total: 0,
        active: 0,
        superAdmin: 0
      },
      form: {
        id: null,
        username: '',
        name: '',
        email: '',
        phone: '',
        password: '',
        role: 'ADMIN'
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱地址', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        phone: [
          { validator: validatePhone, trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度至少 6 个字符', trigger: 'blur' }
        ],
        role: [
          { required: true, message: '请选择角色', trigger: 'change' }
        ]
      }
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    getAvatarColor(username) {
      const colors = [
        '#409EFF', '#67C23A', '#E6A23C', '#F56C6C', 
        '#909399', '#667eea', '#f093fb', '#4facfe',
        '#43e97b', '#fa709a', '#fee140', '#30cfd0'
      ]
      let hash = 0
      for (let i = 0; i < username.length; i++) {
        hash = username.charCodeAt(i) + ((hash << 5) - hash)
      }
      return colors[Math.abs(hash) % colors.length]
    },

    formatDateTime(time) {
      if (!time) return '-'
      return new Date(time).toLocaleString('zh-CN', {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit'
      })
    },

    async loadData() {
      this.loading = true
      try {
        const params = {
          page: this.currentPage,
          pageSize: this.pageSize,
          role: 'ADMIN',
          keyword: this.searchQuery || undefined
        }
        const res = await getUserPage(params)
        let list = []
        if (res?.data) {
          list = Array.isArray(res.data) ? res.data : (res.data.list || [])
        }
        this.tableData = list
        this.total = res?.data?.total || list.length
        
        // 统计数据使用后端返回的总数
        this.stats.total = res?.data?.total || list.length
        this.stats.active = list.filter(u => u.status === 1).length
        this.stats.superAdmin = list.filter(u => u.role === 'ADMIN').length
      } catch (error) {
        console.error('加载数据失败:', error)
        this.$message.error('加载数据失败')
      } finally {
        this.loading = false
      }
    },

    handleSizeChange(val) {
      this.pageSize = val
      this.loadData()
    },

    handlePageChange(val) {
      this.currentPage = val
      this.loadData()
    },

    openAddDialog() {
      this.isAdd = true
      const userSettings = JSON.parse(localStorage.getItem('xm-settings-user') || '{}')
      const defaultAvatar = userSettings.defaultAvatar || ''
      this.form = {
        id: null,
        username: '',
        name: '',
        email: '',
        phone: '',
        password: '',
        role: 'ADMIN',
        avatar: defaultAvatar
      }
      this.dialogVisible = true
    },

    openEditDialog(row) {
      this.isAdd = false
      this.form = { ...row }
      this.dialogVisible = true
    },

    async handleSave() {
      const valid = await this.$refs.formRef.validate().catch(() => false)
      if (!valid) return

      this.saving = true
      try {
        if (this.isAdd) {
          await createUser(this.form)
          this.$message.success('添加成功')
          this.currentPage = 1
        } else {
          await updateUser(this.form.id, this.form)
          this.$message.success('更新成功')
        }
        this.dialogVisible = false
        this.loadData()
      } catch (error) {
        console.error('保存失败:', error)
        const errorMsg = error.message || ''
        if (errorMsg.includes('Duplicate entry') && errorMsg.includes('uk_email')) {
          this.$message.error('该邮箱已被使用，请更换其他邮箱地址')
        } else if (errorMsg.includes('Duplicate entry') && errorMsg.includes('uk_username')) {
          this.$message.error('该用户名已被使用，请更换其他用户名')
        } else {
          this.$message.error(error.message || '保存失败')
        }
      } finally {
        this.saving = false
      }
    },

    async toggleStatus(row) {
      const action = row.status === 1 ? '禁用' : '启用'
      try {
        await this.$confirm(`确定要${action}管理员 ${row.name || row.username} 吗？`, '提示', {
          type: 'warning'
        })
        await updateUser(row.id, { ...row, status: row.status === 1 ? 0 : 1 })
        this.$message.success(`${action}成功`)
        this.loadData()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(`${action}失败`)
        }
      }
    },

    async handleDelete(row) {
      if (row.role === 'ADMIN') {
        this.$message.error('管理员不能删除')
        return
      }
      try {
        await this.$confirm(`确定要删除管理员 ${row.name || row.username} 吗？`, '警告', {
          type: 'error'
        })
        await deleteUser(row.id)
        this.$message.success('删除成功')
        this.loadData()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    }
  }
}
</script>

<style scoped>
.admin-manage-page {
  padding: 0;
  background: #f5f7fa;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: white;
  font-size: 24px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1;
  margin-bottom: 6px;
}

.stat-label {
  font-size: 13px;
  color: #8c8c8c;
  font-weight: 500;
}

.admin-card {
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(to right, #fafafa, #ffffff);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 4px 0;
}

.header-subtitle {
  font-size: 12px;
  color: #8c8c8c;
  margin: 0;
}

.add-btn {
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 500;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  flex-wrap: wrap;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-input {
  flex: 1;
  min-width: 250px;
}

.filter-select {
  width: 140px;
}

.admin-table {
  min-height: 300px;
}

.username-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: flex-start;
}

.username-text {
  font-weight: 500;
  color: #1a1a1a;
}

.status-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.status-dot.active {
  background: #52c41a;
  box-shadow: 0 0 0 3px rgba(82, 196, 26, 0.2);
}

.status-dot.inactive {
  background: #d9d9d9;
}

.action-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.action-btn {
  width: 32px;
  height: 32px;
  padding: 0;
  border-radius: 50%;
  border: none;
  transition: all 0.3s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.action-btn:active {
  transform: translateY(0);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.action-btn.el-button--primary {
  background: linear-gradient(135deg, #409EFF, #66b1ff);
  color: white;
}

.action-btn.el-button--primary:hover {
  background: linear-gradient(135deg, #66b1ff, #409EFF);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.action-btn.el-button--success {
  background: linear-gradient(135deg, #67C23A, #85ce61);
  color: white;
}

.action-btn.el-button--success:hover {
  background: linear-gradient(135deg, #85ce61, #67C23A);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
}

.action-btn.el-button--warning {
  background: linear-gradient(135deg, #E6A23C, #ebb563);
  color: white;
}

.action-btn.el-button--warning:hover {
  background: linear-gradient(135deg, #ebb563, #E6A23C);
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.3);
}

.action-btn.el-button--danger {
  background: linear-gradient(135deg, #F56C6C, #f78989);
  color: white;
}

.action-btn.el-button--danger:hover {
  background: linear-gradient(135deg, #f78989, #F56C6C);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
  box-shadow: none !important;
}

.pagination-wrapper {
  padding: 20px 24px;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

.admin-dialog ::v-deep .el-dialog__header {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(to right, #fafafa, #ffffff);
}

.admin-dialog ::v-deep .el-dialog__title {
  font-size: 18px;
  font-weight: 600;
}

.admin-dialog ::v-deep .el-dialog__body {
  padding: 24px;
}

.admin-form {
  padding: 0 8px;
}

.dialog-footer {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: 1fr;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .add-btn {
    width: 100%;
  }

  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-left {
    flex-direction: column;
  }

  .search-input,
  .filter-select {
    width: 100%;
    min-width: auto;
  }

  .pagination-wrapper {
    justify-content: center;
  }
}
</style>
