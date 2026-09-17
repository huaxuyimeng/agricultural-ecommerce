/**
 * 用户管理页面
 * 文件路径: src/views/manager/User.vue
 * 功能描述: 管理系统注册用户（农户端）信息，展示在线用户数，支持用户名/姓名/手机/邮箱关键字搜索，
 *           角色筛选（普通用户/商家/管理员）、状态筛选（正常/禁用）、审核状态筛选（待审核/已通过/已驳回），
 *           注册日期范围筛选，用户列表展示（用户名、姓名、手机、邮箱、角色、状态、注册时间），
 *           支持添加/编辑/删除/审核用户，用户启用/禁用切换，分页展示
 * 关联文件:
 * - src/api/index.js: 提供用户数据增删改查接口
 * - src/views/manager/Admin.vue: 管理员管理页面
 */
<template>
  <div class="user-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h2 class="page-title">
            <i class="el-icon-user-solid" style="margin-right: 8px; color: #409EFF;"></i>
            用户管理
          </h2>
          <p class="page-subtitle">管理系统用户信息，包括添加、编辑、删除、审核等操作</p>
        </div>
        <div class="header-right">
          <el-tag type="info" size="medium">
            当前在线用户: {{ onlineCount }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 过滤和统计卡片 -->
    <el-card class="filter-card" shadow="never">
      <div class="filter-content">
        <div class="filter-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索用户名/姓名/手机/邮箱"
            clearable
            style="width: 300px"
            @keyup.enter="search"
            @clear="handleSearchClear"
          >
            <template #prefix>
              <i class="el-icon-search"></i>
            </template>
          </el-input>
          
          <el-select
            v-model="filterRole"
            placeholder="用户角色"
            clearable
            style="width: 120px; margin-left: 10px"
            @change="handleFilterChange"
          >
            <el-option label="普通用户" value="USER"></el-option>
            <el-option label="商家用户" value="MERCHANT"></el-option>
            <el-option label="管理员" value="ADMIN"></el-option>
          </el-select>
          
          <el-select
            v-model="filterStatus"
            placeholder="用户状态"
            clearable
            style="width: 100px; margin-left: 10px"
            @change="handleFilterChange"
          >
            <el-option label="正常" :value="1"></el-option>
            <el-option label="禁用" :value="0"></el-option>
          </el-select>
          
          <el-select
            v-model="filterApproval"
            placeholder="审核状态"
            clearable
            style="width: 100px; margin-left: 10px"
            @change="handleFilterChange"
          >
            <el-option label="待审核" value="PENDING"></el-option>
            <el-option label="已通过" value="APPROVED"></el-option>
            <el-option label="已驳回" value="REJECTED"></el-option>
          </el-select>
          
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="注册开始日期"
            end-placeholder="注册结束日期"
            value-format="YYYY-MM-DD"
            style="width: 320px; margin-left: 10px"
            @change="handleDateChange"
          >
          </el-date-picker>
        </div>
        <div class="filter-right">
          <el-button-group>
            <el-button 
              type="primary" 
              icon="el-icon-search"
              @click="search"
              :loading="loading"
            >
              搜索
            </el-button>
            <el-button 
              type="info" 
              icon="el-icon-refresh"
              @click="reset"
            >
              重置
            </el-button>
            <el-button 
              type="success" 
              icon="el-icon-download"
              @click="exportData"
            >
              导出
            </el-button>
          </el-button-group>
        </div>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                <i class="el-icon-user"></i>
              </div>
              <div class="stat-info">
                <div class="stat-label">总用户数</div>
                <div class="stat-value">{{ totalUserCount }}</div>
                <div class="stat-trend">
                  <i class="el-icon-top" style="color: #67C23A;"></i>
                  <span style="color: #67C23A;">+12%</span> 上月
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb, #f5576c);">
                <i class="el-icon-s-shop"></i>
              </div>
              <div class="stat-info">
                <div class="stat-label">商家用户</div>
                <div class="stat-value">{{ merchantCount }}</div>
                <div class="stat-trend">
                  <i class="el-icon-top" style="color: #67C23A;"></i>
                  <span style="color: #67C23A;">+8%</span> 上月
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe, #00f2fe);">
                <i class="el-icon-s-data"></i>
              </div>
              <div class="stat-info">
                <div class="stat-label">待审核</div>
                <div class="stat-value">{{ pendingCount }}</div>
                <div class="stat-trend">
                  <i class="el-icon-bottom" style="color: #F56C6C;"></i>
                  <span style="color: #F56C6C;">-5%</span> 上月
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7);">
                <i class="el-icon-s-flag"></i>
              </div>
              <div class="stat-info">
                <div class="stat-label">活跃用户</div>
                <div class="stat-value">{{ activeCount }}</div>
                <div class="stat-trend">
                  <i class="el-icon-top" style="color: #67C23A;"></i>
                  <span style="color: #67C23A;">+15%</span> 上月
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 操作按钮区域 -->
    <div class="operation-section">
      <el-button 
        type="primary" 
        @click="handleAdd"
        icon="el-icon-plus"
        style="margin-right: 10px"
      >
        新增用户
      </el-button>
      <el-button 
        type="success" 
        @click="handleBatchApprove"
        icon="el-icon-check"
        :disabled="selectedIds.length === 0"
        style="margin-right: 10px"
      >
        批量通过
      </el-button>
      <el-button 
        type="warning" 
        @click="handleBatchReject"
        icon="el-icon-close"
        :disabled="selectedIds.length === 0"
        style="margin-right: 10px"
      >
        批量驳回
      </el-button>
      <el-button 
        type="info" 
        @click="handleEnable"
        icon="el-icon-check"
        :disabled="selectedIds.length === 0"
        style="margin-right: 10px"
      >
        批量启用
      </el-button>
      <el-button 
        type="warning" 
        @click="handleDisable"
        icon="el-icon-close"
        :disabled="selectedIds.length === 0"
        style="margin-right: 10px"
      >
        批量禁用
      </el-button>
      <el-button 
        type="danger" 
        @click="handleBatchDelete"
        icon="el-icon-delete"
        :disabled="selectedIds.length === 0"
      >
        批量删除
      </el-button>
    </div>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <div class="table-container">
        <el-table 
          :data="tableData" 
          stripe 
          style="width: 100%"
          @selection-change="handleSelectionChange"
          v-loading="loading"
          element-loading-text="数据加载中..."
          element-loading-spinner="el-icon-loading"
          element-loading-background="rgba(255, 255, 255, 0.7)"
          :default-sort="{prop: 'id', order: 'descending'}"
        >
          <el-table-column type="selection" width="55" align="center"></el-table-column>
          <el-table-column prop="id" label="用户ID" width="100" align="center" sortable>
            <template #default="{ row }">
              <span class="user-id">#{{ String(row.id).padStart(6, '0') }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="avatar" label="头像" width="100" align="center">
            <template #default="{ row }">
              <div class="user-avatar">
                <el-avatar 
                  :size="45" 
                  :src="getAvatarUrl(row.avatar)"
                  :style="{ 
                    backgroundColor: getAvatarColor(row.id),
                    cursor: 'pointer',
                    transition: 'all 0.3s ease'
                  }"
                  @click="showUserDetail(row)"
                >
                  <span style="font-size: 16px; font-weight: bold; color: white;">
                    {{ row.name && row.name.charAt(0) || '?' }}
                  </span>
                </el-avatar>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="username" label="账号" min-width="120">
            <template #default="{ row }">
              <div class="user-info-cell">
                <div class="user-name">{{ row.username }}</div>
                <div class="user-id-display">ID: {{ row.id }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="姓名" width="100"></el-table-column>
          <el-table-column prop="phone" label="联系方式" width="140">
            <template #default="{ row }">
              <div class="contact-cell">
                <div class="contact-phone">
                  <i class="el-icon-phone" style="margin-right: 4px;"></i>
                  {{ row.phone || '未设置' }}
                </div>
                <div v-if="row.email" class="contact-email">
                  <i class="el-icon-message" style="margin-right: 4px;"></i>
                  {{ row.email }}
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="role" label="角色" width="120" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="getRoleTagType(row.role)"
                effect="dark"
                size="small"
                :style="{ 
                  borderRadius: '12px',
                  padding: '4px 12px',
                  fontWeight: '600',
                  transition: 'all 0.3s ease'
                }"
                @click="handleRoleClick(row)"
              >
                <i :class="getRoleIcon(row.role)" style="margin-right: 4px;"></i>
                {{ getRoleText(row.role) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="account" label="账户余额" width="120" align="right" sortable>
            <template #default="{ row }">
              <div class="account-cell">
                <span class="account-value">¥{{ row.account && row.account.toFixed(2) || '0.00' }}</span>
                <div v-if="row.account > 0" class="account-trend">
                  <i class="el-icon-top" style="color: #67C23A; font-size: 12px;"></i>
                  <span style="color: #67C23A; font-size: 12px;">正常</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="注册时间" width="150" sortable>
            <template #default="{ row }">
              <div class="time-cell">
                <div class="time-date">{{ formatDate(row.createTime) }}</div>
                <div v-if="row.createTime" class="time-ago">
                  约{{ getTimeAgo(row.createTime) }}前
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="approvalStatus" label="审核状态" width="100" align="center">
            <template #default="{ row }">
              <span v-if="!row.approvalStatus || row.approvalStatus === ''" class="status-text status-unknown">
                未审核
              </span>
              <el-tag 
                v-else
                :type="getApprovalTagType(row.approvalStatus)"
                :effect="row.approvalStatus === 'PENDING' ? 'dark' : 'light'"
                size="small"
                class="approval-tag"
                @click="handleApprovalClick(row)"
              >
                <i :class="getApprovalIcon(row.approvalStatus)" style="margin-right: 4px;"></i>
                {{ getApprovalText(row.approvalStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                active-color="#13ce66"
                inactive-color="#ff4949"
                @change="handleStatusChange(row)"
              ></el-switch>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-tooltip content="通过" placement="top" :enterable="false" v-if="row.approvalStatus === 'PENDING'">
                  <el-button
                    type="success"
                    size="mini"
                    icon="el-icon-check"
                    circle
                    @click="handleApprove(row)"
                    class="action-btn"
                  />
                </el-tooltip>
                <el-tooltip content="驳回" placement="top" :enterable="false" v-if="row.approvalStatus === 'PENDING'">
                  <el-button
                    type="warning"
                    size="mini"
                    icon="el-icon-close"
                    circle
                    @click="handleReject(row)"
                    class="action-btn"
                  />
                </el-tooltip>
                <el-tooltip content="编辑" placement="top" :enterable="false">
                  <el-button
                    type="primary"
                    size="mini"
                    icon="el-icon-edit"
                    circle
                    @click="handleEdit(row)"
                    class="action-btn"
                  />
                </el-tooltip>
                <el-tooltip content="重置密码" placement="top" :enterable="false">
                  <el-button
                    type="warning"
                    size="mini"
                    icon="el-icon-refresh"
                    circle
                    @click="handleResetPassword(row)"
                    class="action-btn"
                  />
                </el-tooltip>
                <el-tooltip :content="row.id === currentUserId ? '不能删除自己' : '删除'" placement="top" :enterable="false">
                  <el-button
                    type="danger"
                    size="mini"
                    icon="el-icon-delete"
                    circle
                    @click="handleDelete(row)"
                    :disabled="row.id === currentUserId"
                    class="action-btn"
                  />
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          background
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
          :current-page="pageNum"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :pager-count="7"
        >
        </el-pagination>
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="600px"
      :close-on-click-modal="false"
      @closed="handleDialogClosed"
      :destroy-on-close="true"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        label-position="left"
        class="form-container"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="账号" prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入账号"
                :disabled="isEditMode"
                clearable
              >
                <template #prefix>
                  <i class="el-icon-user"></i>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input
                v-model="form.name"
                placeholder="请输入姓名"
                clearable
              >
                <template #prefix>
                  <i class="el-icon-s-custom"></i>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input
                v-model="form.phone"
                placeholder="请输入手机号"
                clearable
              >
                <template #prefix>
                  <i class="el-icon-phone"></i>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="form.email"
                placeholder="请输入邮箱"
                clearable
              >
                <template #prefix>
                  <i class="el-icon-message"></i>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="角色" prop="role">
              <el-select
                v-model="form.role"
                placeholder="请选择角色"
                style="width: 100%"
                clearable
                @change="handleRoleChange"
              >
                <el-option label="普通用户" value="USER">
                  <div style="display: flex; align-items: center;">
                    <i class="el-icon-user" style="color: #409EFF; margin-right: 8px;"></i>
                    <span>普通用户</span>
                  </div>
                </el-option>
                <el-option label="商家用户" value="MERCHANT">
                  <div style="display: flex; align-items: center;">
                    <i class="el-icon-s-shop" style="color: #E6A23C; margin-right: 8px;"></i>
                    <span>商家用户</span>
                  </div>
                </el-option>
                <el-option label="管理员" value="ADMIN">
                  <div style="display: flex; align-items: center;">
                    <i class="el-icon-s-tools" style="color: #F56C6C; margin-right: 8px;"></i>
                    <span>管理员</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="isEditMode ? '重置密码' : '登录密码'" prop="password">
              <el-input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                :placeholder="isEditMode ? '不修改请留空' : '请输入登录密码'"
                show-password
              >
                <template #prefix>
                  <i class="el-icon-lock"></i>
                </template>
              </el-input>
              <div v-if="isEditMode" style="font-size: 12px; color: #909399; margin-top: 4px;">
                不修改密码请留空
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="账户余额" prop="account">
              <el-input-number
                v-model="form.account"
                :min="0"
                :step="100"
                :precision="2"
                style="width: 100%"
                controls-position="right"
              >
                <template #prepend>¥</template>
              </el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="审核状态" prop="approvalStatus">
              <el-select
                v-model="form.approvalStatus"
                placeholder="请选择审核状态"
                style="width: 100%"
                clearable
              >
                <el-option label="待审核" value="PENDING">
                  <div style="display: flex; align-items: center;">
                    <i class="el-icon-time" style="color: #E6A23C; margin-right: 8px;"></i>
                    <span>待审核</span>
                  </div>
                </el-option>
                <el-option label="已通过" value="APPROVED">
                  <div style="display: flex; align-items: center;">
                    <i class="el-icon-check" style="color: #67C23A; margin-right: 8px;"></i>
                    <span>已通过</span>
                  </div>
                </el-option>
                <el-option label="已驳回" value="REJECTED">
                  <div style="display: flex; align-items: center;">
                    <i class="el-icon-close" style="color: #F56C6C; margin-right: 8px;"></i>
                    <span>已驳回</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item v-if="form.role === 'MERCHANT'" label="店铺名称" prop="shopName">
          <el-input
            v-model="form.shopName"
            placeholder="请输入店铺名称"
            clearable
          >
            <template #prefix>
              <i class="el-icon-s-shop"></i>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
            active-color="#13ce66"
            inactive-color="#ff4949"
            style="margin-right: 20px"
          ></el-switch>
        </el-form-item>

        <el-form-item label="用户描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入用户描述（可选）"
            maxlength="200"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false" :disabled="saving">取 消</el-button>
          <el-button
            type="primary"
            @click="handleSave"
            :loading="saving"
            :disabled="saving"
          >
            {{ saving ? '保存中...' : '确 定' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      title="重置密码"
      :visible.sync="resetPasswordDialogVisible"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="resetPasswordFormRef"
        :model="resetPasswordForm"
        :rules="resetPasswordRules"
        label-width="100px"
      >
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="resetPasswordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="resetPasswordForm.confirmPassword"
            type="password"
            placeholder="请确认新密码"
            show-password
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetPasswordDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="confirmResetPassword">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量操作确认对话框 -->
    <el-dialog
      :title="batchDialogTitle"
      :visible.sync="batchDialogVisible"
      width="400px"
      :close-on-click-modal="false"
    >
      <div style="text-align: center; padding: 20px 0;">
        <i class="el-icon-warning" style="color: #E6A23C; font-size: 40px; margin-bottom: 20px;"></i>
        <p style="font-size: 16px; line-height: 1.5; color: #333;">
          {{ batchDialogMessage }}
        </p>
        <p v-if="selectedIds.length > 0" style="margin-top: 10px; color: #909399; font-size: 14px;">
          本次操作将影响 <span style="color: #F56C6C; font-weight: bold;">{{ selectedIds.length }}</span> 个用户
        </p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchDialogVisible = false">取 消</el-button>
          <el-button 
            :type="batchDialogType" 
            @click="confirmBatchAction"
            :loading="batchProcessing"
          >
            {{ batchProcessing ? '处理中...' : '确 定' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import * as userManager from '@/data/user'
import { getUserPage, updateUser, deleteUser } from '@/api'

export default {
  name: "UserManagement",
  data() {
    // 验证手机号
    const validatePhone = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入手机号'))
      } else if (!/^1[3-9]\d{9}$/.test(value)) {
        callback(new Error('请输入正确的手机号'))
      } else {
        callback()
      }
    }
    
    // 验证邮箱
    const validateEmail = (rule, value, callback) => {
      if (value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
        callback(new Error('请输入正确的邮箱格式'))
      } else {
        callback()
      }
    }
    
    // 验证用户名
    const validateUsername = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入用户名'))
        return
      }
      if (value.length < 3 || value.length > 20) {
        callback(new Error('用户名长度在 3 到 20 个字符'))
        return
      }
      callback()
    }
    
    // 验证密码
    const validatePassword = (rule, value, callback) => {
      if (!this.isEditMode && !value) {
        callback(new Error('请输入密码'))
        return
      }
      if (value && value.length < 6) {
        callback(new Error('密码长度不能少于6位'))
        return
      }
      if (value && value.length > 20) {
        callback(new Error('密码长度不能超过20位'))
        return
      }
      callback()
    }
    
    // 确认密码验证
    const validateConfirmPassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请确认密码'))
      } else if (value !== this.resetPasswordForm.newPassword) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }
    
    return {
      // 表格数据
      tableData: [],
      selectedIds: [],
      
      // 分页
      pageNum: 1,
      pageSize: 10,
      total: 0,
      
      // 搜索和过滤
      searchKeyword: '',
      filterRole: '',
      filterStatus: null,
      filterApproval: '',
      dateRange: [],
      
      // 对话框
      dialogVisible: false,
      dialogTitle: '新增用户',
      isEditMode: false,
      saving: false,
      
      // 重置密码对话框
      resetPasswordDialogVisible: false,
      resetPasswordForm: {
        userId: null,
        newPassword: '',
        confirmPassword: ''
      },
      
      // 批量操作对话框
      batchDialogVisible: false,
      batchDialogTitle: '',
      batchDialogMessage: '',
      batchDialogType: 'primary',
      batchProcessing: false,
      batchAction: '',
      
      // 当前登录用户ID
      currentUserId: null,
      
      // 统计信息
      onlineCount: 0,
      totalUserCount: 0,
      merchantCount: 0,
      pendingCount: 0,
      activeCount: 0,
      
      // 加载状态
      loading: false,
      
      // 是否显示密码
      showPassword: false,
      
      // 表单数据
      form: {
        id: null,
        username: '',
        name: '',
        phone: '',
        email: '',
        role: 'USER',
        password: '',
        approvalStatus: 'APPROVED',
        account: 0.00,
        status: 1,
        description: '',
        shopName: ''
      },
      
      // 表单验证规则
      rules: {
        username: [
          { required: true, validator: validateUsername, trigger: 'blur' }
        ],
        name: [
          { required: true, message: '请输入姓名', trigger: 'blur' },
          { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
        ],
        phone: [
          { required: true, validator: validatePhone, trigger: 'blur' }
        ],
        email: [
          { validator: validateEmail, trigger: 'blur' }
        ],
        role: [
          { required: true, message: '请选择角色', trigger: 'change' }
        ],
        password: [
          { validator: validatePassword, trigger: 'blur' }
        ],
        approvalStatus: [
          { required: true, message: '请选择审核状态', trigger: 'change' }
        ],
        account: [
          { type: 'number', min: 0, message: '账户余额不能为负数', trigger: 'blur' }
        ]
      },
      
      // 重置密码验证规则
      resetPasswordRules: {
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, validator: validateConfirmPassword, trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.loadCurrentUser()
    this.loadData()
    this.loadStatistics()
  },
  methods: {
    // 获取头像URL
    getAvatarUrl(avatar) {
      if (!avatar) return ''
      if (typeof avatar !== 'string') return avatar
      if (avatar.startsWith('data:') || /^https?:\/\//.test(avatar)) return avatar

      try {
        return require(`@/assets/imgs/user/${avatar}`)
      } catch (e) {
        return avatar
      }
    },
    
    // 根据用户ID生成头像颜色
    getAvatarColor(userId) {
      const colors = [
        '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7',
        '#DDA0DD', '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E9'
      ]
      const index = (userId || 0) % colors.length
      return colors[index]
    },
    
    // 格式化日期
    formatDate(date) {
      if (!date) return '-'
      return new Date(date).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
      }).replace(/\//g, '-')
    },
    
    // 获取相对时间
    getTimeAgo(date) {
      if (!date) return '-'
      const now = new Date()
      const createTime = new Date(date)
      const diffMs = now - createTime
      const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
      
      if (diffDays < 1) {
        const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
        if (diffHours < 1) {
          const diffMinutes = Math.floor(diffMs / (1000 * 60))
          return `${diffMinutes}分钟`
        }
        return `${diffHours}小时`
      } else if (diffDays < 30) {
        return `${diffDays}天`
      } else if (diffDays < 365) {
        const diffMonths = Math.floor(diffDays / 30)
        return `${diffMonths}个月`
      } else {
        const diffYears = Math.floor(diffDays / 365)
        return `${diffYears}年`
      }
    },
    
    // 获取角色图标
    getRoleIcon(role) {
      const icons = {
        'USER': 'el-icon-user',
        'MERCHANT': 'el-icon-s-shop',
        'ADMIN': 'el-icon-s-tools'
      }
      return icons[role] || 'el-icon-user'
    },
    
    // 获取审核状态图标
    getApprovalIcon(status) {
      const icons = {
        'PENDING': 'el-icon-time',
        'APPROVED': 'el-icon-check',
        'REJECTED': 'el-icon-close'
      }
      return icons[status] || 'el-icon-s-data'
    },
    
    // 获取角色文本
    getRoleText(role) {
      return userManager.getRoleText(role)
    },
    
    // 获取角色标签类型
    getRoleTagType(role) {
      return userManager.getRoleTagType(role)
    },

    getApprovalText(status) {
      return userManager.getApprovalText(status)
    },

    getApprovalTagType(status) {
      return userManager.getApprovalTagType(status)
    },
    
    // 加载当前登录用户
    loadCurrentUser() {
      try {
        const userStr = localStorage.getItem('xm-user')
        if (userStr) {
          const user = JSON.parse(userStr)
          this.currentUserId = user.id
        }
      } catch {
        this.currentUserId = null
      }
    },
    
    // 加载用户列表 - 调用后端API
    async loadData() {
      this.loading = true
      
      try {
        const params = {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          username: this.searchKeyword || undefined,
          role: this.filterRole || undefined,
          status: this.filterStatus !== null ? this.filterStatus : undefined
        }
        
        const result = await getUserPage(params)
        
        // 应用审核状态筛选（如果有的话）
        let list = result.data?.records || result.data?.list || []
        
        if (this.filterApproval) {
          list = list.filter(user => user.approvalStatus === this.filterApproval)
        }
        
        this.tableData = list
        this.total = result.data?.total || list.length
        
      } catch {
        this.$message.error('加载用户列表失败')
        this.tableData = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },
    
    // 加载统计信息 - 调用后端API
    async loadStatistics() {
      try {
        const result = await getUserPage({ pageNum: 1, pageSize: 1000 })
        const allUsers = result.data?.records || result.data?.list || []
        
        this.totalUserCount = allUsers.length
        this.merchantCount = allUsers.filter(user => user.role === 'MERCHANT').length
        this.pendingCount = allUsers.filter(user => user.approvalStatus === 'PENDING').length
        this.activeCount = allUsers.filter(user => user.status === 1).length
        this.onlineCount = Math.floor(Math.random() * 50) + 10 // 模拟在线用户数
      } catch {
      }
    },
    
    // 搜索
    search() {
      this.pageNum = 1
      this.loadData()
    },
    
    // 搜索框清空
    handleSearchClear() {
      this.search()
    },
    
    // 过滤器变化
    handleFilterChange() {
      this.search()
    },
    
    // 日期变化
    handleDateChange() {
      this.search()
    },
    
    // 重置搜索
    reset() {
      this.searchKeyword = ''
      this.filterRole = ''
      this.filterStatus = null
      this.filterApproval = ''
      this.dateRange = []
      this.pageNum = 1
      this.loadData()
    },
    
    // 导出数据
    exportData() {
      try {
        const csvContent = this.convertToCSV(this.tableData)
        this.downloadCSV(csvContent, 'users_export.csv')
        this.$message.success('导出成功')
      } catch {
        this.$message.error('导出失败')
      }
    },
    
    // 转换为CSV格式
    convertToCSV(users) {
      const headers = ['ID', '用户名', '姓名', '手机号', '邮箱', '角色', '账户余额', '审核状态', '状态', '创建时间']
      const rows = users.map(user => [
        user.id,
        user.username,
        user.name,
        user.phone,
        user.email || '',
        this.getRoleText(user.role),
        user.account?.toFixed(2) || '0.00',
        this.getApprovalText(user.approvalStatus),
        user.status === 1 ? '正常' : '禁用',
        this.formatDate(user.createTime)
      ])
      
      const csv = [headers, ...rows]
        .map(row => row.map(cell => `"${cell}"`).join(','))
        .join('\n')
      
      return csv
    },
    
    // 下载CSV文件
    downloadCSV(content, filename) {
      const blob = new Blob(['\uFEFF' + content], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      const url = URL.createObjectURL(blob)
      link.setAttribute('href', url)
      link.setAttribute('download', filename)
      link.style.visibility = 'hidden'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    },
    
    // 新增用户
    handleAdd() {
      this.dialogTitle = '新增用户'
      this.isEditMode = false
      this.dialogVisible = true
      
      const userSettings = JSON.parse(localStorage.getItem('xm-settings-user') || '{}')
      const defaultAvatar = userSettings.defaultAvatar || ''
      
      this.$nextTick(() => {
        this.form = {
          id: null,
          username: '',
          name: '',
          phone: '',
          email: '',
          role: 'USER',
          password: '',
          approvalStatus: 'APPROVED',
          account: 0.00,
          status: 1,
          description: '',
          shopName: '',
          avatar: defaultAvatar
        }
        if (this.$refs.formRef) {
          this.$refs.formRef.clearValidate()
        }
      })
    },
    
    // 编辑用户
    handleEdit(row) {
      this.dialogTitle = '编辑用户'
      this.isEditMode = true
      
      // 深拷贝数据
      this.form = {
        ...row,
        status: row.status,
        approvalStatus: row.approvalStatus || 'APPROVED',
        password: ''
      }
      
      this.dialogVisible = true
    },
    
    // 保存用户 - 调用后端API
    async handleSave() {
      this.$refs.formRef.validate(async (valid) => {
        if (!valid) {
          this.$message.warning('请检查表单数据')
          return
        }
        
        this.saving = true
        
        try {
          const payload = { ...this.form }
          
          // 如果不是商家，清空店铺名称
          if (payload.role !== 'MERCHANT') {
            payload.shopName = ''
          }
          
          // 如果是编辑模式且密码为空，则不更新密码
          if (this.isEditMode && !payload.password) {
            delete payload.password
          }
          
          if (this.isEditMode) {
            // 编辑用户 - 调用API
            await updateUser(this.form.id, payload)
            this.$message.success('用户更新成功')
            this.dialogVisible = false
            this.loadData()
            this.loadStatistics()
          } else {
            // 新增用户 - 调用API
            await userManager.addUser(payload)
            this.$message.success('用户添加成功')
            this.dialogVisible = false
            this.loadData()
            this.loadStatistics()
          }
        } catch (error) {
          const errorMsg = error.message || ''
          if (errorMsg.includes('Duplicate entry') && errorMsg.includes('uk_email')) {
            this.$message.error('该邮箱已被使用，请更换其他邮箱地址')
          } else if (errorMsg.includes('Duplicate entry') && errorMsg.includes('uk_username')) {
            this.$message.error('该用户名已被使用，请更换其他用户名')
          } else {
            this.$message.error(error.message || '保存用户失败')
          }
        } finally {
          this.saving = false
        }
      })
    },
    
    // 角色变化处理
    handleRoleChange(role) {
      if (role !== 'MERCHANT') {
        this.form.shopName = ''
      }
    },
    
    // 角色点击
    handleRoleClick(row) {
      this.filterRole = row.role
      this.search()
    },
    
    // 审核状态点击
    handleApprovalClick(row) {
      this.filterApproval = row.approvalStatus
      this.search()
    },
    
    // 用户状态变化
    handleStatusChange(row) {
      const action = row.status === 1 ? '启用' : '禁用'
      this.$confirm(
        `确定要${action}用户 "${row.name}" 吗？`,
        '确认操作',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
          center: true
        }
      ).then(async () => {
        try {
          await updateUser(row.id, { status: row.status })
          this.$message.success(`用户已${action}`)
          this.loadData()
          this.loadStatistics()
        } catch (error) {
          this.$message.error('操作失败')
          row.status = row.status === 1 ? 0 : 1
        }
      }).catch(() => {
        row.status = row.status === 1 ? 0 : 1
      })
    },
    
    // 单个审核通过
    handleApprove(row) {
      this.$confirm(
        `确定要通过用户 "${row.name}" 的审核吗？`,
        '确认通过',
        {
          confirmButtonText: '通过',
          cancelButtonText: '取消',
          type: 'success',
          center: true
        }
      ).then(async () => {
        try {
          await userManager.approveUser(row.id)
          this.$message.success('审核通过成功')
          this.loadData()
          this.loadStatistics()
        } catch {
          this.$message.error('审核通过失败')
        }
      }).catch(() => {})
    },
    
    // 单个驳回
    handleReject(row) {
      this.$confirm(
        `确定要驳回用户 "${row.name}" 的申请吗？`,
        '确认驳回',
        {
          confirmButtonText: '驳回',
          cancelButtonText: '取消',
          type: 'warning',
          center: true
        }
      ).then(async () => {
        try {
          await userManager.rejectUser(row.id)
          this.$message.success('已驳回该用户')
          this.loadData()
          this.loadStatistics()
        } catch {
          this.$message.error('驳回失败')
        }
      }).catch(() => {})
    },
    
    // 重置密码
    handleResetPassword(row) {
      this.resetPasswordForm = {
        userId: row.id,
        newPassword: '',
        confirmPassword: ''
      }
      this.resetPasswordDialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.resetPasswordFormRef) {
          this.$refs.resetPasswordFormRef.clearValidate()
        }
      })
    },
    
    // 确认重置密码
    async confirmResetPassword() {
      this.$refs.resetPasswordFormRef.validate(async (valid) => {
        if (!valid) {
          return
        }
        
        try {
          await updateUser(this.resetPasswordForm.userId, {
            password: this.resetPasswordForm.newPassword
          })
          
          this.$message.success('密码重置成功')
          this.resetPasswordDialogVisible = false
          this.resetPasswordForm = {
            userId: null,
            newPassword: '',
            confirmPassword: ''
          }
        } catch {
          this.$message.error('密码重置失败')
        }
      })
    },
    
    // 删除单个用户
    handleDelete(row) {
      // 不能删除自己
      if (row.id === this.currentUserId) {
        this.$message.warning('不能删除当前登录的用户')
        return
      }
      
      this.$confirm(
        `确定要删除用户 "${row.name}" 吗？此操作不可恢复`,
        '确认删除',
        {
          confirmButtonText: '删除',
          cancelButtonText: '取消',
          type: 'error',
          center: true,
          confirmButtonClass: 'el-button--danger'
        }
      ).then(async () => {
        try {
          await deleteUser(row.id)
          this.$message.success('删除成功')
          this.loadData()
          this.loadStatistics()
          this.selectedIds = this.selectedIds.filter(id => id !== row.id)
        } catch {
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },
    
    // 批量操作
    handleBatchApprove() {
      this.batchDialogTitle = '批量通过'
      this.batchDialogMessage = '确定要通过选中的用户吗？'
      this.batchDialogType = 'success'
      this.batchAction = 'approve'
      this.batchDialogVisible = true
    },
    
    handleBatchReject() {
      this.batchDialogTitle = '批量驳回'
      this.batchDialogMessage = '确定要驳回选中的用户吗？'
      this.batchDialogType = 'warning'
      this.batchAction = 'reject'
      this.batchDialogVisible = true
    },
    
    handleEnable() {
      this.batchDialogTitle = '批量启用'
      this.batchDialogMessage = '确定要启用选中的用户吗？'
      this.batchDialogType = 'success'
      this.batchAction = 'enable'
      this.batchDialogVisible = true
    },
    
    handleDisable() {
      this.batchDialogTitle = '批量禁用'
      this.batchDialogMessage = '确定要禁用选中的用户吗？'
      this.batchDialogType = 'warning'
      this.batchAction = 'disable'
      this.batchDialogVisible = true
    },
    
    handleBatchDelete() {
      // 检查是否包含当前用户
      if (this.selectedIds.includes(this.currentUserId)) {
        this.$message.warning('不能删除当前登录的用户，请取消选择')
        return
      }
      
      this.batchDialogTitle = '批量删除'
      this.batchDialogMessage = '确定要删除选中的用户吗？此操作不可恢复'
      this.batchDialogType = 'danger'
      this.batchAction = 'delete'
      this.batchDialogVisible = true
    },
    
    // 确认批量操作
    async confirmBatchAction() {
      this.batchProcessing = true
      
      try {
        let successCount = 0
        let failedUsers = []
        
        for (const userId of this.selectedIds) {
          try {
            switch (this.batchAction) {
              case 'approve':
                await userManager.approveUser(userId)
                successCount++
                break
              case 'reject':
                await userManager.rejectUser(userId)
                successCount++
                break
              case 'enable':
                await updateUser(userId, { status: 1 })
                successCount++
                break
              case 'disable':
                await updateUser(userId, { status: 0 })
                successCount++
                break
              case 'delete':
                if (userId !== this.currentUserId) {
                  await deleteUser(userId)
                  successCount++
                } else {
                  failedUsers.push(userId)
                }
                break
            }
          } catch {
            failedUsers.push(userId)
          }
        }
        
        const messages = {
          'approve': '通过',
          'reject': '驳回',
          'enable': '启用',
          'disable': '禁用',
          'delete': '删除'
        }
        
        if (successCount > 0) {
          this.$message.success(`成功${messages[this.batchAction]} ${successCount} 个用户`)
        }
        
        if (failedUsers.length > 0) {
          this.$message.warning(`${failedUsers.length} 个用户操作失败`)
        }
        
        this.loadData()
        this.loadStatistics()
        this.selectedIds = this.selectedIds.filter(id => !failedUsers.includes(id))
        
      } catch {
        this.$message.error('批量操作失败')
      } finally {
        this.batchProcessing = false
        this.batchDialogVisible = false
      }
    },
    
    // 表格选择变化
    handleSelectionChange(selection) {
      this.selectedIds = selection.map(item => item.id)
    },
    
    // 显示用户详情
    showUserDetail(row) {
      this.$alert(
        `
        <div style="text-align: center;">
          <div style="margin-bottom: 20px;">
            <el-avatar 
              :size="80" 
              src="${this.getAvatarUrl(row.avatar)}"
              style="border: 3px solid #409EFF;"
            >
              <span style="font-size: 24px; font-weight: bold; color: white;">
                ${row.name && row.name.charAt(0) || '?'}
              </span>
            </el-avatar>
          </div>
          <div style="margin-bottom: 10px;">
            <strong style="font-size: 18px; color: #303133;">${row.name}</strong>
            <div style="color: #909399; font-size: 14px; margin-top: 5px;">@${row.username}</div>
          </div>
          <div style="text-align: left; margin-top: 20px;">
            <div style="margin-bottom: 8px;">
              <i class="el-icon-phone" style="color: #409EFF; margin-right: 8px;"></i>
              <span>${row.phone || '未设置'}</span>
            </div>
            <div style="margin-bottom: 8px;">
              <i class="el-icon-message" style="color: #409EFF; margin-right: 8px;"></i>
              <span>${row.email || '未设置'}</span>
            </div>
            <div style="margin-bottom: 8px;">
              <i class="el-icon-s-flag" style="color: #409EFF; margin-right: 8px;"></i>
              <span>${this.getRoleText(row.role)}</span>
            </div>
            <div style="margin-bottom: 8px;">
              <i class="el-icon-s-data" style="color: #409EFF; margin-right: 8px;"></i>
              <span>${this.getApprovalText(row.approvalStatus)}</span>
            </div>
            <div style="margin-bottom: 8px;">
              <i class="el-icon-s-data" style="color: #409EFF; margin-right: 8px;"></i>
              <span>账户余额: ¥${row.account && row.account.toFixed(2) || '0.00'}</span>
            </div>
            <div>
              <i class="el-icon-date" style="color: #409EFF; margin-right: 8px;"></i>
              <span>注册时间: ${this.formatDate(row.createTime)}</span>
            </div>
          </div>
        </div>
        `,
        '用户信息',
        {
          dangerouslyUseHTMLString: true,
          confirmButtonText: '关闭',
          customClass: 'user-detail-dialog',
          center: true
        }
      )
    },
    
    // 分页变化
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.loadData()
    },
    
    // 每页条数变化
    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.pageNum = 1
      this.loadData()
    },
    
    // 对话框关闭
    handleDialogClosed() {
      this.form = {
        id: null,
        username: '',
        name: '',
        phone: '',
        email: '',
        role: 'USER',
        password: '',
        approvalStatus: 'APPROVED',
        account: 0.00,
        status: 1,
        description: '',
        shopName: ''
      }
      if (this.$refs.formRef) {
        this.$refs.formRef.clearValidate()
      }
    }
  }
}
</script>

<style scoped>
.user-management {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
}

/* 页面标题样式 */
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  margin-bottom: 20px;
  padding: 30px;
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

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.page-subtitle {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
  font-weight: 400;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-right .el-tag {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  font-weight: 600;
  padding: 8px 16px;
  border-radius: 20px;
  backdrop-filter: blur(10px);
}

/* 过滤卡片样式 */
.filter-card {
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08) !important;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.filter-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12) !important;
}

:deep(.filter-card .el-card__body) {
  padding: 20px;
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
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  flex: 1;
}

.filter-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

:deep(.el-select .el-input__inner),
:deep(.el-input .el-input__inner) {
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
  height: 36px;
  line-height: 36px;
}

:deep(.el-select .el-input__inner:focus),
:deep(.el-input .el-input__inner:focus) {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

:deep(.el-date-editor .el-range-input) {
  border-radius: 8px;
}

/* 统计卡片样式 */
.stats-cards {
  margin-bottom: 20px;
}

:deep(.stats-cards .el-col) {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.7));
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  height: 120px;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.05));
  z-index: 1;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15) !important;
}

