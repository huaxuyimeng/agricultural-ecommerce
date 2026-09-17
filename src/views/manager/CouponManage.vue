/**
 * 优惠券管理页面
 * 文件路径: src/views/manager/CouponManage.vue
 * 功能描述: 管理员发放和管理优惠券，展示优惠券列表（名称、面额、使用条件、有效期、发行量、已领取数、来源类型），
 *           支持新增/编辑/删除优惠券、按名称和状态搜索筛选、分页展示，统计数据概览
 * 关联文件:
 * - src/api/index.js: 提供优惠券增删改查接口
 * - src/views/manager/CouponHistory.vue: 优惠券使用历史页面
 * - src/views/manager/Manager.vue: 后台管理框架布局
 */
<template>
  <div class="coupon-manage-page">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">
          <i class="el-icon-present"></i>
          优惠券管理
        </h2>
        <p class="page-subtitle">管理系统优惠券，由管理员统一发放</p>
      </div>
      <div class="header-right">
        <el-button @click="$router.push('/coupon-history')">
          <i class="el-icon-time"></i>
          使用历史
        </el-button>
        <el-button type="primary" @click="handleAdd">
          <i class="el-icon-plus"></i>
          发放优惠券
        </el-button>
      </div>
    </div>

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

    <el-card class="filter-card" shadow="never">
      <div class="filter-bar">
        <div class="filter-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索优惠券名称"
            clearable
            prefix-icon="el-icon-search"
            class="search-input"
            @keyup.enter.native="loadData"
            @clear="loadData"
          />
          <el-select v-model="statusFilter" placeholder="全部状态" clearable class="status-select" @change="loadData">
            <el-option label="进行中" value="active" />
            <el-option label="已结束" value="expired" />
            <el-option label="未开始" value="pending" />
            <el-option label="已领完" value="exhausted" />
          </el-select>
        </div>
        <div class="filter-right">
          <el-button icon="el-icon-refresh" @click="loadData" circle title="刷新"></el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        class="coupon-table"
        empty-text="暂无优惠券数据"
      >
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="name" label="优惠券名称" min-width="150">
          <template #default="{ row }">
            <span class="coupon-name-text">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="面额" width="100" align="center">
          <template #default="{ row }">
            <span class="amount-text">¥{{ Number(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="使用条件" width="120" align="center">
          <template #default="{ row }">
            <span v-if="Number(row.minAmount) > 0">满¥{{ Number(row.minAmount) }}</span>
            <el-tag v-else type="success" size="mini">无门槛</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="有效期" width="220" align="center">
          <template #default="{ row }">
            <div class="time-cell">
              <span>{{ formatTime(row.startTime) }}</span>
              <span class="time-sep">~</span>
              <span>{{ formatTime(row.endTime) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="发行/已领" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.totalNum === -1" class="unlimited-text">不限量</span>
            <span v-else>{{ row.receivedNum || 0 }} / {{ row.totalNum }}</span>
          </template>
        </el-table-column>
        <el-table-column label="来源" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getSourceTagType(row.source)" size="small">
              {{ getSourceLabel(row.source) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row)" size="small">
              {{ getStatusLabel(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button type="text" size="mini" @click="handleEdit(row)">
                <i class="el-icon-edit"></i> 编辑
              </el-button>
              <el-button type="text" size="mini" class="danger-text" @click="handleDelete(row)">
                <i class="el-icon-delete"></i> 删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

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

    <el-dialog
      :visible.sync="dialogVisible"
      :title="isEdit ? '编辑优惠券' : '发放优惠券'"
      width="560px"
      @close="resetForm"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="100px" label-position="right">
        <el-form-item label="优惠券名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入优惠券名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="优惠面额" prop="amount">
          <el-input-number v-model="form.amount" :min="1" :max="9999" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="使用门槛" prop="minAmount">
          <el-input-number v-model="form.minAmount" :min="0" :max="99999" :precision="2" style="width: 100%;" />
          <span class="form-tip">0表示无门槛使用</span>
        </el-form-item>
        <el-form-item label="发行数量" prop="totalNum">
          <el-input-number v-model="form.totalNum" :min="-1" :max="99999" style="width: 100%;" />
          <span class="form-tip">-1表示不限量</span>
        </el-form-item>
        <el-form-item label="来源类型" prop="source">
          <el-select v-model="form.source" placeholder="请选择来源类型" style="width: 100%;">
            <el-option label="平台发放" :value="1" />
            <el-option label="活动赠送" :value="2" />
            <el-option label="充值赠券" :value="3" />
            <el-option label="消费赠券" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="有效期" prop="dateRange">
          <el-date-picker
            v-model="form.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd HH:mm:ss"
            :default-time="['00:00:00', '23:59:59']"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入优惠券描述信息"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ isEdit ? '保存修改' : '确认发放' }}
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCouponPage, createCoupon, updateCoupon, deleteCoupon } from '@/api/index'

export default {
  name: 'CouponManage',
  data() {
    return {
      loading: false,
      submitting: false,
      searchKeyword: '',
      statusFilter: '',
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      isEdit: false,
      editId: null,
      form: {
        name: '',
        amount: 10,
        minAmount: 0,
        totalNum: 100,
        source: 1,
        dateRange: [],
        description: ''
      },
      rules: {
        name: [
          { required: true, message: '请输入优惠券名称', trigger: 'blur' }
        ],
        amount: [
          { required: true, message: '请输入优惠面额', trigger: 'blur' }
        ],
        dateRange: [
          { required: true, message: '请选择有效期', trigger: 'change' }
        ]
      }
    }
  },
  computed: {
    statsList() {
      const data = this.tableData
      const total = data.length
      const now = new Date()
      const active = data.filter(c => {
        const end = new Date(c.endTime)
        return end >= now && (c.totalNum === -1 || c.receivedNum < c.totalNum)
      }).length
      const expired = data.filter(c => new Date(c.endTime) < now).length
      let totalReceived = 0
      data.forEach(c => { totalReceived += (c.receivedNum || 0) })
      return [
        { key: 'total', label: '优惠券总数', value: total, icon: 'el-icon-present', color: 'linear-gradient(135deg, #667eea, #764ba2)', class: 'stat-purple' },
        { key: 'active', label: '进行中', value: active, icon: 'el-icon-circle-check', color: 'linear-gradient(135deg, #43e97b, #38f9d7)', class: 'stat-green' },
        { key: 'received', label: '已领取', value: totalReceived, icon: 'el-icon-s-order', color: 'linear-gradient(135deg, #f093fb, #f5576c)', class: 'stat-pink' },
        { key: 'expired', label: '已过期', value: expired, icon: 'el-icon-time', color: 'linear-gradient(135deg, #fa709a, #fee140)', class: 'stat-orange' }
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
          pageNum: this.pageNum,
          pageSize: this.pageSize
        }
        if (this.searchKeyword) params.name = this.searchKeyword
        if (this.statusFilter) params.status = this.statusFilter
        const res = await getCouponPage(params)
        if (res.code === 200 && res.data) {
          this.tableData = res.data.records || res.data.list || []
          this.total = res.data.total || 0
        } else {
          this.tableData = []
          this.total = 0
        }
      } catch (err) {
        console.error('获取优惠券列表失败:', err)
        this.$message.error('获取优惠券列表失败')
        this.tableData = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

    handleAdd() {
      this.isEdit = false
      this.editId = null
      this.dialogVisible = true
    },

    handleEdit(row) {
      this.isEdit = true
      this.editId = row.id
      this.form = {
        name: row.name || '',
        amount: row.amount || 10,
        minAmount: row.minAmount || 0,
        totalNum: row.totalNum !== undefined ? row.totalNum : 100,
        source: row.source || 1,
        dateRange: [row.startTime, row.endTime],
        description: row.description || ''
      }
      this.dialogVisible = true
    },

    handleDelete(row) {
      this.$confirm(`确认删除优惠券"${row.name}"吗？删除后不可恢复。`, '删除确认', {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteCoupon(row.id)
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.error(res.msg || '删除失败')
          }
        } catch (err) {
          console.error('删除优惠券失败:', err)
          this.$message.error('删除优惠券失败')
        }
      }).catch(() => {})
    },

    async submitForm() {
      try {
        await this.$refs.form.validate()
      } catch {
        return
      }
      this.submitting = true
      try {
        const data = {
          name: this.form.name,
          amount: this.form.amount,
          minAmount: this.form.minAmount,
          totalNum: this.form.totalNum,
          source: this.form.source,
          startTime: this.form.dateRange[0],
          endTime: this.form.dateRange[1],
          description: this.form.description
        }
        let res
        if (this.isEdit) {
          res = await updateCoupon(this.editId, data)
        } else {
          res = await createCoupon(data)
        }
        if (res.code === 200) {
          this.$message.success(this.isEdit ? '修改成功' : '发放成功')
          this.dialogVisible = false
          this.loadData()
        } else {
          this.$message.error(res.msg || '操作失败')
        }
      } catch (err) {
        console.error('操作失败:', err)
        this.$message.error('操作失败')
      } finally {
        this.submitting = false
      }
    },

    resetForm() {
      this.form = {
        name: '',
        amount: 10,
        minAmount: 0,
        totalNum: 100,
        source: 1,
        dateRange: [],
        description: ''
      }
      this.isEdit = false
      this.editId = null
      if (this.$refs.form) {
        this.$refs.form.resetFields()
      }
    },

    formatTime(time) {
      if (!time) return '-'
      const d = new Date(time)
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${y}-${m}-${day}`
    },

    getSourceLabel(source) {
      const map = { 1: '平台发放', 2: '活动赠送', 3: '充值赠券', 4: '消费赠券' }
      return map[source] || '未知'
    },

    getSourceTagType(source) {
      const map = { 1: 'primary', 2: 'success', 3: 'warning', 4: 'info' }
      return map[source] || 'info'
    },

    getStatusLabel(row) {
      const now = new Date()
      const start = new Date(row.startTime)
      const end = new Date(row.endTime)
      if (now < start) return '未开始'
      if (now > end) return '已结束'
      if (row.totalNum !== -1 && row.receivedNum >= row.totalNum) return '已领完'
      return '进行中'
    },

    getStatusTagType(row) {
      const now = new Date()
      const start = new Date(row.startTime)
      const end = new Date(row.endTime)
      if (now < start) return 'info'
      if (now > end) return 'danger'
      if (row.totalNum !== -1 && row.receivedNum >= row.totalNum) return 'warning'
      return 'success'
    }
  }
}
</script>

<style scoped>
.coupon-manage-page {
  padding: 20px;
  background: #f0f2f5;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.header-left .page-title {
  margin: 0 0 6px 0;
  font-size: 22px;
  color: #1a1a2e;
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-subtitle {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.header-right {
  display: flex;
  gap: 12px;
}

.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  border-radius: 12px;
  border: none;
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 14px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
  flex-shrink: 0;
}

.stat-number {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

.filter-card {
  margin-bottom: 16px;
  border-radius: 12px;
  border: none;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  width: 260px;
}

.status-select {
  width: 140px;
}

.table-card {
  border-radius: 12px;
  border: none;
}

.coupon-table {
  width: 100%;
}

.amount-text {
  color: #f56c6c;
  font-weight: 600;
  font-size: 15px;
}

.unlimited-text {
  color: #67c23a;
  font-weight: 500;
}

.time-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 12px;
}

.time-sep {
  color: #c0c4cc;
}

.coupon-name-text {
  font-weight: 500;
  color: #303133;
}

.action-btns {
  display: flex;
  gap: 4px;
  justify-content: center;
}

.danger-text {
  color: #f56c6c !important;
}

.danger-text:hover {
  color: #e64242 !important;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}
</style>