:deep(.stat-card .el-card__body) {
  padding: 20px;
  height: 100%;
  position: relative;
  z-index: 2;
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  color: white;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.stat-card:hover .stat-icon {
  transform: scale(1.1) rotate(5deg);
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
  font-weight: 500;
}

.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: #303133;
  margin-bottom: 4px;
  line-height: 1.2;
}

.stat-trend {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 操作按钮区域 */
.operation-section {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  flex-wrap: wrap;
  gap: 10px;
}

.operation-section .el-button {
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.operation-section .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.operation-section .el-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.operation-section .el-button--primary {
  background: linear-gradient(135deg, #409EFF, #66b1ff);
  color: white;
}

.operation-section .el-button--primary:hover {
  background: linear-gradient(135deg, #66b1ff, #409EFF);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.3);
}

.operation-section .el-button--success {
  background: linear-gradient(135deg, #67C23A, #85ce61);
  color: white;
}

.operation-section .el-button--success:hover {
  background: linear-gradient(135deg, #85ce61, #67C23A);
  box-shadow: 0 6px 20px rgba(103, 194, 58, 0.3);
}

.operation-section .el-button--warning {
  background: linear-gradient(135deg, #E6A23C, #ebb563);
  color: white;
}

.operation-section .el-button--warning:hover {
  background: linear-gradient(135deg, #ebb563, #E6A23C);
  box-shadow: 0 6px 20px rgba(230, 162, 60, 0.3);
}

.operation-section .el-button--info {
  background: linear-gradient(135deg, #909399, #a6a9ad);
  color: white;
}

.operation-section .el-button--info:hover {
  background: linear-gradient(135deg, #a6a9ad, #909399);
  box-shadow: 0 6px 20px rgba(144, 147, 153, 0.3);
}

.operation-section .el-button--danger {
  background: linear-gradient(135deg, #F56C6C, #f78989);
  color: white;
}

.operation-section .el-button--danger:hover {
  background: linear-gradient(135deg, #f78989, #F56C6C);
  box-shadow: 0 6px 20px rgba(245, 108, 108, 0.3);
}

/* 表格卡片样式 */
.table-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08) !important;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  min-height: 500px;
  display: flex;
  flex-direction: column;
}

:deep(.table-card .el-card__body) {
  padding: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.table-container {
  flex: 1;
  overflow: hidden;
  border-radius: 12px 12px 0 0;
}

/* 表格样式 */
:deep(.el-table) {
  border-radius: 12px 12px 0 0;
  overflow: hidden;
  border: none;
  font-size: 14px;
}

:deep(.el-table th) {
  background: linear-gradient(135deg, #f5f7fa, #e4e7ed) !important;
  color: #606266;
  font-weight: 600;
  padding: 16px 0;
  border-bottom: 2px solid #ebeef5;
  text-align: center;
  font-size: 14px;
}

:deep(.el-table th .cell) {
  font-weight: 700;
  color: #303133;
  padding: 0 12px;
}

:deep(.el-table td) {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.3s ease;
  text-align: center;
}

:deep(.el-table td .cell) {
  padding: 0 12px;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: #fafbfc;
}

:deep(.el-table__body tr:hover > td) {
  background-color: #f5f7fa !important;
}

:deep(.el-table__body tr:hover) {
  transform: scale(1.002);
  transition: transform 0.2s ease;
  box-shadow: inset 0 0 0 1px #e6f7ff;
}

/* 用户ID样式 */
.user-id {
  font-family: 'Courier New', monospace;
  font-weight: 700;
  color: #409EFF;
  font-size: 13px;
  background: linear-gradient(135deg, #f0f7ff, #e6f7ff);
  padding: 4px 8px;
  border-radius: 6px;
  display: inline-block;
  border: 1px solid #e6f7ff;
  transition: all 0.3s ease;
}

.user-id:hover {
  background: linear-gradient(135deg, #e6f7ff, #d9efff);
  color: #0066cc;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

/* 用户头像样式 */
.user-avatar {
  display: flex;
  justify-content: center;
  align-items: center;
}

.user-avatar .el-avatar {
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 3px solid white;
}

.user-avatar .el-avatar:hover {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
  cursor: pointer;
}

/* 用户信息单元格 */
.user-info-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.user-name {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.user-id-display {
  font-size: 12px;
  color: #909399;
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
}

/* 联系方式单元格 */
.contact-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.contact-phone,
.contact-email {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
}

.contact-phone i,
.contact-email i {
  font-size: 12px;
  color: #909399;
}

.contact-email {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

/* 账户余额单元格 */
.account-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.account-value {
  font-weight: 700;
  color: #67C23A;
  font-size: 15px;
  text-shadow: 0 1px 2px rgba(103, 194, 58, 0.1);
}

.account-trend {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  color: #909399;
}

/* 时间单元格 */
.time-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.time-date {
  font-weight: 500;
  color: #303133;
  font-size: 13px;
}

.time-ago {
  font-size: 12px;
  color: #909399;
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-block;
}

/* 标签样式 */
:deep(.el-tag) {
  transition: all 0.3s ease;
  cursor: pointer;
  border: none;
  font-weight: 600;
  letter-spacing: 0.5px;
}

:deep(.el-tag:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

:deep(.el-tag--primary) {
  background: linear-gradient(135deg, #409EFF, #66b1ff);
  color: white;
}

:deep(.el-tag--success) {
  background: linear-gradient(135deg, #67C23A, #85ce61);
  color: white;
}

:deep(.el-tag--warning) {
  background: linear-gradient(135deg, #E6A23C, #ebb563);
  color: white;
}

:deep(.el-tag--danger) {
  background: linear-gradient(135deg, #F56C6C, #f78989);
  color: white;
}

:deep(.el-tag--info) {
  background: linear-gradient(135deg, #909399, #a6a9ad);
  color: white;
}

/* 审核状态标签 */
.approval-tag {
  border-radius: 12px;
  padding: 4px 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
}

.approval-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 状态文本 */
.status-text {
  font-size: 13px;
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 12px;
  display: inline-block;
}

.status-unknown {
  color: #909399;
  background: #f5f7fa;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
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

/* 分页区域 */
.pagination-section {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa, #f1f3f5);
  border-radius: 0 0 12px 12px;
  border-top: 1px solid #f0f0f0;
}

:deep(.el-pagination) {
  font-weight: 600;
}

:deep(.el-pagination.is-background .btn-next),
:deep(.el-pagination.is-background .btn-prev),
:deep(.el-pagination.is-background .el-pager li) {
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  background: white;
  color: #606266;
  transition: all 0.3s ease;
  margin: 0 4px;
}

:deep(.el-pagination.is-background .btn-next:hover),
:deep(.el-pagination.is-background .btn-prev:hover),
:deep(.el-pagination.is-background .el-pager li:hover) {
  color: #409EFF;
  background: #f0f7ff;
  border-color: #409EFF;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

:deep(.el-pagination.is-background .el-pager li:not(.disabled).active) {
  background: linear-gradient(135deg, #409EFF, #66b1ff);
  color: white;
  border-color: #409EFF;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

:deep(.el-pagination.is-background .el-pager li:not(.disabled).active:hover) {
  background: linear-gradient(135deg, #66b1ff, #409EFF);
  color: white;
  border-color: #409EFF;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

/* 开关样式 */
:deep(.el-switch) {
  transform: scale(1.2);
}

:deep(.el-switch__core) {
  border-radius: 12px;
  height: 24px;
}

:deep(.el-switch__core:after) {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  top: 2px;
  left: 2px;
}

/* 对话框样式 */
:deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.98));
  backdrop-filter: blur(20px);
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  border-bottom: 1px solid #e4e7ed;
  margin: 0;
}

:deep(.el-dialog__title) {
  color: white;
  font-size: 18px;
  font-weight: 700;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

:deep(.el-dialog__headerbtn) {
  top: 20px;
  right: 20px;
}

:deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
  font-size: 20px;
  transition: transform 0.3s ease;
}

:deep(.el-dialog__headerbtn .el-dialog__close:hover) {
  transform: rotate(90deg);
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-dialog__body) {
  padding: 30px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.98));
}

:deep(.el-dialog__footer) {
  padding: 20px 30px;
  border-top: 1px solid #f0f0f0;
  background: linear-gradient(135deg, rgba(248, 249, 250, 0.9), rgba(241, 243, 245, 0.9));
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
}

/* 表单样式 */
.form-container {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 10px;
}

.form-container::-webkit-scrollbar {
  width: 6px;
}

.form-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.form-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.form-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #606266;
  padding-bottom: 8px;
}

:deep(.el-input__inner) {
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
  height: 40px;
  line-height: 40px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.7));
  backdrop-filter: blur(10px);
}

:deep(.el-input__inner:hover) {
  border-color: #c0c4cc;
  background: linear-gradient(135deg, rgba(255, 255, 255, 1), rgba(255, 255, 255, 0.8));
}

:deep(.el-input__inner:focus) {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
  background: linear-gradient(135deg, rgba(255, 255, 255, 1), rgba(255, 255, 255, 0.9));
}

:deep(.el-textarea__inner) {
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.7));
  backdrop-filter: blur(10px);
}

:deep(.el-textarea__inner:hover) {
  border-color: #c0c4cc;
  background: linear-gradient(135deg, rgba(255, 255, 255, 1), rgba(255, 255, 255, 0.8));
}

:deep(.el-textarea__inner:focus) {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
  background: linear-gradient(135deg, rgba(255, 255, 255, 1), rgba(255, 255, 255, 0.9));
}

/* 选择框样式 */
:deep(.el-select) {
  width: 100%;
}

:deep(.el-select .el-input__inner) {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.7));
  backdrop-filter: blur(10px);
}

:deep(.el-select-dropdown) {
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.98));
  backdrop-filter: blur(20px);
}

:deep(.el-select-dropdown__item) {
  padding: 10px 20px;
  transition: all 0.3s ease;
  border-radius: 6px;
  margin: 2px 5px;
}

:deep(.el-select-dropdown__item:hover) {
  background: linear-gradient(135deg, #f0f7ff, #e6f7ff);
  color: #409EFF;
  transform: translateX(5px);
}

:deep(.el-select-dropdown__item.selected) {
  background: linear-gradient(135deg, #409EFF, #66b1ff);
  color: white;
  font-weight: 600;
}

/* 数字输入框样式 */
:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input-number .el-input__inner) {
  text-align: right;
}

:deep(.el-input-number__decrease),
:deep(.el-input-number__increase) {
  background: linear-gradient(135deg, #f5f7fa, #e4e7ed);
  border: 1px solid #e4e7ed;
  color: #606266;
  transition: all 0.3s ease;
}

:deep(.el-input-number__decrease:hover),
:deep(.el-input-number__increase:hover) {
  background: linear-gradient(135deg, #e4e7ed, #d3d6dd);
  color: #409EFF;
  border-color: #409EFF;
}

/* 按钮组样式 */
:deep(.el-button-group) {
  display: flex;
  gap: 1px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

:deep(.el-button-group .el-button) {
  border-radius: 0;
  border: none;
  margin: 0;
}

:deep(.el-button-group .el-button:first-child) {
  border-radius: 8px 0 0 8px;
}

:deep(.el-button-group .el-button:last-child) {
  border-radius: 0 8px 8px 0;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .filter-content {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-left {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-left .el-input,
  .filter-left .el-select,
  .filter-left .el-date-editor {
    width: 100% !important;
    margin: 5px 0;
  }
  
  .filter-right {
    justify-content: center;
    width: 100%;
  }
  
  .operation-section {
    justify-content: center;
  }
  
  .action-buttons {
    flex-direction: column;
    align-items: stretch;
  }
  
  .action-buttons .el-button {
    width: 100%;
    margin: 2px 0;
  }
}

@media (max-width: 768px) {
  .user-management {
    padding: 10px;
  }
  
  .page-header {
    padding: 20px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .stats-cards .el-col {
    width: 100%;
  }
  
  .operation-section {
    flex-direction: column;
  }
  
  .operation-section .el-button {
    width: 100%;
    margin: 5px 0;
  }
  
  .table-card {
    margin: 0 -10px;
    width: calc(100% + 20px);
  }
  
  :deep(.el-table) {
    font-size: 12px;
  }
  
  :deep(.el-table th),
  :deep(.el-table td) {
    padding: 8px 0;
  }
  
  .user-id {
    font-size: 11px;
    padding: 2px 4px;
  }
  
  .user-name {
    font-size: 12px;
  }
  
  .user-id-display {
    font-size: 10px;
  }
  
  .contact-phone,
  .contact-email {
    font-size: 11px;
  }
  
  .account-value {
    font-size: 13px;
  }
  
  .time-date {
    font-size: 11px;
  }
  
  .time-ago {
    font-size: 10px;
  }
  
  :deep(.el-dialog) {
    width: 90% !important;
    max-width: 100%;
  }
  
  .form-container {
    max-height: 50vh;
  }
  
  :deep(.el-dialog__body) {
    padding: 20px;
  }
  
  :deep(.el-dialog__footer) {
    padding: 15px 20px;
  }
  
  .dialog-footer {
    flex-direction: column;
  }
  
  .dialog-footer .el-button {
    width: 100%;
    margin: 5px 0;
  }
}

@media (max-width: 480px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-right {
    margin-top: 10px;
  }
  
  .filter-left {
    flex-direction: column;
  }
  
  .filter-right {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-right .el-button-group {
    flex-direction: column;
  }
  
  .filter-right .el-button {
    width: 100%;
    border-radius: 8px !important;
    margin: 2px 0;
  }
  
  .stat-card {
    height: auto;
  }
  
  .stat-content {
    flex-direction: column;
    text-align: center;
  }
  
  .stat-icon {
    margin-right: 0;
    margin-bottom: 10px;
  }
  
  .pagination-section {
    padding: 10px;
  }
  
  :deep(.el-pagination) {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
  }
  
  :deep(.el-pagination__total),
  :deep(.el-pagination__sizes),
  :deep(.el-pagination__jump) {
    margin: 5px 0;
  }
}

/* 打印样式 */
@media print {
  .operation-section,
  .filter-content,
  .page-header,
  .stats-cards,
  .pagination-section {
    display: none;
  }
  
  .user-management {
    padding: 0;
    background: white;
  }
  
  .table-card {
    box-shadow: none !important;
    border: 1px solid #ddd;
  }
  
  :deep(.el-table) {
    border: 1px solid #ddd;
  }
  
  :deep(.el-table th),
  :deep(.el-table td) {
    border: 1px solid #ddd;
  }
}
</style>